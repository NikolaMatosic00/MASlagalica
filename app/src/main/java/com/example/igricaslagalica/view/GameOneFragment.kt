package com.example.igricaslagalica.view

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.igricaslagalica.R
import com.example.igricaslagalica.controller.SpojnicaGameController
import com.example.igricaslagalica.model.Connection
import com.example.igricaslagalica.model.Game
import com.example.igricaslagalica.view.adapter.AnswersAdapter
import com.example.igricaslagalica.view.adapter.QuestionsAdapter

class GameOneFragment : Fragment() {

    private lateinit var gameController: SpojnicaGameController
    private lateinit var questionsRecyclerView: RecyclerView
    private lateinit var answersRecyclerView: RecyclerView

    private lateinit var questionsAdapter: QuestionsAdapter
    private lateinit var answersAdapter: AnswersAdapter

    private lateinit var player1Score: TextView
    private lateinit var player2Score: TextView
    var selectedQuestionIndex: Int? = 0
    var selectedAnswerIndex: Int? = 0

    private lateinit var switchPlayerButton: Button
    private lateinit var timer: CountDownTimer
    private val timerDuration = 30000L
    private lateinit var timerTextView: TextView
    private lateinit var currentPlayerTurn: TextView
    private var gameId = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gameController = SpojnicaGameController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gameone, container, false)


        questionsRecyclerView = view.findViewById(R.id.questionsRecyclerView)
        answersRecyclerView = view.findViewById(R.id.answersRecyclerView)


        questionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        answersRecyclerView.layoutManager = LinearLayoutManager(requireContext())


        player1Score = view.findViewById(R.id.player1Score)
        player2Score = view.findViewById(R.id.player2Score)

        timerTextView = view.findViewById(R.id.gameTimer)
        switchPlayerButton = view.findViewById(R.id.submit)
        currentPlayerTurn = view.findViewById(R.id.currentPlayerTurn)

        timer = object : CountDownTimer(timerDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.text = "Time remaining: ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                switchPlayer()

            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()

        val gameId = arguments?.getString("gameId")

        val sharedPreferences = activity?.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val currentPlayerId = sharedPreferences?.getString("currentPlayerId", null)

        if (gameId != null && currentPlayerId != null) {


            gameController.watchGame(gameId) { game ->

                game?.let {
                    handleGameUpdate(it, currentPlayerId)
                } ?: run {

                }
            }
        }
        setupSwitchPlayerButton(gameId)


    }

    private fun setupAdapters() {
        questionsAdapter = QuestionsAdapter(emptyList(), this::onQuestionSelected)
        answersAdapter = AnswersAdapter(emptyList(), this::onAnswerSelected)
        questionsRecyclerView.adapter = questionsAdapter
        answersRecyclerView.adapter = answersAdapter
    }

    private fun handleGameUpdate(game: Game, currentPlayerId: String) {
        val currentPlayerId = getCurrentPlayerId(game)
        val filteredQuestions = game.questionInfo.filter { it.assignedToPlayer == currentPlayerId }

        updateDataForAdapters(filteredQuestions)

        handlePlayerTurn(game, currentPlayerId)
    }

    private fun getCurrentPlayerId(game: Game): String {
        return if (game.currentRound == 0 || game.currentRound == 1) game.player1.toString() else game.player2.toString()
    }

    private fun updateDataForAdapters(filteredQuestions: List<Connection>) {
        questionsAdapter.updateData(filteredQuestions)
        answersAdapter.updateData(filteredQuestions)
    }

    private fun handlePlayerTurn(game: Game, currentPlayerId: String) {
        if (game.currentTurn == currentPlayerId) {

            enableTurnForCurrentPlayer(game, currentPlayerId)
        } else {

            disableTurnForCurrentPlayer()
        }
    }

    private fun enableTurnForCurrentPlayer(game: Game, currentPlayerId: String) {
        switchPlayerButton.isEnabled = true
        getCurrentPlayer(currentPlayerId, game.currentRound)
        timer.start()
    }

    private fun disableTurnForCurrentPlayer() {
        switchPlayerButton.isEnabled = false
        timer.cancel()
    }

    private fun setupSwitchPlayerButton(gameId: String?) {
        switchPlayerButton.setOnClickListener {
            gameId?.let {
                updateGameAndScores(it)
            }
        }
    }

    private fun updateGameAndScores(gameId: String) {
        gameController.getGame(gameId) { game ->
            game?.let {
                handleGameAfterAnswer(it, gameId)
            } ?: run {

            }
        }
    }

    private fun handleGameAfterAnswer(game: Game, gameId: String) {
        val connection = game.questionInfo[selectedQuestionIndex!!]
        gameController.updateGameAfterAnswer(game, game.currentTurn, connection) { success ->
            if (success) {
                updateScore(game.player1Score.toString(), game.player2Score.toString())
                switchTurnAndCheckGameEnd(game, gameId)
            } else {

            }
        }
    }

    private fun switchTurnAndCheckGameEnd(game: Game, gameId: String) {
        gameController.switchTurn(game, game.currentTurn) { success ->
            if (success) {

                getCurrentPlayer(game.currentTurn, game.currentRound)
                switchPlayer()
                if (game.currentRound == 2 && game.currentTurn == game.player1) {
                    val bundle = bundleOf("gameId" to gameId)


                }
                if (game.currentRound == 1 && game.currentTurn != game.player1) {
                    val bundle = bundleOf("gameId" to gameId)


                }
            } else {

            }
        }
    }

    private fun updateScore(scorePlayer1: String, scorePlayer2: String) {
        player1Score.text = "Player 1: $scorePlayer1"
        player2Score.text = "Player 2: $scorePlayer2"
    }

    private fun getCurrentPlayer(currentPlayerId: String, round: Int) {
        gameController.getPlayerName(currentPlayerId) { playerName ->
            currentPlayerTurn.text = "Round $round: $playerName playing"
        }
    }

    private fun switchPlayer() {

        timer.cancel()







        timer.start()
    }

    fun onQuestionSelected(index: Int) {
        selectedQuestionIndex = index
        switchPlayerButton.isEnabled = selectedQuestionIndex != null && selectedAnswerIndex != null
        checkAnswer()
    }

    fun onAnswerSelected(index: Int) {
        selectedAnswerIndex = index
        switchPlayerButton.isEnabled = selectedQuestionIndex != null && selectedAnswerIndex != null
        checkAnswer()
    }

    fun checkAnswer() {
        val gameId = arguments?.getString("gameId")
        if (selectedQuestionIndex != null && selectedAnswerIndex != null) {
            if (gameId != null) {
                gameController.getGame(gameId) { game ->
                    if (game != null) {
                        val connection = game.questionInfo[selectedQuestionIndex!!]
                        val userAnswer =

                            gameController.answerQuestion(
                                game,
                                game.currentTurn,
                                connection,
                                selectedQuestionIndex!!,
                                selectedAnswerIndex!!
                            ) { success ->
                                if (success) {

                                } else {

                                }
                            }
                    }
                }
            }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance() = GameOneFragment()
    }
}
