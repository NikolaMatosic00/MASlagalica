package com.example.igricaslagalica.view.multiplayer

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.R
import com.example.igricaslagalica.SharedViewModel
import com.example.igricaslagalica.controller.FirebaseGameController
import com.example.igricaslagalica.controller.MojBrojMultiController
import com.example.igricaslagalica.databinding.FragmentMojBrojBinding
import com.example.igricaslagalica.model.Game
import com.google.firebase.auth.FirebaseAuth
import net.objecthunter.exp4j.ExpressionBuilder
import java.lang.Math.sqrt
import java.util.*
import kotlin.random.Random


class MojBrojMulti : Fragment() {
    private var _binding: FragmentMojBrojBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var mojBrojController: MojBrojMultiController
    private lateinit var firebaseGameController: FirebaseGameController

    private lateinit var timer: CountDownTimer
    private var targetNumber: Int = 0
    private var daLiJeIgraPocela: Boolean = false
    private var matematickiIzraz: String = ""
    private var rezultat: Double = 0.0

    private lateinit var number1TextView: TextView
    private lateinit var number2TextView: TextView
    private lateinit var number3TextView: TextView
    private lateinit var number4TextView: TextView
    private lateinit var number5TextView: TextView
    private lateinit var number6TextView: TextView
    private lateinit var resultString: TextView
    private var gameId: String = ""
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private var currentGame: Game = Game()
    var offeredNumbers: MutableList<Int> = mutableListOf()
    private var hasStartedGame: Boolean = false

    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mojBrojController = MojBrojMultiController()
        firebaseGameController = FirebaseGameController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMojBrojBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameUI()
        gameId = arguments?.getString("gameId").toString()

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(
                sensorListener, sensorManager!!
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL
            )

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH


        if (gameId != null) {
            firebaseGameController.listenForGameChanges(gameId) { updatedGame ->
                if (updatedGame != null && currentUserId != null) {
                    handleUI(updatedGame)
                    currentGame = updatedGame
                } else {


                }
            }
        }

        binding.stopButton.setOnClickListener {
            if (!daLiJeIgraPocela)
                generateTrazeniBroj()
            if (daLiJeIgraPocela) {
                generatePonudjeneBrojeve()
                binding.stopButton.isEnabled = false
            }
            daLiJeIgraPocela = true
        }

        izvrsiMatematickeOperacije()
        binding.calculateAndFinishButton.setOnClickListener {
            izracunajDobijeniIzraz()
            stopGame()

            if (binding.calculateAndFinishButton.text == "Finish") {
                if (gameId != null) {


                    val bundle = bundleOf(
                        "player1Score" to currentGame.player1Score,
                        "player2Score" to currentGame.player2Score,
                        "idOne" to currentGame.player1,
                        "idTwo" to currentGame.player2
                    )

                    findNavController().navigate(R.id.endGameScore, bundle)
                }
            }


        }
    }

    private fun handleUI(game: Game) {
        if (!hasStartedGame) {
            hasStartedGame = true
        }
        if (currentUserId != null) {
            handlePlayerTurn(game, currentUserId)
        }
        targetNumber = game.targetNumber
        if (targetNumber != 0) {
            binding.targetNumberTextView.text = game.targetNumber.toString()
        }
        if (!game.offeredNumbers.isNullOrEmpty()) {
            offeredNumbers = game.offeredNumbers
            showOfferedNumbers()
        }
        if (game.player1AnswerBroj != 0.0) {
            if (currentGame.player1 == currentUserId) {
                resultString.text = game.player1AnswerBroj.toString()
            }
        }
        if (game.player2AnswerBroj != 0.0) {
            if (currentGame.player2 == currentUserId) {
                resultString.text = game.player2AnswerBroj.toString()
            }
        }
        if (game.isPlayer1Done && game.isPlayer2Done && game.currentRound == 1) {

            calculateScores()
            stopGame()
            endOfRound1()

        }
        if (game.currentRound == 2) {
            binding.calculateAndFinishButton.text = "Finish"
        }
    }

    private fun generateTrazeniBroj() {

        targetNumber = Random.nextInt(100, 1000)
        mojBrojController.updateGameField(gameId, "targetNumber", targetNumber) {}


        binding.targetNumberTextView.text = targetNumber.toString()
    }

    private fun generatePonudjeneBrojeve() {
        for (i in 1..4) {
            offeredNumbers.add(Random.nextInt(1, 10))
        }
        offeredNumbers.add(listOf(10, 15, 20).random())
        offeredNumbers.add(listOf(25, 50, 75, 100).random())

        mojBrojController.updateGameField(gameId, "offeredNumbers", offeredNumbers) {

        }
        showOfferedNumbers()
    }

    private fun showOfferedNumbers() {
        number1TextView.text = offeredNumbers[0].toString()
        number2TextView.text = offeredNumbers[1].toString()
        number3TextView.text = offeredNumbers[2].toString()
        number4TextView.text = offeredNumbers[3].toString()
        number5TextView.text = offeredNumbers[4].toString()
        number6TextView.text = offeredNumbers[5].toString()
    }

    var secondsLeft: Long = 0L
    private fun startTimer() {
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsLeft = millisUntilFinished / 1000
                if (secondsLeft < 55 && !daLiJeIgraPocela) {
                    binding.stopButton.performClick()
                    binding.stopButton.performClick()

                }
                binding.timerTextView.text =
                    String.format("%2d s", secondsLeft)
            }

            override fun onFinish() {
                binding.calculateAndFinishButton.performClick()
            }
        }
        timer.start()
    }

    private fun endOfRound1() {
        timer.cancel()
        var timerEnd = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.timerTextView.text =
                    String.format("New round will start in %2d s", secondsLeft)
            }

            override fun onFinish() {
                switchTurnAndCheckGameEnd()
                startTimer()
                resetForRound2()

            }
        }
        timerEnd.start()
    }

    private fun izvrsiMatematickeOperacije() {
        binding.numbersLayout.findViewById<CardView>(R.id.number1CardView).setOnClickListener {
            matematickiIzraz += number1TextView.text
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.number2CardView).setOnClickListener {
            matematickiIzraz += number2TextView.text
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.number3CardView).setOnClickListener {
            matematickiIzraz += number3TextView.text
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.number4CardView).setOnClickListener {
            matematickiIzraz += number4TextView.text
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.number5CardView).setOnClickListener {
            matematickiIzraz += number5TextView.text
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.number6CardView).setOnClickListener {
            matematickiIzraz += number6TextView.text
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.plusCardView).setOnClickListener {
            matematickiIzraz += "+"
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.minusCardView).setOnClickListener {
            matematickiIzraz += "-"
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.putaCardView).setOnClickListener {
            matematickiIzraz += "*"
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.otvorenaCardView).setOnClickListener {
            matematickiIzraz += "("
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.zatvorenaCardView).setOnClickListener {
            matematickiIzraz += ")"
            resultString.text = matematickiIzraz
        }
        binding.numbersLayout.findViewById<CardView>(R.id.podjeljenoCardView).setOnClickListener {
            matematickiIzraz += "/"
            resultString.text = matematickiIzraz
        }
    }

    private fun izracunajDobijeniIzraz() {
        val expressionString = resultString.text.toString()
        try {
            val expression = ExpressionBuilder(expressionString).build()
            rezultat = expression.evaluate()
            if (currentGame.player1 == currentUserId) {
                mojBrojController.updateGameField(gameId, "player1AnswerBroj", rezultat) {}
                mojBrojController.updateGameField(gameId, "player1Done", true) {}

            } else {
                mojBrojController.updateGameField(gameId, "player2AnswerBroj", rezultat) {}
                mojBrojController.updateGameField(gameId, "player2Done", true) {}

            }

            resultString.text = rezultat.toString()


        } catch (e: Exception) {
            resultString.text = "0"
        }
    }

    private fun calculateScores() {

        val difference = Math.abs(rezultat - targetNumber)
        var playerScore = 0
        var opponentScore = 0

        if (rezultat == targetNumber.toDouble()) {

            playerScore += 20
        } else if (playerHasRequiredNumber(rezultat)) {

            playerScore += 20
        } else if (opponentHasRequiredNumber(rezultat)) {

            opponentScore += 20
        } else if (rezultat != 0.0 && currentGame.player2AnswerBroj != 0.0 && rezultat == currentGame.player1AnswerBroj) {


            if (currentGame.currentTurn == currentUserId) {
                playerScore += 5
            } else {
                opponentScore += 5
            }
        } else {

            val points = calculatePointsForCloseness(
                currentGame.player1AnswerBroj,
                currentGame.player2AnswerBroj
            )
            playerScore += points
        }

        if (currentGame.player1 == currentUserId) {
            playerScore += currentGame.player1Score
            mojBrojController.saveResultPlayer1(gameId, playerScore)
        } else {
            opponentScore += currentGame.player2Score
            mojBrojController.saveResultPlayer2(gameId, opponentScore)
        }

    }

    private fun playerHasRequiredNumber(result: Double): Boolean {

        return result == currentGame.targetNumber.toDouble()
    }

    private fun opponentHasRequiredNumber(result: Double): Boolean {

        return result == currentGame.targetNumber.toDouble()
    }

    private fun calculatePointsForCloseness(playerResult: Double, opponentResult: Double): Int {

        val playerDifference = Math.abs(playerResult - currentGame.targetNumber)
        val opponentDifference = Math.abs(opponentResult - currentGame.targetNumber)

        if (playerDifference < opponentDifference) {

            return 5
        } else if (opponentDifference < playerDifference) {

            return 5
        } else {

            return 0
        }
    }

    private fun resetForRound2() {
        binding.numbersTextView.visibility = View.VISIBLE
        binding.numbersLayout.visibility = View.VISIBLE
        targetNumber = 0
        binding.targetNumberTextView.text = "Trazeni broj"
        binding.resultTextView.text = "Rezultat"
        resultString.text = "Rezultat"
        matematickiIzraz = ""
        number1TextView.text = "Broj 1"
        number2TextView.text = "Broj 2"
        number3TextView.text = "Broj 3"
        number4TextView.text = "Broj 4"
        number5TextView.text = "Broj 5"
        number6TextView.text = "Broj 6"

    }

    private fun stopGame() {
        acceleration = 0f
        timer.cancel()
        binding.numbersTextView.visibility = View.GONE
        binding.numbersLayout.visibility = View.GONE
        if (currentGame.currentRound == 1) {
            binding.calculateAndFinishButton.text = "Next round"

        } else {
            binding.calculateAndFinishButton.text = "Finish"
        }

    }

    private fun gameUI() {
        number1TextView = binding.numbersLayout.findViewById<TextView>(R.id.number1TextView)
        number2TextView = binding.numbersLayout.findViewById<TextView>(R.id.number2TextView)
        number3TextView = binding.numbersLayout.findViewById<TextView>(R.id.number3TextView)
        number4TextView = binding.numbersLayout.findViewById<TextView>(R.id.number4TextView)
        number5TextView = binding.numbersLayout.findViewById<TextView>(R.id.number5TextView)
        number6TextView = binding.numbersLayout.findViewById<TextView>(R.id.number6TextView)
        resultString = binding.resultTextView

        startTimer()
    }

    private fun handlePlayerTurn(game: Game, currentPlayerId: String) {
        if (game.currentTurn == currentPlayerId) {

            enableTurnForCurrentPlayer(game, currentPlayerId)

        } else {

            disableTurnForCurrentPlayer()

        }
    }

    private fun enableTurnForCurrentPlayer(game: Game, currentPlayerId: String) {
        binding.stopButton.visibility = View.VISIBLE
        binding.stopButton.isEnabled = true
    }

    private fun disableTurnForCurrentPlayer() {
        binding.stopButton.isEnabled = false

    }

    private fun switchTurnAndCheckGameEnd() {
        mojBrojController.switchTurn(currentGame, currentGame.currentTurn) { success ->
            if (success) {
                endRound()
                handlePlayerTurn(currentGame, currentGame.currentTurn)

            } else {

            }
        }
    }

    private fun endRound() {

        val empty: MutableList<Int> = mutableListOf()
        if (currentGame.isPlayer1Done && currentGame.isPlayer2Done) {
            currentGame.currentRound++
            mojBrojController.updateGameField(
                gameId,
                "currentRound",
                currentGame.currentRound
            ) { success ->
                if (success) {

                    mojBrojController.updateGameField(gameId, "player1Done", false) {}
                    mojBrojController.updateGameField(gameId, "player2Done", false) {}
                    mojBrojController.updateGameField(gameId, "player1AnswerBroj", 0.0) {}
                    mojBrojController.updateGameField(gameId, "player2AnswerBroj", 0.0) {}
                    daLiJeIgraPocela = false

                }
            }
        }

    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {


            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration



            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta



            if (acceleration > 12) {
                binding.stopButton.performClick()
                showShakeToast(requireContext())
            }
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    override fun onResume() {
        sensorManager?.registerListener(
            sensorListener, sensorManager!!.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER
            ), SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(sensorListener)
        super.onPause()
    }

    fun showShakeToast(context: Context) {
        Toast.makeText(context, "You are using shake to get numbers", Toast.LENGTH_SHORT).show()
    }
}