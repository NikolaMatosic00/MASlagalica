package com.example.igricaslagalica.view.multiplayer

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.R
import com.example.igricaslagalica.SharedViewModel
import com.example.igricaslagalica.controller.FirebaseGameController
import com.example.igricaslagalica.controller.KorakPoKorakController
import com.example.igricaslagalica.databinding.FragmentKorakPoKorakBinding
import com.example.igricaslagalica.model.Game
import com.example.igricaslagalica.model.KorakPoKorak
import com.google.firebase.auth.FirebaseAuth

class KorakPoKorakMulti : Fragment() {
    private var _binding: FragmentKorakPoKorakBinding? = null
    private lateinit var firebaseGameController: FirebaseGameController
    private lateinit var korakPoKorakController: KorakPoKorakController
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var currentGame: Game = Game()

    private lateinit var timer: CountDownTimer
    private var remainingTime: Long = 0
    private var brojPojma = 0
    private lateinit var pitanje: KorakPoKorak
    private var pojmoviTextView = ArrayList<TextView>()
    private var gameId: String = ""
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    private var hasStartedGame: Boolean = false
    private var hasSecondRoundStarted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseGameController = FirebaseGameController()
        korakPoKorakController = KorakPoKorakController()
        pitanje = KorakPoKorak()
        startTimer()
        timer.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKorakPoKorakBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gameId = arguments?.getString("gameId")

        if (gameId != null) {
            firebaseGameController.listenForGameChanges(gameId) { updatedGame ->
                if (updatedGame != null && currentUserId != null) {
                    handleUI(updatedGame)
                    currentGame = updatedGame
                } else {


                }
            }
        }

        binding.proveriButton.setOnClickListener {
            handleAnswer()
            if (binding.proveriButton.text == "Next game") {


                if (gameId != null) {
                    korakPoKorakController.updateGameField(gameId, "currentRound", 1) { success ->
                        if (success) {

                        }

                    }
                    korakPoKorakController.updateGameField(gameId, "player1Done", false) {}
                    korakPoKorakController.updateGameField(gameId, "player2Done", false) {}

                }
                val bundle = bundleOf("gameId" to gameId)
                findNavController().navigate(
                    R.id.action_korakPoKorakMulti_to_mojBrojMulti,
                    bundle
                )
            }
            if (binding.proveriButton.text == "Next round") {
                switchTurnAndCheckGameEnd()
            }


        }

    }

    private fun handleUI(game: Game) {
        gameId = game.id.toString()
        if (!hasStartedGame) {
            hasStartedGame = true
            handleUIGame()
            if (game.korakPoKorakQuestions.isEmpty()) {
                generateQuestions(game)
            } else {
                pitanje = game.korakPoKorakQuestions[0]
                prikaziPojam()
            }

        }
        if (currentUserId != null && hasStartedGame) {
            handlePlayerTurn(game, currentUserId)
        }
        if (game.opponentAnswer.equals(pitanje.odgovor, ignoreCase = true)) {
            setAndShowCorrectAnswer()
            brojPojma = 0
        }
        if (game.currentRound == 2 && !hasSecondRoundStarted) {
            binding.proveriButton.text = "Provjeri"
            resetujPojam()
            pitanje = game.korakPoKorakQuestions[1]
            prikaziPojam()
            hasSecondRoundStarted = true

        }
        if (currentGame.currentRound >= 3) {
            binding.tacanOdgovorTextView.text = pitanje.odgovor
            binding.proveriButton.visibility = View.VISIBLE
        }


    }

    private fun handleAnswer(): Boolean {
        var odgovor = binding.odgovorEditText.text.toString()
        odgovor = odgovor.trim()

        if (odgovor.equals(pitanje.odgovor, ignoreCase = true)) {

            binding.proveriButton.text = "Next round"

            var brojBodova = 20 - ((brojPojma - 1) * 2)
            pitanje.bodovi = brojBodova
            binding.timerTextView.text = "$brojBodova bodova"
            synchronizeAnswer(pitanje.odgovor)
            binding.odgovorEditText.setText("")
            if (currentGame.player1 == currentUserId) {
                brojBodova += currentGame.player1Score
                Log.w("bodovi", "bodovi su tu $brojBodova nakon")
                korakPoKorakController.saveResultPlayer1(gameId, brojBodova)
            } else {
                brojBodova += currentGame.player2Score
                korakPoKorakController.saveResultPlayer2(gameId, brojBodova)
            }

            return true
        } else
            binding.odgovorEditText.setText("")
        synchronizeAnswer(odgovor)
        return false
    }

    private fun synchronizeAnswer(answer: String) {

        korakPoKorakController.updateGameField(gameId, "opponentAnswer", answer) { success ->
            if (success) {

            } else {

            }
        }
    }

    private fun handlePlayerTurn(game: Game, currentPlayerId: String) {
        if (game.currentTurn == currentPlayerId) {

            enableTurnForCurrentPlayer(game, currentPlayerId)

        } else {

            disableTurnForCurrentPlayer()
        }
    }

    private fun enableTurnForCurrentPlayer(game: Game, currentPlayerId: String) {
        binding.proveriButton.visibility = View.VISIBLE
        binding.odgovorEditText.visibility = View.VISIBLE
        binding.odgovorEditText.isClickable = true
        binding.odgovorEditText.isEnabled = true

    }

    private fun disableTurnForCurrentPlayer() {

        binding.proveriButton.visibility = View.GONE
        binding.odgovorEditText.isClickable = false
        binding.odgovorEditText.isEnabled = false
        if (binding.proveriButton.text == "Next game") {
            binding.proveriButton.visibility = View.VISIBLE
        }


    }

    private fun switchTurnAndCheckGameEnd() {
        korakPoKorakController.switchTurn(currentGame, currentGame.currentTurn) { success ->
            if (success) {
                endRound()
                handlePlayerTurn(currentGame, currentGame.currentTurn)

            } else {

            }
        }


    }

    private fun handleUIGame() {
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam1TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam2TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam3TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam4TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam5TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam6TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam7TextView))
    }

    private fun generateQuestions(game: Game) {

        korakPoKorakController.generateNewQuestions(game) { newQuestions ->
            game.korakPoKorakQuestions = newQuestions
            pitanje = newQuestions[0]
            game.id?.let { gameId ->
                korakPoKorakController.updateGameField(
                    gameId,
                    "korakPoKorakQuestions",
                    game.korakPoKorakQuestions
                ) { success ->
                    if (success) {
                        prikaziPojam()
                    }

                }
            }
        }

    }

    private fun startTimer() {
        timer = object : CountDownTimer(10000, 10) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                val secondsLeft = millisUntilFinished / 1000
                val millisecondsLeft = millisUntilFinished % 1000
                binding.timerTextView.text =
                    String.format("%2d s: %s ms", secondsLeft, millisecondsLeft)
            }

            override fun onFinish() {
                prikaziPojam()
            }
        }
        timer.start()
    }

    private fun prikaziPojam() {
        Log.w("tes", "broj $brojPojma")
        if (brojPojma < 7) {
            startTimer()
            pojmoviTextView[brojPojma].text = pitanje.pojmovi[brojPojma]
            brojPojma++
        } else
            endGame()


    }

    private fun resetujPojam() {
        timer.cancel()
        for (i in 0 until pojmoviTextView.size) {
            pojmoviTextView[i].text = "Pojam ${i + 1}"
        }
        binding.tacanOdgovorCardView.visibility = View.GONE
    }

    private fun endGame() {
        if (currentGame.currentRound == 1) {
            binding.proveriButton.text = "Next round"
        } else {
            binding.proveriButton.text = "Next game"

        }
    }

    private fun setAndShowCorrectAnswer() {

        binding.odgovorEditText.visibility = View.VISIBLE
        binding.tacanOdgovorCardView.visibility = View.VISIBLE
        binding.tacanOdgovorTextView.text = pitanje.odgovor
        endGame()
    }

    private fun endRound() {
        endGame()
        currentGame.currentRound++
        korakPoKorakController.updateGameField(
            gameId,
            "currentRound",
            currentGame.currentRound
        ) { success ->
            if (success) {

            }
        }
    }
}