package com.example.igricaslagalica.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.igricaslagalica.controller.FriendsController
import com.example.igricaslagalica.databinding.FragmentFriendsBinding
import com.example.igricaslagalica.view.adapter.FriendsAdapter


class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private lateinit var friendsController: FriendsController

    private val binding get() = _binding!!

    private lateinit var recyclerViewMyFriends: RecyclerView
    private lateinit var friendsAdapter: FriendsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        friendsController = FriendsController()

        recyclerViewMyFriends = binding.recyclerViewMyFriends
        friendsAdapter = FriendsAdapter(friendsController.friendsList)
        recyclerViewMyFriends.adapter = friendsAdapter
        recyclerViewMyFriends.layoutManager = LinearLayoutManager(requireContext())

        binding.buttonAddFriend.setOnClickListener {
            val trazeniUsername = binding.editTextSearch.text.toString()
            if (trazeniUsername.isNotEmpty()) {
                friendsController.addFriend(trazeniUsername,
                    {


                        friendsController.getFriends { friends ->
                            friendsAdapter.updateData(friends)
                            Toast.makeText(
                                context,
                                "You added new friend: $trazeniUsername",
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                    },
                    { e ->


                        Toast.makeText(
                            context,
                            "Error adding friend: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )

            }
        }

        friendsController.getFriends { friends ->

            friendsAdapter.updateData(friends)
        }
    }

}