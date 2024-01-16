package com.example.igricaslagalica.view.signleplayer

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.R
import com.example.igricaslagalica.SharedViewModel
import com.example.igricaslagalica.databinding.FragmentMojBrojBinding
import net.objecthunter.exp4j.ExpressionBuilder
import kotlin.random.Random


class MojBroj : Fragment() {
    private var _binding: FragmentMojBrojBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

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
    private var gameId: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMojBrojBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        number1TextView = binding.numbersLayout.findViewById<TextView>(R.id.number1TextView)
        number2TextView = binding.numbersLayout.findViewById<TextView>(R.id.number2TextView)
        number3TextView = binding.numbersLayout.findViewById<TextView>(R.id.number3TextView)
        number4TextView = binding.numbersLayout.findViewById<TextView>(R.id.number4TextView)
        number5TextView = binding.numbersLayout.findViewById<TextView>(R.id.number5TextView)
        number6TextView = binding.numbersLayout.findViewById<TextView>(R.id.number6TextView)
        resultString = binding.resultTextView

        startTimer()
        gameId = arguments?.getString("gameId")

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
            if (binding.calculateAndFinishButton.text == "Finish")
                findNavController().navigate(R.id.action_mojBroj_to_singlePlayer)

            binding.calculateAndFinishButton.text = "Finish"
        }
    }

    private fun generateTrazeniBroj() {

        targetNumber = Random.nextInt(100, 1000)


        binding.targetNumberTextView.text = targetNumber.toString()
    }

    private fun generatePonudjeneBrojeve() {
        number1TextView.text = Random.nextInt(1, 10).toString()
        number2TextView.text = Random.nextInt(1, 10).toString()
        number3TextView.text = Random.nextInt(1, 10).toString()
        number4TextView.text = Random.nextInt(1, 10).toString()
        number5TextView.text = listOf(10, 15, 20).random().toString()
        number6TextView.text = listOf(25, 50, 75, 100).random().toString()
    }

    private fun startTimer() {
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
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
            resultString.text = rezultat.toString()
        } catch (e: Exception) {
            resultString.text = "0"
        }
    }

    private fun stopGame() {

        timer.cancel()
        binding.numbersTextView.visibility = View.GONE
        binding.numbersLayout.visibility = View.GONE
    }

}