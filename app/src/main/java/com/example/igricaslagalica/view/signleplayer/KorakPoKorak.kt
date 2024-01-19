package com.example.igricaslagalica.view.signleplayer

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.R
import com.example.igricaslagalica.SharedViewModel
import com.example.igricaslagalica.data.StaticData
import com.example.igricaslagalica.databinding.FragmentKorakPoKorakBinding
import com.example.igricaslagalica.model.KorakPoKorak

class KorakPoKorak : Fragment() {
    private var _binding: FragmentKorakPoKorakBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var timer: CountDownTimer
    private var remainingTime: Long = 0
    private var brojPojma = 0
    private lateinit var pitanje: KorakPoKorak
    private var pojmoviTextView = ArrayList<TextView>()
    private var gameId: String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKorakPoKorakBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pitanje = StaticData.dajPitanjeKorakPoKorak()[0]
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam1TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam2TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam3TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam4TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam5TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam6TextView))
        pojmoviTextView.add(binding.pojmoviView.findViewById(R.id.pojam7TextView))

        gameId = arguments?.getString("gameId").toString()

        startGame()

        binding.proveriButton.setOnClickListener {

            if (binding.proveriButton.text == "Finish")
                findNavController().navigate(R.id.action_singlePlayer_to_online_mojBroj)


            var odgovor = binding.odgovorEditText.text.toString()
            odgovor = odgovor.trim()

            if (odgovor.equals(pitanje.odgovor, ignoreCase = true)) {
                binding.proveriButton.text = "Finish"
                timer.cancel()
                val brojBodova = 20 - ((brojPojma - 1) * 2)
                pitanje.bodovi = brojBodova
                binding.timerTextView.text = "$brojBodova bodova"
                setAndShowCorrectAnswer()
            } else
                binding.odgovorEditText.setText("")
        }

    }

    private fun startGame() {
        prikaziPojam()
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
        if (brojPojma < 7) {
            startTimer()
            pojmoviTextView[brojPojma].text = pitanje.pojmovi[brojPojma]
            brojPojma++
        } else
            endGame()

    }

    private fun endGame() {
        binding.timerTextView.text = "0 bodova"
        setAndShowCorrectAnswer()
        binding.proveriButton.text = "Finish"
    }

    private fun setAndShowCorrectAnswer() {
        binding.odgovorEditText.visibility = View.GONE
        binding.tacanOdgovorCardView.visibility = View.VISIBLE
        binding.tacanOdgovorTextView.text = pitanje.odgovor
    }

}