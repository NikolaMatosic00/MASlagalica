package com.example.igricaslagalica.model

data class Round(
    val id: Int,
    val currentPlayer: String? = null,
    val connections: List<Connection> = listOf()
)
