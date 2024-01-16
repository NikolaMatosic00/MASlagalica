package com.example.igricaslagalica.model

data class KoZnaZna(
    val id: Int? = null,
    val questionText: String = "",
    val options: List<String> = listOf(),
    val correctAnswer: Int = 0,
    var answerTime: Long? = null,
    var player1Answered: Int = -1,
    var player2Answered: Int = -1,
    var player1AnswerTime: Long = 0,
    var player2AnswerTime: Long = 0,

    )