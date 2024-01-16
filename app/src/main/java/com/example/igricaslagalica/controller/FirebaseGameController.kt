package com.example.igricaslagalica.controller

import android.content.ContentValues.TAG
import android.util.Log
import com.example.igricaslagalica.model.Connection
import com.example.igricaslagalica.model.Game
import com.example.igricaslagalica.model.KoZnaZna
import com.example.igricaslagalica.model.Player
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class FirebaseGameController {
    private val db = FirebaseFirestore.getInstance()

    fun startGame(playerId: String, onComplete: (Boolean, String) -> Unit) {
        val game = Game(player1 = playerId, currentTurn = playerId)
        db.collection("games").add(game).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val gameId = task.result?.id ?: ""
                createGame(playerId, gameId) { updatedGameId ->
                    onComplete(true, updatedGameId)
                }

            } else {
                onComplete(false, "")
            }
        }
    }

    fun joinGame(gameId: String, playerId: String, onComplete: (Boolean) -> Unit) {
        db.collection("games").document(gameId)
            .update("player2", playerId, "status", "playing")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true)
                } else {
                    Log.e("FirebaseGameController", "Error joining game: ", task.exception)
                    onComplete(false)
                }
            }
    }

    fun getWaitingGame(playerId: String, onGameFound: (Game?) -> Unit) {
        db.collection("games")
            .whereEqualTo("status", "waiting")
            .whereNotEqualTo("player1", playerId)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {

                    val document = documents.documents[0]
                    val game = document.toObject(Game::class.java)
                    game?.id = document.id
                    onGameFound(game)
                } else {
                    onGameFound(null)
                }
            }
    }

    fun getGame(gameId: String, callback: (Game?) -> Unit) {
        db.collection("games").document(gameId).get()
            .addOnSuccessListener { documentSnapshot ->
                val game = documentSnapshot.toObject(Game::class.java)
                Log.w(TAG, "Game objekt preuzet $game}")

                if (game != null) {
                    Log.w(TAG, "Game objekt preuzet $game lista ${game.questionInfo.size}")
                }
                callback(game)

            }
            .addOnFailureListener {
                callback(null)
            }
    }

    fun createGame(playerId: String, gameId: String, onQuestionsFetched: (String) -> Unit) {
        val gameRef = db.collection("games").document(gameId)

        val game = Game(
            id = gameId,
            player1 = playerId,
            currentTurn = playerId
        )


        FirebaseFirestore.getInstance().collection("koZnaZnaQuestions").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val allQuestions =
                        task.result?.documents?.mapNotNull { it.toObject(KoZnaZna::class.java) }

                    if (allQuestions != null) {
                        val gameQuestions = allQuestions.shuffled().take(5).map {
                            KoZnaZna(
                                id = it.id,
                                questionText = it.questionText,
                                options = it.options,
                                correctAnswer = it.correctAnswer,
                                player1Answered = it.player1Answered,
                                player1AnswerTime = it.player1AnswerTime,
                                player2Answered = it.player2Answered,
                                player2AnswerTime = it.player2AnswerTime
                            )
                        }
                        game.koZnaZnaQuestions = gameQuestions


                        FirebaseFirestore.getInstance().collection("spojnicaQuestions").get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val allQuestions =
                                        task.result?.documents?.mapNotNull { it.toObject(Connection::class.java) }

                                    if (allQuestions != null) {
                                        val gameQuestions = allQuestions.shuffled().take(5).map {
                                            Connection(
                                                question = it.question,
                                                answer = it.answer,
                                                assignedToPlayer = playerId
                                            )
                                        }
                                        game.questionInfo = gameQuestions


                                        gameRef.set(game).addOnSuccessListener {

                                            Log.w(TAG, "Game updated: $gameId")


                                            onQuestionsFetched(gameId)
                                        }
                                    } else {

                                    }
                                }
                            }
                    }
                }
            }
    }

    fun listenForGameChanges(gameId: String, gameCallback: (Game?) -> Unit) {
        val gameRef = db.collection("games").document(gameId)
        gameRef.addSnapshotListener { documentSnapshot, e ->
            if (e != null) {
                Log.e("ListenForGameChanges", "Error listening for game changes", e)
                gameCallback(null)
                return@addSnapshotListener
            }

            val game = documentSnapshot?.toObject(Game::class.java)
            gameCallback(game)
        }
    }

    fun updatePlayerStars(playerId: String, value: Any, callback: (Boolean) -> Unit) {
        db.collection("players").document(playerId)
            .update("score", value)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun getCurrentStars(playerId: String, onComplete: (Int?) -> Unit) {
        db.collection("players").document(playerId).get().addOnSuccessListener { documentSnapshot ->
            val playerStars = documentSnapshot.getLong("score")?.toInt()
            onComplete(playerStars)
        }.addOnFailureListener { e ->
            Log.w(TAG, "Error retrieving player name", e)
            onComplete(null)
        }
    }

    fun getPlayerName(playerId: String, onComplete: (String?) -> Unit) {
        db.collection("players").document(playerId).get().addOnSuccessListener { documentSnapshot ->
            val playerName = documentSnapshot.getString("username")
            onComplete(playerName)
        }.addOnFailureListener { e ->
            Log.w(TAG, "Error retrieving player name", e)
            onComplete(null)
        }
    }

    fun updateGameField(gameId: String, field: String, value: Any, callback: (Boolean) -> Unit) {
        db.collection("games").document(gameId)
            .update(field, value)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun getRankedPlayers(rankedPlayersCallback: (List<Player>) -> Unit) {
        val playersCollection = db.collection("players")
        playersCollection.orderBy("score", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val rankedPlayersList = mutableListOf<Player>()
                for (documentSnapshot in querySnapshot) {
                    val player = documentSnapshot.toObject(Player::class.java)
                    rankedPlayersList.add(player)
                }
                Log.w("ol", "|Playerlist $rankedPlayersList")

                rankedPlayersCallback(rankedPlayersList)
            }
            .addOnFailureListener { e ->

                rankedPlayersCallback(emptyList())
            }
    }

    fun listenForPlayerCompletion(
        gameId: String,
        player: String,
        completionCallback: (Boolean) -> Unit
    ) {
        val gameRef = db.collection("games").document(gameId)
        gameRef.addSnapshotListener { documentSnapshot, e ->
            if (e != null) {
                Log.e("ListenForPlayerCompletion", "Error listening for player completion", e)
                completionCallback(false)
                return@addSnapshotListener
            }

            val game = documentSnapshot?.toObject(Game::class.java)
            val isPlayerDone = when (player) {
                "player1" -> game?.isPlayer1Done ?: false
                "player2" -> game?.isPlayer2Done ?: false
                else -> false
            }

            completionCallback(isPlayerDone)
        }
    }


    fun fetchQuestionsForIgra1(): Task<List<KoZnaZna>> {
        return FirebaseFirestore.getInstance().collection("koZnaZnaQuestions").get()
            .continueWith { task ->
                val allQuestions =
                    task.result?.documents?.mapNotNull { it.toObject(KoZnaZna::class.java) }
                allQuestions?.shuffled()?.take(5)?.map {
                    KoZnaZna(
                        id = it.id,
                        questionText = it.questionText,
                        options = it.options,
                        correctAnswer = it.correctAnswer,
                        player1Answered = it.player1Answered,
                        player1AnswerTime = it.player1AnswerTime,
                        player2Answered = it.player2Answered,
                        player2AnswerTime = it.player2AnswerTime
                    )
                } ?: listOf()
            }
    }

    fun fetchQuestionsForIgra2(): Task<List<Connection>> {
        return FirebaseFirestore.getInstance().collection("spojniceQuestion").get()
            .continueWith { task ->
                val allQuestions =
                    task.result?.documents?.mapNotNull { it.toObject(Connection::class.java) }
                allQuestions?.shuffled()?.take(5)?.map {
                    Connection(
                        question = it.question,
                        answer = it.answer,
                        assignedToPlayer = it.assignedToPlayer
                    )
                } ?: listOf()
            }
    }

}

