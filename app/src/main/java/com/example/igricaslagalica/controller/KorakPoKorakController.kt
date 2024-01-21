package com.example.igricaslagalica.controller

import com.example.igricaslagalica.model.Game
import com.example.igricaslagalica.model.KorakPoKorak
import com.google.firebase.firestore.FirebaseFirestore

class KorakPoKorakController {
    val db = FirebaseFirestore.getInstance()

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

    fun generateNewQuestions(
        game: Game,
        assignedToPlayer: String = "",
        onQuestionsFetched: (List<KorakPoKorak>) -> Unit
    ) {
        db.collection("korakPoKorakQuestions").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val allQuestions =
                    task.result?.documents?.mapNotNull { it.toObject(KorakPoKorak::class.java) }
                if (allQuestions != null) {
                    val shuffledQuestions = allQuestions.shuffled()


                    val player1Questions = shuffledQuestions.take(1).map { question ->
                        game.player1?.let {
                            KorakPoKorak(
                                id = question.id,
                                pojmovi = question.pojmovi,
                                odgovor = question.odgovor,
                                assignedToPlayer = it
                            )
                        }
                    }

                    val player2Questions = shuffledQuestions.drop(1).take(1).map { question ->
                        game.player2?.let {
                            KorakPoKorak(
                                id = question.id,
                                pojmovi = question.pojmovi,
                                odgovor = question.odgovor,
                                assignedToPlayer = it
                            )
                        }
                    }

                    val gameQuestions = player1Questions + player2Questions
                    onQuestionsFetched(gameQuestions as List<KorakPoKorak>)


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

}