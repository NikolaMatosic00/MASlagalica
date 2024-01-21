package com.example.igricaslagalica.view

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.igricaslagalica.R
import com.example.igricaslagalica.controller.FirebaseGameController
import com.example.igricaslagalica.controller.auth.FirebaseAuthController
import com.example.igricaslagalica.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val authController: FirebaseAuthController
    private val gameController: FirebaseGameController
    private lateinit var content: ActivityResultLauncher<String>


    private val binding get() = _binding!!

    init {
        val db = FirebaseFirestore.getInstance()
        authController = FirebaseAuthController(db)
        gameController = FirebaseGameController()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        content =
            registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri: Uri? ->

                val user = FirebaseAuth.getInstance().currentUser
                val storageReference =
                    FirebaseStorage.getInstance().getReference("profilepics/${user?.uid}")

                imageUri?.let {
                    storageReference.putFile(it).addOnSuccessListener {
                        storageReference.downloadUrl.addOnSuccessListener { uri ->
                            val sharedPreferences =
                                activity?.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
                            sharedPreferences?.edit()?.putString("profilePicUrl", uri.toString())
                                ?.apply()
                            Log.d("ProfileFragment", "Download URL: $uri")


                            Glide.with(this)
                                .load(uri)
                                .into(binding.imageViewProfile)
                        }.addOnFailureListener { exception ->

                            Log.e("ProfileFragment", "Could not get download URL: $exception")

                        }
                    }.addOnFailureListener { exception ->

                        Log.e("ProfileFragment", "Could not upload image: $exception")
                    }

                }

            }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        binding.textViewEmail.text = user?.email
        binding.textViewUsername.text = user?.displayName

        binding.imageViewProfile.setOnClickListener {
            content.launch("image/*")

        }

        val sharedPreferences = activity?.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val profilePicUrl = sharedPreferences?.getString("profilePicUrl", null)
        if (profilePicUrl != null) {
            Glide.with(this)
                .load(profilePicUrl)
                .into(binding.imageViewProfile)
        }

        binding.buttonPlayOnline.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            user?.let {
                onPlayOnlineButtonClicked(it.uid)
            }
        }
        binding.buttonFriends.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_friendsFragment)
        }

        binding.buttonLogout.setOnClickListener {
            authController.signOut()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
        binding.buttonRangList.setOnClickListener {

            findNavController().navigate(R.id.action_profileFragment_to_score_list)
        }




        if (user != null) {
            authController.loadPlayer(user.uid) { player ->

                binding.textViewTokeni.text = player.tokens.toString()
            }
        }


    }

    private fun onPlayOnlineButtonClicked(playerId: String) {
        Log.d("onPlayOnline", "Function called with playerId: $playerId")
        val sharedPreferences = activity?.getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        sharedPreferences?.edit()?.putString("currentPlayerId", playerId)?.apply()

        gameController.getWaitingGame(playerId) { game ->
            Log.d("getWaitingGame", "Callback is being called with game: $game")
            if (game != null) {
                Log.d("getWaitingGame", "Game is not null, attempting to join")
                game.id?.let {
                    gameController.joinGame(it, playerId) { success ->
                        if (success) {
                            Log.d("joinGame", "Successfully joined the game")
                            Toast.makeText(context, "Joined to $it", Toast.LENGTH_LONG).show()
                            val bundle = bundleOf("gameId" to it)


                            findNavController().navigate(R.id.koZnaZnaGameMulti, bundle)
                        } else {
                            Log.d("joinGame", "Failed to join the game")
                            Toast.makeText(context, "Failed to join the game", Toast.LENGTH_LONG)
                                .show()

                        }
                    }
                }
            } else {
                Log.d("getWaitingGame", "Game is null, attempting to start a new game")
                gameController.startGame(playerId) { success, gameId ->
                    if (success) {


                        Toast.makeText(context, "Started $gameId", Toast.LENGTH_LONG).show()

                        val bundle = bundleOf("gameId" to gameId)
                        findNavController().navigate(R.id.koZnaZnaGameMulti, bundle)

                    } else {
                        Log.d("startGame", "Failed to start a new game")
                        Toast.makeText(context, "Failed to start the game", Toast.LENGTH_LONG)
                            .show()

                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}