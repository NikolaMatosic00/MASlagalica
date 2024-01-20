package com.example.igricaslagalica.model

data class Connection(

    var question: String = "",
    var answer: String = "",
    var correct: Boolean = false,
    var answeredBy: String? = null,
    var answered: Boolean = false,
    var assignedToPlayer: String? = null

) {

    constructor() : this(
        "",
        "",
        false,
        null,
        false

    )
}

