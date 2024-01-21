package com.example.igricaslagalica.controller

import android.content.ContentValues.TAG
import android.util.Log
import com.example.igricaslagalica.model.Game
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions


class KoZnaZnaController {
    private val db = FirebaseFirestore.getInstance()
    fun fetchGame(gameId: String, onSuccess: (Game) -> Unit, onFailure: (Exception) -> Unit) {
        val gameRef = FirebaseFirestore.getInstance().collection("games").document(gameId)
        gameRef.get()
            .addOnSuccessListener { document ->
                val game = document.toObject(Game::class.java)
                if (game != null) {
                    onSuccess(game)
                } else {
                    onFailure(Exception("Game not found"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
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

    fun saveAnswerForPlayer1(
        gameId: String,
        questionIndex: Int,
        player1AnswerTime: Long,
        player1Answered: Int
    ) {
        val gameRef = db.collection("games").document(gameId)

        getGame(gameId) { game ->
            if (game != null) {
                game.koZnaZnaQuestions[questionIndex].player1Answered = player1Answered
                game.koZnaZnaQuestions[questionIndex].player1AnswerTime = player1AnswerTime
                gameRef.set(game, SetOptions.merge())
            }
        }

    }

    fun saveAnswerForPlayer2(
        gameId: String,
        questionIndex: Int,
        player2Answered: Int,
        player2AnswerTime: Long
    ) {
        val gameRef = db.collection("games").document(gameId)

        getGame(gameId) { game ->
            if (game != null) {
                game.koZnaZnaQuestions[questionIndex].player2Answered = player2Answered
                game.koZnaZnaQuestions[questionIndex].player2AnswerTime = player2AnswerTime
                gameRef.set(game, SetOptions.merge())
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