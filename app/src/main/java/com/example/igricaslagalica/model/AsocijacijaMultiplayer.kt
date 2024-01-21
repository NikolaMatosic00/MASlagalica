package com.example.igricaslagalica.model

data class AsocijacijaMultiplayer(
    val asocijacijaList: List<String>,
    val asocijacijaListOne: List<String>,
    val asocijacijaTwo: List<String>,
    val asocijacijaThree: List<String>,
    val asocijacijaKonacnoResenje: String,
    var answeredBy: String? = null,
    var assignedToPlayer: String? = null
) {

    constructor() : this(
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList(),
        "",
        null
    )
}
