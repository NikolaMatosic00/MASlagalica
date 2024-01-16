package com.example.igricaslagalica

import androidx.lifecycle.ViewModel
import com.example.igricaslagalica.model.KoZnaZna

class SharedViewModel : ViewModel() {
    private var trenutniBodovi = 0
    private var igricaBodovi = 0
    private lateinit var koZnaZnaQuestionList: MutableList<KoZnaZna>

    fun setQuestionList(questions: List<KoZnaZna>) {
        koZnaZnaQuestionList = questions as MutableList<KoZnaZna>
    }

    fun getQuestionList(): MutableList<KoZnaZna> {
        return koZnaZnaQuestionList
    }


}