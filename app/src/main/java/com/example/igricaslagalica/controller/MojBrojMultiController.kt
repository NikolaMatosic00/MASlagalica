package com.example.igricaslagalica.controller

import com.example.igricaslagalica.model.Game
import com.example.igricaslagalica.model.Skocko
import com.google.firebase.firestore.FirebaseFirestore

class MojBrojMultiController {

    val db = FirebaseFirestore.getInstance()

    private fun makeRandomCombination(): List<Skocko> {
        return List(4) { Skocko.values().random() }
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