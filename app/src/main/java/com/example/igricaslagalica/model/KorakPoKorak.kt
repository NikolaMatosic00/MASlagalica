package com.example.igricaslagalica.model

data class KorakPoKorak(
    val id: Int = 0,
    val odgovor: String = "",
    val pojmovi: List<String> = mutableListOf(),
    var bodovi: Int = 0,
    var assignedToPlayer: String = ""
)