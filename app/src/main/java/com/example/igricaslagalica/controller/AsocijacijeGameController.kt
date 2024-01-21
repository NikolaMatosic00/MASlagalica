package com.example.igricaslagalica.controller

import com.example.igricaslagalica.model.AsocijacijaMultiplayer
import com.example.igricaslagalica.model.Game
import com.google.firebase.firestore.FirebaseFirestore

class AsocijacijeGameController {

    private val db = FirebaseFirestore.getInstance()

    fun saveResultToDatabase(
        gameId: String,
        player1Score: Int,
        player2Score: Int,
        nextTurn: String
    ) {
        updateGameField(gameId, "player1ScoreAssocijacija", player1Score) { success1 ->
            if (success1) {
                updateGameField(gameId, "player1ScoreAssocijacija", player2Score) { success2 ->
                    updateGameField(gameId, "currentTurn", nextTurn) { success3 ->
                    }

                }
            }
        }
    }

    fun updateQuestionAssocijacija(game: Game, callback: (Boolean) -> Unit) {
        val gameDocumentRef = db.collection("games").document(game.id!!)
        val updatedQuestions = game.asocijacijaQuestions.toMutableList()
        if (updatedQuestions != null && updatedQuestions.size > 0) {
            val indexToUpdate = 0
            updatedQuestions[indexToUpdate].assignedToPlayer = game.player1
            updatedQuestions[1].assignedToPlayer = game.player2



            gameDocumentRef.update("asocijacijaQuestions", updatedQuestions)
                .addOnSuccessListener {

                    callback(true)
                }
                .addOnFailureListener { e ->

                    callback(false)
                }
        }


    }

    fun generateNewQuestions(
        game: Game,
        assignedPlayer: String = "",
        onQuestionsFetched: (List<AsocijacijaMultiplayer>) -> Unit
    ) {

        db.collection("asocijacijaQuestions").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val allQuestions =
                    task.result?.documents?.mapNotNull { it.toObject(AsocijacijaMultiplayer::class.java) }
                if (allQuestions != null) {
                    val gameQuestions = allQuestions.shuffled().mapIndexed { index, question ->
                        val assignedPlayer = if (index == 0) game.player1 else game.player2
                        AsocijacijaMultiplayer(
                            question.asocijacijaList,
                            question.asocijacijaListOne,
                            question.asocijacijaTwo,
                            question.asocijacijaThree,
                            question.asocijacijaKonacnoResenje,
                            null,
                            assignedPlayer
                        )
                    }












                    onQuestionsFetched(gameQuestions)
                    game.id?.let { updateGameField(it, "asocijacijaQuestions", gameQuestions) {} }
                }
            }
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

    fun updateInteractions(gameId: String, interactions: List<Map<String, Int>>) {
        val gameDocumentRef = db.collection("games").document(gameId)
        gameDocumentRef.update("interactionsAsocijacija", interactions)

    }

    fun switchTurn(game: Game, currentPlayerId: String, callback: (Boolean) -> Unit) {
        val otherPlayerId = if (game.player1 == currentPlayerId) game.player2 else game.player1
        if (otherPlayerId != null) {
            game.currentTurn = otherPlayerId
        }
        game.id?.let { gameId ->
            if (otherPlayerId != null) {
                updateGameField(gameId, "currentTurn", otherPlayerId, callback)
            }
        }
    }

    fun saveResultPlayer1(gameId: String, player1Score: Int) {
        updateGameField(gameId, "player1Score", player1Score) { success1 ->
            if (success1) {


            }
        }
    }

    fun saveResultPlayer2(gameId: String, player2Score: Int) {
        updateGameField(gameId, "player2Score", player2Score) { success2 ->
            if (success2) {
            }
        }
    }
}