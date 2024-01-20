package com.example.igricaslagalica.model

data class Friend(
    val id: String? = "",
    val username: String? = "",
    val profileImage: Int? = 0,
    val monthlyRank: String? = "",
    val stars: Int? = 0,
    val isActive: Boolean? = false,
    val isInGame: Boolean? = false
)