package com.example.igricaslagalica.view.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.igricaslagalica.R
import com.example.igricaslagalica.model.Connection

class QuestionsAdapter(
    private var questions: List<Connection>,
    private val onQuestionSelected: (Int) -> Unit
) : RecyclerView.Adapter<QuestionsAdapter.QuestionViewHolder>() {
    private var selectedPosition = -1
    var isInteractionEnabled = true

    inner class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionText: TextView = itemView.findViewById(R.id.questionText)

        init {
            itemView.setOnClickListener {
                if (isInteractionEnabled) {
                    onQuestionSelected(adapterPosition)
                    selectedPosition = adapterPosition
                    notifyDataSetChanged()
                }
            }
        }

        fun bind(question: Connection, position: Int) {

            questionText.text = question.question


            if (selectedPosition == position) {
                itemView.setBackgroundColor(Color.LTGRAY)
            } else {
                itemView.setBackgroundColor(Color.WHITE)
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.question_item, parent, false)
        return QuestionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questions[position]
        holder.bind(question, position)
    }

    fun updateData(newQuestions: List<Connection>) {
        this.questions = newQuestions
        notifyDataSetChanged()
    }

}
