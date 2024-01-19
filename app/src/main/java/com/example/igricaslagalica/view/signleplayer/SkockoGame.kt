package com.example.igricaslagalica.view.signleplayer

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.R
import com.example.igricaslagalica.SharedViewModel
import com.example.igricaslagalica.databinding.FragmentSkockoGameBinding
import com.example.igricaslagalica.model.Skocko


class SkockoGame : Fragment() {
    private var _binding: FragmentSkockoGameBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var timer: CountDownTimer
    private lateinit var skockoList: List<Skocko>
    private var hintList = ArrayList<ImageView>()
    private var listaPolja = ArrayList<ImageView>()
    private var pokusaj = ArrayList<Skocko>()
    private var element = 0
    private var bodovi = 0
    var gameId: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSkockoGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        napuniListuPolja()
        napuniHintListu()
        startGame()

        binding.finishButton.setOnClickListener {

            findNavController().navigate(R.id.action_singlePlayer_to_online_korakpokorak)

        }

    }

    private fun napuniListuPolja() {
        listaPolja.add(binding.vrsta1.findViewById(R.id.kolona1Polje))
        listaPolja.add(binding.vrsta1.findViewById(R.id.kolona2Polje))
        listaPolja.add(binding.vrsta1.findViewById(R.id.kolona3Polje))
        listaPolja.add(binding.vrsta1.findViewById(R.id.kolona4Polje))
        listaPolja.add(binding.vrsta2.findViewById(R.id.kolona1Polje))
        listaPolja.add(binding.vrsta2.findViewById(R.id.kolona2Polje))
        listaPolja.add(binding.vrsta2.findViewById(R.id.kolona3Polje))
        listaPolja.add(binding.vrsta2.findViewById(R.id.kolona4Polje))
        listaPolja.add(binding.vrsta3.findViewById(R.id.kolona1Polje))
        listaPolja.add(binding.vrsta3.findViewById(R.id.kolona2Polje))
        listaPolja.add(binding.vrsta3.findViewById(R.id.kolona3Polje))
        listaPolja.add(binding.vrsta3.findViewById(R.id.kolona4Polje))
        listaPolja.add(binding.vrsta4.findViewById(R.id.kolona1Polje))
        listaPolja.add(binding.vrsta4.findViewById(R.id.kolona2Polje))
        listaPolja.add(binding.vrsta4.findViewById(R.id.kolona3Polje))
        listaPolja.add(binding.vrsta4.findViewById(R.id.kolona4Polje))
        listaPolja.add(binding.vrsta5.findViewById(R.id.kolona1Polje))
        listaPolja.add(binding.vrsta5.findViewById(R.id.kolona2Polje))
        listaPolja.add(binding.vrsta5.findViewById(R.id.kolona3Polje))
        listaPolja.add(binding.vrsta5.findViewById(R.id.kolona4Polje))
        listaPolja.add(binding.vrsta6.findViewById(R.id.kolona1Polje))
        listaPolja.add(binding.vrsta6.findViewById(R.id.kolona2Polje))
        listaPolja.add(binding.vrsta6.findViewById(R.id.kolona3Polje))
        listaPolja.add(binding.vrsta6.findViewById(R.id.kolona4Polje))
    }

    private fun napuniHintListu() {
        var hint = binding.vrsta1.findViewById<LinearLayout>(R.id.kolonaHint)
        hintList.add(hint.findViewById(R.id.image1))
        hintList.add(hint.findViewById(R.id.image2))
        hintList.add(hint.findViewById(R.id.image3))
        hintList.add(hint.findViewById(R.id.image4))
        hint = binding.vrsta2.findViewById(R.id.kolonaHint)
        hintList.add(hint.findViewById(R.id.image1))
        hintList.add(hint.findViewById(R.id.image2))
        hintList.add(hint.findViewById(R.id.image3))
        hintList.add(hint.findViewById(R.id.image4))
        hint = binding.vrsta3.findViewById(R.id.kolonaHint)
        hintList.add(hint.findViewById(R.id.image1))
        hintList.add(hint.findViewById(R.id.image2))
        hintList.add(hint.findViewById(R.id.image3))
        hintList.add(hint.findViewById(R.id.image4))
        hint = binding.vrsta4.findViewById(R.id.kolonaHint)
        hintList.add(hint.findViewById(R.id.image1))
        hintList.add(hint.findViewById(R.id.image2))
        hintList.add(hint.findViewById(R.id.image3))
        hintList.add(hint.findViewById(R.id.image4))
        hint = binding.vrsta5.findViewById(R.id.kolonaHint)
        hintList.add(hint.findViewById(R.id.image1))
        hintList.add(hint.findViewById(R.id.image2))
        hintList.add(hint.findViewById(R.id.image3))
        hintList.add(hint.findViewById(R.id.image4))
        hint = binding.vrsta6.findViewById(R.id.kolonaHint)
        hintList.add(hint.findViewById(R.id.image1))
        hintList.add(hint.findViewById(R.id.image2))
        hintList.add(hint.findViewById(R.id.image3))
        hintList.add(hint.findViewById(R.id.image4))

    }

    private fun startGame() {
        makeRandomCombination()
        startTimer()
        pokusaj()
    }

    private fun pokusaj() {
        binding.opcije.findViewById<ImageView>(R.id.skocko).setOnClickListener {
            listaPolja[element].setImageResource(R.drawable.skocko)
            pokusaj.add(Skocko.SKOCKO)
            element++
            if (element % 4 == 0)
                provjeriPokusaj()
        }
        binding.opcije.findViewById<ImageView>(R.id.kvadrat).setOnClickListener {
            listaPolja[element].setImageResource(R.drawable.kvadrat)
            pokusaj.add(Skocko.KVADRAT)
            element++
            if (element % 4 == 0)
                provjeriPokusaj()
        }
        binding.opcije.findViewById<ImageView>(R.id.krug).setOnClickListener {
            listaPolja[element].setImageResource(R.drawable.crveni_krug)
            pokusaj.add(Skocko.KRUG)
            element++
            if (element % 4 == 0)
                provjeriPokusaj()
        }
        binding.opcije.findViewById<ImageView>(R.id.srce).setOnClickListener {
            listaPolja[element].setImageResource(R.drawable.srce)
            pokusaj.add(Skocko.SRCE)
            element++
            if (element % 4 == 0)
                provjeriPokusaj()
        }
        binding.opcije.findViewById<ImageView>(R.id.trougao).setOnClickListener {
            listaPolja[element].setImageResource(R.drawable.trougao)
            pokusaj.add(Skocko.TROUGAO)
            element++
            if (element % 4 == 0)
                provjeriPokusaj()
        }
        binding.opcije.findViewById<ImageView>(R.id.zvezda).setOnClickListener {
            listaPolja[element].setImageResource(R.drawable.zvezda)
            pokusaj.add(Skocko.ZVEZDA)
            element++
            if (element % 4 == 0)
                provjeriPokusaj()
        }
    }

    private fun provjeriPokusaj() {
        if (pokusaj == skockoList) {
            bodovi = vratiBrojBodova()
            endGame()
        } else {
            if (element >= 24)
                endGame()

            var hintNo = element - 4
            for (i in 0 until vratiBrojTacnihMjesta()) {
                hintList[hintNo].setImageResource(R.drawable.crveni_krug)
                hintNo++
            }
            for (i in 0 until vratiBrojNaPogresnomMjestu()) {
                hintList[hintNo].setImageResource(R.drawable.zuti_krug)
                hintNo++
            }
            pokusaj = ArrayList()
        }
    }

    private fun vratiBrojTacnihMjesta(): Int {
        return pokusaj.zip(skockoList).count { (element1, element2) ->
            element1 == element2
        }
    }

    private fun vratiBrojNaPogresnomMjestu(): Int {
        val brojIstihElemenata = pokusaj.intersect(skockoList).sumOf { element ->
            minOf(pokusaj.count { it == element }, skockoList.count { it == element })
        }
        return brojIstihElemenata - vratiBrojTacnihMjesta()
    }

    private fun endGame() {
        timer.cancel()
        napuniTacanOdgovor()
        binding.skor.text = "Vasi bodovi u ovoj igri su : $bodovi"
        binding.pokusaji.visibility = View.GONE
        binding.tacanOdgovor.visibility = View.VISIBLE
        binding.opcije.visibility = View.GONE

    }

    private fun vratiImageResourceZaVrijednost(s: Skocko): Int {
        return when (s) {
            Skocko.SKOCKO -> R.drawable.skocko
            Skocko.ZVEZDA -> R.drawable.zvezda
            Skocko.TROUGAO -> R.drawable.trougao
            Skocko.SRCE -> R.drawable.srce
            Skocko.KRUG -> R.drawable.crveni_krug
            else -> R.drawable.kvadrat
        }
    }

    private fun vratiBrojBodova(): Int {
        return if (element <= 8)
            20
        else if (element <= 16)
            15
        else
            10
    }

    private fun napuniTacanOdgovor() {
        binding.tacanOdgovor.findViewById<ImageView>(R.id.kolona1Polje).setImageResource(
            vratiImageResourceZaVrijednost(
                skockoList[0]
            )
        )
        binding.tacanOdgovor.findViewById<ImageView>(R.id.kolona2Polje).setImageResource(
            vratiImageResourceZaVrijednost(
                skockoList[1]
            )
        )
        binding.tacanOdgovor.findViewById<ImageView>(R.id.kolona3Polje).setImageResource(
            vratiImageResourceZaVrijednost(
                skockoList[2]
            )
        )
        binding.tacanOdgovor.findViewById<ImageView>(R.id.kolona4Polje).setImageResource(
            vratiImageResourceZaVrijednost(
                skockoList[3]
            )
        )
    }

    private fun startTimer() {
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.timerTextView.text =
                    String.format("%2d s", secondsLeft)
            }

            override fun onFinish() {
                endGame()
            }
        }
        timer.start()
    }

    private fun makeRandomCombination() {
        skockoList = List(4) { Skocko.values().random() }
    }

}