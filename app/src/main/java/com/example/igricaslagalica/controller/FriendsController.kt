package com.example.igricaslagalica.controller

import com.example.igricaslagalica.model.Friend
import com.example.igricaslagalica.model.Player
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FriendsController {
    val friendsList: MutableList<Friend> = mutableListOf()
    val db = FirebaseFirestore.getInstance()
    val playersCollection = db.collection("players")

    val currentPlayerId = FirebaseAuth.getInstance().currentUser?.uid

    fun addFriend(
        username: String,
        successCallback: () -> Unit,
        failureCallback: (Exception) -> Unit
    ) {
        playersCollection.whereEqualTo("username", username).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val friendDocument = querySnapshot.documents[0]
                    val friendId = friendDocument.id

                    currentPlayerId?.let { playerId ->
                        val playerRef = playersCollection.document(playerId)
                        playerRef.get()
                            .addOnSuccessListener { documentSnapshot ->
                                val playerData = documentSnapshot.toObject(Player::class.java)
                                val friendIds = playerData?.friends ?: emptyList()

                                if (!friendIds.contains(friendId)) {
                                    playerRef.update("friends", FieldValue.arrayUnion(friendId))
                                        .addOnSuccessListener {
                                            successCallback()
                                        }
                                        .addOnFailureListener { e ->

                                        }
                                } else {

                                }
                            }
                            .addOnFailureListener { e ->

                            }
                    }
                } else {

                }
            }
            .addOnFailureListener { e ->

            }
    }

    fun getFriends(friendsCallback: (List<Friend>) -> Unit) {
        currentPlayerId?.let { playerId ->
            playersCollection.document(playerId).get()
                .addOnSuccessListener { documentSnapshot ->
                    val playerData = documentSnapshot.toObject(Player::class.java)
                    val friendIds = playerData?.friends ?: emptyList()
                    val friendsList = mutableListOf<Friend>()

                    for (friendId in friendIds) {
                        val friendRef = playersCollection.document(friendId)
                        friendRef.get().addOnSuccessListener { friendDocumentSnapshot ->
                            val friend = friendDocumentSnapshot.toObject(Friend::class.java)
                            if (friend != null) {
                                friendsList.add(friend)
                            }

                            if (friendsList.size == friendIds.size) {
                                friendsCallback(friendsList)
                            }
                        }.addOnFailureListener { e ->

                            friendsCallback(emptyList())
                        }
                    }
                }
                .addOnFailureListener { e ->

                    friendsCallback(emptyList())
                }
        }
    }

}
