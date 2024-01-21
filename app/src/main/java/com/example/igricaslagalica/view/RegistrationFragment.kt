package com.example.igricaslagalica.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.igricaslagalica.R
import com.example.igricaslagalica.controller.auth.AuthListener
import com.example.igricaslagalica.controller.auth.FirebaseAuthController
import com.example.igricaslagalica.databinding.FragmentRegistrationBinding
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val authController: FirebaseAuthController


    private val binding get() = _binding!!

    init {
        val db = FirebaseFirestore.getInstance()
        authController = FirebaseAuthController(db)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRegister.setOnClickListener {
            registerUser()

        }
    }

    private fun registerUser() {
        val email = binding.editTextEmail.text.toString()
        val username = binding.editTextUsername.text.toString()
        val password = binding.editTextPassword.text.toString()
        val confirmPassword = binding.editTextConfirmPassword.text.toString()
        if (email.isNullOrEmpty() || username.isNullOrEmpty() || password.isNullOrEmpty() || confirmPassword.isNullOrEmpty()) {
            Toast.makeText(activity, "All inputs must be entered", Toast.LENGTH_SHORT).show()
            return
        }
        if (password == confirmPassword) {

            authController.signUp(email, password, username, object : AuthListener {
                override fun onAuthSuccess() {
                    findNavController().navigate(R.id.action_registrationFragment_to_loginFragment)
                }

                override fun onAuthFailed(message: String) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}