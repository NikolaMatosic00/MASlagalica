package com.example.igricaslagalica.controller

import android.content.ContentValues.TAG
import android.util.Log
import com.example.igricaslagalica.model.Connection
import com.example.igricaslagalica.model.Game
import com.google.firebase.firestore.FirebaseFirestore

enum class GameState {
    ROUND_ONE_PLAYER_ONE,
    ROUND_ONE_PLAYER_TWO,
    ROUND_TWO_PLAYER_ONE,
    ROUND_TWO_PLAYER_TWO

}

class SpojnicaGameController {

    private val db = FirebaseFirestore.getInstance()
    fun watchGame(gameId: String, onUpdate: (Game?) -> Unit) {


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

    fun checkAnswer(connection: Connection, answer: String): Boolean {
        return connection.answer == answer
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

    fun updateGameAfterAnswer(
        game: Game,
        currentPlayerId: String,
        connection: Connection,
        callback: (Boolean) -> Unit
    ) {
        var playerScore = 0


        for (connection in game.questionInfo) {

            if (connection.answeredBy == currentPlayerId && connection.correct) {
                playerScore += 2
            }
        }


        if (connection.correct) {
            if (game.player1 == currentPlayerId) {
                game.player1Score += playerScore
            } else if (game.player2 == currentPlayerId) {
                game.player2Score += playerScore
            }
        }
        game.id?.let { gameId ->
            updateGameField(gameId, "player1Score", game.player1Score) { success1 ->
                if (success1) {
                    updateGameField(gameId, "player2Score", game.player2Score) { success2 ->
                        callback(success2)
                    }
                } else {
                    callback(false)
                }
            }
        }

    }

    fun answerQuestion(
        game: Game,
        currentPlayerId: String,
        connection: Connection,
        questionIndex: Int,
        answerIndex: Int,
        callback: (Boolean) -> Unit
    ) {

        connection.correct = processAnswer(questionIndex, answerIndex)
        connection.answered = true
        connection.answeredBy = currentPlayerId


        game.id?.let { gameId ->
            updateGameField(gameId, "questionInfo", game.questionInfo) { success ->
                if (success) {

                    callback(true)
                } else {

                    callback(false)
                }
            }
        }

    }


    fun handleRound1(game: Game, callback: (Boolean) -> Unit) {
        val allQuestionsAnsweredCorrectly = game.questionInfo.all { it.correct }

        if (allQuestionsAnsweredCorrectly) {

            game.currentRound = 2
            game.currentTurn = game.player2!!
            updateGameField(game.id!!, "currentRound", 2) { success ->
                if (success) {
                    updateGameField(game.id!!, "currentTurn", game.player2) { success ->
                        callback(success)
                    }
                } else {
                    callback(false)
                }
            }
        } else {

            game.player1?.let {
                switchTurn(game, it) { success ->
                    callback(success)
                }
            }
        }
    }

    fun handleRound2(game: Game, callback: (Boolean) -> Unit) {
        val allQuestionsAnsweredCorrectly =
            game.questionInfo.all { it.correct && it.answeredBy == game.player2 }

        if (allQuestionsAnsweredCorrectly) {


            callback(true)
        } else {

            game.player1?.let {
                switchTurn(game, it) { success ->
                    callback(success)
                }
            }
        }
    }

    fun switchTurn(game: Game, currentPlayerId: String, callback: (Boolean) -> Unit) {
        val otherPlayerId = if (game.player1 == currentPlayerId) game.player2 else game.player1
        if (otherPlayerId != null) {
            game.currentTurn = otherPlayerId
        }

        if (game.currentRound == 1) {

            val allRound1QuestionsAnswered =
                game.questionInfo.all { it.assignedToPlayer != null && it.correct }
            if (allRound1QuestionsAnswered) {

                game.currentRound = 2

                game.id?.let { gameId ->
                    updateGameField(gameId, "currentRound", game.currentRound, callback)
                }
                return
            }
        }

        if (game.currentRound == 2 && game.player2 == currentPlayerId) {

            val allRound2QuestionsAnswered =
                game.questionInfo.all { it.assignedToPlayer != null && it.correct }
            if (allRound2QuestionsAnswered) {

                game.id?.let { gameId ->
                    updateGameField(gameId, "currentRound", 3, callback)
                }
                callback(true)
                return
            }
        }


        game.id?.let { gameId ->
            if (otherPlayerId != null) {
                updateGameField(gameId, "currentTurn", otherPlayerId, callback)
            }
        }
    }

    fun saveResultToDatabase(gameId: String, player1Score: Int, player2Score: Int) {
        updateGameField(gameId, "player1Score", player1Score) { success1 ->
            if (success1) {
                updateGameField(gameId, "player2Score", player2Score) { success2 ->
                }
            }
        }
    }

    fun generateNewQuestions(
        game: Game,
        assignedToPlayer: String = "",
        onQuestionsFetched: (List<Connection>) -> Unit
    ) {
        db.collection("spojnicaQuestions").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val allQuestions =
                    task.result?.documents?.mapNotNull { it.toObject(Connection::class.java) }
                if (allQuestions != null) {
                    val gameQuestions = allQuestions.shuffled().take(5).map {
                        Connection(
                            question = it.question,
                            answer = it.answer,

                            assignedToPlayer = if (assignedToPlayer.isNotEmpty()) assignedToPlayer else game.currentTurn
                        )
                    }
                    onQuestionsFetched(gameQuestions)
                }
            }
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


    fun processAnswer(questionIndex: Int, answerIndex: Int): Boolean {
        return questionIndex == answerIndex
    }

}
