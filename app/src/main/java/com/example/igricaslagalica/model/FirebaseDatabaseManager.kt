package com.example.igricaslagalica.model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class FirebaseDatabaseManager {
    private val database = FirebaseDatabase.getInstance()
    private val usersReference = database.getReference("users")

    fun storeUser(email: String, username: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            usersReference.child(userId).setValue(User(email, username))
        }
    }
}
