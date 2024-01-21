package com.example.igricaslagalica.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.R
import com.example.igricaslagalica.SharedViewModel
import com.example.igricaslagalica.databinding.FragmentSinglePlayerBinding

class SinglePlayer : Fragment() {

    private var _binding: FragmentSinglePlayerBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSinglePlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonBack.setOnClickListener {
            findNavController().navigate(R.id.action_singlePlayer_to_loginFragment)
        }
        binding.buttonAsocijacije.setOnClickListener {
            findNavController().navigate(R.id.action_singlePlayer_to_asocijacijaGame)
        }
        binding.buttonSkocko.setOnClickListener {
            findNavController().navigate(R.id.action_singlePlayer_to_skockoGame)
        }
        binding.buttonKoZnaZna.setOnClickListener {
            findNavController().navigate(R.id.action_singlePlayer_to_koZnaZnaGame)
        }
        binding.buttonMojBroj.setOnClickListener {
            findNavController().navigate(R.id.action_singlePlayer_to_mojBroj)
        }
        binding.buttonKorakPoKorak.setOnClickListener {
            findNavController().navigate(R.id.action_singlePlayer_to_korakPoKorak)
        }
        binding.buttonSpojnice.setOnClickListener {

            findNavController().navigate(R.id.action_singlePlayer_to_spojnica_single)
        }
    }

}