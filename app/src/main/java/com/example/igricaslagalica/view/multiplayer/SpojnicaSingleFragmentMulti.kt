package com.example.igricaslagalica.view.multiplayer

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.igricaslagalica.R
import com.example.igricaslagalica.controller.FirebaseGameController
import com.example.igricaslagalica.controller.SpojnicaGameController
import com.example.igricaslagalica.model.Connection
import com.example.igricaslagalica.model.Game
import com.example.igricaslagalica.view.GameOneFragment
import com.example.igricaslagalica.view.adapter.AnswersAdapter
import com.example.igricaslagalica.view.adapter.QuestionsAdapter
import com.google.firebase.auth.FirebaseAuth

class SpojnicaSingleFragmentMulti : Fragment() {

    private lateinit var gameController: SpojnicaGameController
    private lateinit var firebaseGameController: FirebaseGameController

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
    private var hasStartedGame: Boolean = false
    private var showUnansweredQuestions: Boolean = false
    private var isRoundTwo: Boolean = false
    private var isGameDone: Boolean = false


    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gameController = SpojnicaGameController()
        firebaseGameController = FirebaseGameController()
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


            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()

        val gameId = arguments?.getString("gameId")



        if (gameId != null) {
            firebaseGameController.listenForGameChanges(gameId) { updatedGame ->
                if (updatedGame != null && currentUserId != null) {
                    handleGameUpdate(updatedGame, currentUserId)
                    setupSwitchPlayerButton(updatedGame)
                } else {


                }
            }
        }


    }

    private fun setupAdapters() {
        questionsAdapter = QuestionsAdapter(emptyList(), this::onQuestionSelected)
        answersAdapter = AnswersAdapter(emptyList(), this::onAnswerSelected)
        questionsRecyclerView.adapter = questionsAdapter
        answersRecyclerView.adapter = answersAdapter
    }

    private fun handleGameUpdate(game: Game, currentPlayer: String) {

        gameId = game.id.toString()
        if (!hasStartedGame && game.isPlayer1Done && game.isPlayer2Done) {
            hasStartedGame = true
            val currentPlayerTurn = game.currentTurn
            val filteredQuestions =
                game.questionInfo.filter { it.assignedToPlayer == currentPlayerTurn }
            updateDataForAdapters(filteredQuestions)
            handlePlayerTurn(game, currentPlayer)

        }
        if (currentUserId != null && hasStartedGame) {
            handlePlayerTurn(game, currentUserId)
        }


        if (!isRoundTwo && game.currentRound == 2) {
            game.player2?.let { it1 ->
                gameController.generateNewQuestions(game, it1) { newQuestions ->
                    game.questionInfo = newQuestions
                    Log.d("TAG", "Udje u Transition ${game.player2} !@ $newQuestions +++ ")
                    game.id?.let { gameId ->
                        gameController.updateGameField(
                            gameId,
                            "questionInfo",
                            game.questionInfo
                        ) { success ->
                            if (success) {
                                val filteredQuestions =
                                    game.questionInfo.filter { it.assignedToPlayer == game.player2 }
                                updateDataForAdapters(filteredQuestions)
                            }

                        }
                    }
                }
            }

            isRoundTwo = true
        }

        if (!showUnansweredQuestions && game.currentTurn == game.player2 && game.currentRound == 1) {
            val filteredQuestions =
                game.questionInfo.filter { it.assignedToPlayer == game.player1 && !it.correct }
            updateDataForAdapters(filteredQuestions)
            showUnansweredQuestions = true
        }
        if (!showUnansweredQuestions && game.currentTurn == game.player1 && game.currentRound == 2) {
            val filteredQuestions =
                game.questionInfo.filter { it.assignedToPlayer == game.player2 && !it.correct }
            updateDataForAdapters(filteredQuestions)
            showUnansweredQuestions = true
        }

        if (isGameDone || game.currentRound == 3) {

            switchPlayerButton.text = "Next game"
            switchPlayerButton.isEnabled = true
            currentPlayerTurn.text =
                "This game is done and you need to press Next game to go to next game"

            questionsAdapter.isInteractionEnabled = false
            answersAdapter.isInteractionEnabled = false
            timer.start()
        }
    }

    private fun updateDataForAdapters(filteredQuestions: List<Connection>) {
        questionsAdapter.updateData(filteredQuestions)
        answersAdapter.updateData(filteredQuestions)
    }

    private fun handlePlayerTurn(game: Game, currentPlayer: String) {
        if (game.currentTurn == currentPlayer) {

            enableTurnForCurrentPlayer(game, currentPlayer)
        } else {

            disableTurnForCurrentPlayer()
        }
    }

    private fun enableTurnForCurrentPlayer(game: Game, currentPlayerId: String) {
        switchPlayerButton.isEnabled = true
        if (game.currentRound != 3) {
            getCurrentPlayer(currentPlayerId, game.currentRound)
        } else {
            currentPlayerTurn.text =
                "This game is done and you need to press Next game to go to next game"

        }
        questionsAdapter.isInteractionEnabled = true
        answersAdapter.isInteractionEnabled = true

    }

    private fun disableTurnForCurrentPlayer() {
        switchPlayerButton.isEnabled = false
        currentPlayerTurn.text = "Wait for your turn "
        questionsAdapter.isInteractionEnabled = false
        answersAdapter.isInteractionEnabled = false


    }


    private fun setupSwitchPlayerButton(game: Game) {
        switchPlayerButton.setOnClickListener {
            game.id?.let { gameId ->
                if (game.currentRound == 1 && game.currentTurn == game.player2) {


                    game.currentRound++
                    gameController.updateGameField(
                        gameId,
                        "currentRound",
                        game.currentRound
                    ) { success ->
                        if (success) {

                        }

                    }

                } else if (game.currentRound == 2 && game.currentTurn == game.player1) {

                    isGameDone = true
                    game.currentRound++
                    gameController.updateGameField(
                        gameId,
                        "currentRound",
                        game.currentRound
                    ) { success ->
                        if (success) {

                        }

                    }
                } else {

                    if (currentUserId == game.player1) {
                        gameController.handleRound1(game) { success ->
                            if (success) {

                            } else {

                            }
                        }
                    } else if (currentUserId == game.player2 && game.currentRound == 2) {

                        gameController.handleRound2(game) { success ->
                            if (success) {

                                isGameDone = true

                            } else {

                            }
                        }
                    }


                }
                if (switchPlayerButton.text == "Next game") {
                    game.player1?.let { it1 ->
                        gameController.updateGameField(
                            gameId, "currentTurn",
                            it1
                        ) { suc ->
                            if (suc) {
                                val bundle = bundleOf("gameId" to gameId)
                                findNavController().navigate(
                                    R.id.action_spojnicaSingleFragmentMulti_to_asocijacijaGameMulti,
                                    bundle
                                )

                            }

                        }
                    }
                }
                handleGameAfterAnswer(game, gameId)
            }

        }
    }

    private fun handleGameAfterAnswer(game: Game, gameId: String) {
        val connection = game.questionInfo[selectedQuestionIndex!!]
        gameController.updateGameAfterAnswer(game, game.currentTurn, connection) { success ->
            if (success) {
                updateScore(game, game.player1Score, game.player2Score)
                switchTurnAndCheckGameEnd(game, gameId)
            } else {

            }
        }
    }

    private fun switchTurnAndCheckGameEnd(game: Game, gameId: String) {
        if (currentUserId != null) {
            gameController.switchTurn(game, currentUserId) { success ->
                if (success) {
                    getCurrentPlayer(game.currentTurn, game.currentRound)
                }
            }
        }
    }

    private fun updateScore(game: Game, scorePlayer1: Int, scorePlayer2: Int) {
        player1Score.text = "Player 1: $scorePlayer1"
        player2Score.text = "Player 2: $scorePlayer2"
        var player1FinalScore = scorePlayer1
        var player2FinalScore = scorePlayer2

        player1FinalScore += game.player1Score
        player2FinalScore += game.player2Score

        gameController.saveResultToDatabase(gameId, player1FinalScore, player2FinalScore)

    }

    private fun getCurrentPlayer(currentPlayerId: String, round: Int) {
        gameController.getPlayerName(currentPlayerId) { playerName ->
            currentPlayerTurn.text = "Round $round: $playerName playing"
        }
    }

    private fun switchPlayer(game: Game) {

        timer.cancel()



        if (currentUserId != null) {
            gameController.switchTurn(game, currentUserId) { success ->
                if (success) {

                    timer.start()
                } else {

                }
            }
        }







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
