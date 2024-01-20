package com.example.igricaslagalica.model

import com.google.firebase.Timestamp

data class Player(
    val id: String? = null,
    val email: String? = null,
    val username: String? = null,
    val friends: MutableList<String> = mutableListOf(),
    var tokens: Int = 0,
    var lastTokenTime: Timestamp = Timestamp.now(),
    var score: Int = 0
) {
    constructor(username: String, score: Int) : this(username = username, score = score, id = null)
}
