package com.example.igricaslagalica.view.signleplayer

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.R
import com.example.igricaslagalica.SharedViewModel
import com.example.igricaslagalica.controller.KoZnaZnaController
import com.example.igricaslagalica.data.StaticData
import com.example.igricaslagalica.databinding.FragmentKoZnaZnaGameBinding
import com.example.igricaslagalica.model.Game
import com.example.igricaslagalica.model.KoZnaZna
import com.google.firebase.firestore.FirebaseFirestore

class KoZnaZnaGame : Fragment() {
    private var _binding: FragmentKoZnaZnaGameBinding? = null
    private lateinit var gameController: KoZnaZnaController
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var textViewScore: TextView
    private lateinit var questionTextView: TextView
    private lateinit var optionsRadioGroup: RadioGroup
    private lateinit var option1RadioButton: RadioButton
    private lateinit var option2RadioButton: RadioButton
    private lateinit var option3RadioButton: RadioButton
    private lateinit var option4RadioButton: RadioButton

    private lateinit var questionList: List<KoZnaZna>
    private var currentQuestionIndex: Int = 0
    private lateinit var timer: CountDownTimer
    private var totalScore: Int = 0
    private var remainingTime: Long = 0
    private var isMultiplayer: Boolean = false
    private var player1Answer: Int = -1
    private var player2Answer: Int = -1
    private var player1Time: Long = 0
    private var player2Time: Long = 0
    private var player1Score: Int = 0
    private var player2Score: Int = 0
    private var playerId1: String? = null
    private var playerId2: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gameController = KoZnaZnaController()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKoZnaZnaGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        questionTextView = binding.pitanjeView.findViewById(R.id.questionTextView)
        optionsRadioGroup =
            binding.pitanjeView.findViewById(R.id.optionsRadioGroup)
        option1RadioButton = binding.pitanjeView.findViewById(R.id.option1RadioButton)
        option2RadioButton = binding.pitanjeView.findViewById(R.id.option2RadioButton)
        option3RadioButton = binding.pitanjeView.findViewById(R.id.option3RadioButton)
        option4RadioButton = binding.pitanjeView.findViewById(R.id.option4RadioButton)

        textViewScore = binding.textViewScore
        val gameId = arguments?.getString("gameId")
        isMultiplayer = gameId != null
        if (!isMultiplayer) {

            questionList = generateQuestions(gameId)
            binding.nextButton.setOnClickListener {
                timer.cancel()
                checkAnswer()
                showNextQuestion()
            }
            binding.finishButton.setOnClickListener {
                sharedViewModel.setQuestionList(questionList)

                findNavController().navigate(R.id.action_koZnaZnaGame_to_spojnica_single)
            }
        } else {
            binding.finishButton.text = "Continue"
            questionList = generateQuestions(gameId)
            binding.nextButton.setOnClickListener {
                timer.cancel()
                checkAnswerMultiPlayer()
                showNextQuestionMultiPlayer(questionList)
            }
            binding.finishButton.setOnClickListener {
                val bundle = bundleOf("gameId" to gameId)

                findNavController().navigate(R.id.action_profileFragment_to_online_spojnice, bundle)
            }
        }

        if (gameId != null) {

            gameController.fetchGame(gameId,
                onSuccess = { game ->


                    Log.w(TAG, "Questions from fetch 1 ${game}")
                    playerId1 = game.player1
                    playerId2 = game.player2

                },
                onFailure = { exception ->

                }
            )

        }


    }


    private fun showNextQuestion() {
        if (currentQuestionIndex < 5) {
            if (currentQuestionIndex == 4)
                binding.nextButton.text = "Done"

            resetRadioButtons()

            val question = questionList[currentQuestionIndex]
            questionTextView.text = question.questionText
            option1RadioButton.text = question.options[0]
            option2RadioButton.text = question.options[1]
            option3RadioButton.text = question.options[2]
            option4RadioButton.text = question.options[3]

            currentQuestionIndex++
            startTimer()
        } else {

            endGame()
        }
    }


    private fun checkAnswer() {
        val currentQuestion = questionList[currentQuestionIndex - 1]
        val selectedAnswerIndex = getSelectedAnswerIndex()

        questionList[currentQuestionIndex - 1].player1Answered = selectedAnswerIndex
        questionList[currentQuestionIndex - 1].player1AnswerTime = remainingTime
        if (selectedAnswerIndex == currentQuestion.correctAnswer) {
            totalScore += 10
        } else {
            if (selectedAnswerIndex != -1)
                totalScore -= 5
        }
    }

    private fun getSelectedAnswerIndex(): Int {
        return when {
            option1RadioButton.isChecked -> 1
            option2RadioButton.isChecked -> 2
            option3RadioButton.isChecked -> 3
            option4RadioButton.isChecked -> 4
            else -> -1
        }
    }

    private fun resetRadioButtons() {
        optionsRadioGroup.clearCheck()
    }

    private fun generateQuestions(gameId: String?): List<KoZnaZna> {
        var randomQuestions = mutableListOf<KoZnaZna>()

        if (isMultiplayer) {
            if (gameId != null) {
                gameController.fetchGame(gameId,
                    onSuccess = { questions ->
                        questionList = questions.koZnaZnaQuestions
                        randomQuestions = questions.koZnaZnaQuestions.toMutableList()
                        showNextQuestionMultiPlayer(questions.koZnaZnaQuestions)
                    },
                    onFailure = { exception ->

                    }
                )
            }
        } else {
            val questions = StaticData.dajPitanjaKoZnaZna().shuffled()

            for (i in 0 until 5) {
                randomQuestions.add(questions[i])
            }

            questionList = randomQuestions
            showNextQuestion()
        }
        return randomQuestions
    }

    private fun showNextQuestionMultiPlayer(questionList: List<KoZnaZna>) {
        if (currentQuestionIndex < questionList.size) {
            if (currentQuestionIndex == questionList.size - 1)
                binding.nextButton.text = "Done"

            resetRadioButtons()

            val question = questionList[currentQuestionIndex]
            questionTextView.text = question.questionText
            option1RadioButton.text = question.options[0]
            option2RadioButton.text = question.options[1]
            option3RadioButton.text = question.options[2]
            option4RadioButton.text = question.options[3]

            currentQuestionIndex++
            startTimer()
        } else {
            checkAndShowWinner()
            endGame()
        }
    }

    private fun checkAnswerMultiPlayer() {
        val currentQuestion = questionList[currentQuestionIndex - 1]
        val selectedAnswerIndex = getSelectedAnswerIndex()


        val sharedPreferences = activity?.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val currentPlayerId = sharedPreferences?.getString("currentPlayerId", null)
        var answerTimePlayerOne: Long = 0
        var answerTimePlayerTwo: Long = 0
        Log.w(TAG, "test $currentPlayerId")
        if (currentPlayerId == playerId1) {
            player1Answer = selectedAnswerIndex
            answerTimePlayerOne = 5000 - remainingTime

        } else if (currentPlayerId == playerId2) {

            Log.w(TAG, "test player klik $currentPlayerId")

            player2Answer = selectedAnswerIndex
            answerTimePlayerTwo = 5000 - remainingTime

        }

        if (selectedAnswerIndex == currentQuestion.correctAnswer) {
            totalScore += 10
        } else {
            if (selectedAnswerIndex != -1) {
                totalScore -= 5
            }
        }
        if (isMultiplayer) {
            val gameId = arguments?.getString("gameId")
            if (gameId != null) {

                val questionIndex = currentQuestionIndex - 1

                if (currentPlayerId == playerId1) {
                    player1Answer = selectedAnswerIndex
                    answerTimePlayerOne = 5000 - remainingTime
                    gameController.saveAnswerForPlayer1(
                        gameId,
                        questionIndex,
                        answerTimePlayerOne,
                        player1Answer
                    )
                } else if (currentPlayerId == playerId2) {

                    player2Answer = selectedAnswerIndex
                    answerTimePlayerTwo = 5000 - remainingTime
                    gameController.saveAnswerForPlayer2(
                        gameId,
                        questionIndex,
                        player2Answer,
                        answerTimePlayerTwo
                    )

                }


            }
        }
    }

    private fun checkAndShowWinner() {
        val gameId = arguments?.getString("gameId")
        if (gameId != null) {
            val gameRef = FirebaseFirestore.getInstance().collection("games").document(gameId)

            gameRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {

                        val game = documentSnapshot.toObject(Game::class.java)
                        val questionList = game?.koZnaZnaQuestions
                        questionList?.let {
                            var player1Score = 0
                            var player2Score = 0

                            for (element in questionList) {
                                val player1Answer = element.player1Answered
                                val player2Answer = element.player2Answered
                                player1Time = element.player1AnswerTime
                                player2Time = element.player2AnswerTime

                                if (player1Answer == element.correctAnswer && player2Answer == element.correctAnswer) {

                                    if (player1Time < player2Time) {

                                        player1Score += 10
                                    } else if (player2Time < player1Time) {

                                        player2Score += 10
                                    } else {

                                        player1Score += 10
                                        player2Score += 10
                                    }
                                } else if (player1Answer == element.correctAnswer) {

                                    player1Score += 10
                                    player2Score -= 5

                                } else if (player2Answer == element.correctAnswer) {

                                    player2Score += 10
                                    player1Score -= 5
                                }

                            }

                            Log.d(TAG, "questions from Player 1 Score: $player1Score")
                            Log.d(TAG, "questions from Player 2 Score: $player2Score")
                            textViewScore.text =
                                "Player1 score: $player1Score \n Player2 score: $player2Score"

                            if (gameId != null) {
                                gameController.saveResultToDatabase(
                                    gameId,
                                    player1Score,
                                    player2Score
                                )
                            }

                        }
                    }
                }
                .addOnFailureListener { exception ->

                }
        }

    }

    private fun startTimer() {
        timer = object : CountDownTimer(5000, 10) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                val secondsLeft = millisUntilFinished / 1000
                val millisecondsLeft = millisUntilFinished % 1000
                binding.timerTextView.text =
                    String.format("%2d s: %s ms", secondsLeft, millisecondsLeft)
            }

            override fun onFinish() {
                remainingTime = 0
                binding.nextButton.performClick()
            }
        }
        timer.start()
    }

    private fun endGame() {
        timer.cancel()
        binding.pitanjeView.visibility = View.GONE
        binding.textViewScore.visibility = View.VISIBLE
        binding.nextButton.visibility = View.GONE
        binding.finishButton.visibility = View.VISIBLE
        binding.timerTextView.visibility = View.GONE
        if (isMultiplayer) {

        } else {
            updateScore()
        }

    }

    private fun updateScore() {
        textViewScore.text = "Ukupan skor je: $totalScore"
    }

}