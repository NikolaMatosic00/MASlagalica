package com.example.igricaslagalica.controller

import android.util.Log
import com.example.igricaslagalica.model.Connection
import com.google.firebase.firestore.FirebaseFirestore


class SpojnicaSinglePlayer {

    private val db = FirebaseFirestore.getInstance()
    private var currentPlayer: Int = 1
    private val playerScores = mutableMapOf(1 to 0, 2 to 0)
    private val questions = mutableListOf(
        Connection("Bengalski", "Tigar"),
        Connection("Opalo", "Lišće"),
        Connection("Velika", "Zgrada"),
        Connection("Brzi", "Gonzles"),
        Connection("Visok", "Visok"),

        )
    private val madeConnections = mutableListOf<Pair<Int, Int>>()

    private var gameState = GameState.ROUND_ONE_PLAYER_ONE
    private val questionsAndAnswers = mutableListOf<Pair<String, String>>()
    private val unansweredQuestionIndices = mutableListOf<Int>()
    private val unansweredConnections = questions.toMutableList()


    fun loadGameData(callback: (List<Connection>, List<Connection>) -> Unit) {
        val connections = generateNewConnections()

        callback(connections, connections)

        gameState = GameState.ROUND_ONE_PLAYER_ONE

        unansweredQuestionIndices.addAll(questions.indices)
    }

    fun checkConnection(connection: Connection): Boolean {


        return connection.correct
    }

    fun makeConnection(questionIndex: Int, answerIndex: Int) {
        val connection = questions[questionIndex]
        if (connection.answeredBy == null && checkAnswer(questionIndex, answerIndex)) {
            val connectionPair = Pair(questionIndex, answerIndex)
            madeConnections.add(connectionPair)

            playerScores[currentPlayer] = playerScores.getValue(currentPlayer) + 2
            Log.d("GameController", "Correct answer! Score is now: ")

            connection.answered = true
            unansweredConnections.remove(connection)
            checkGameState()
        } else {
            Log.d("GameController", "Incorrect answer or connection already made.")
        }
    }

    fun generateNewConnections(): List<Connection> {


        questions.shuffle()

        return questions.map { Connection(it.question, it.answer) }
    }

    fun checkGameState() {
        if (questionsAndAnswers.size == 5) {

            when (gameState) {
                GameState.ROUND_ONE_PLAYER_ONE -> gameState = GameState.ROUND_ONE_PLAYER_TWO
                GameState.ROUND_ONE_PLAYER_TWO -> gameState = GameState.ROUND_TWO_PLAYER_ONE
                GameState.ROUND_TWO_PLAYER_ONE -> gameState = GameState.ROUND_TWO_PLAYER_TWO
                GameState.ROUND_TWO_PLAYER_TWO -> endGame()
            }
        }
    }

    fun endGame() {

    }

    fun checkAnswer(questionIndex: Int, answerIndex: Int): Boolean {

        val connection = questions[questionIndex]
        if (questionIndex == answerIndex) {
            connection.answeredBy = currentPlayer.toString()
            return true
        }
        return false
    }

    fun switchPlayer() {
        currentPlayer = if (currentPlayer == 1) 2 else 1
        when (gameState) {
            GameState.ROUND_ONE_PLAYER_ONE -> gameState = GameState.ROUND_ONE_PLAYER_TWO
            GameState.ROUND_ONE_PLAYER_TWO -> gameState = GameState.ROUND_TWO_PLAYER_ONE
            GameState.ROUND_TWO_PLAYER_ONE -> gameState = GameState.ROUND_TWO_PLAYER_TWO
            GameState.ROUND_TWO_PLAYER_TWO -> gameState = GameState.ROUND_ONE_PLAYER_ONE
        }


    }

    fun getUnansweredConnections(): List<Connection> {
        return unansweredConnections
    }

    fun getScores(): Map<Int, Int> {
        return playerScores
    }
}
