package com.example.igricaslagalica.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.igricaslagalica.R
import com.example.igricaslagalica.model.Player

class RangListAdapter(private var friendsList: List<Player>) :
    RecyclerView.Adapter<RangListAdapter.FriendsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.prijatelj_item, parent, false)
        return FriendsViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val friend = friendsList[position]
        holder.bind(friend, position)
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    fun updateData(newFriendsList: List<Player>) {
        friendsList = newFriendsList
        notifyDataSetChanged()
    }

    inner class FriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewProfile: ImageView = itemView.findViewById(R.id.imageViewProfile)
        private val textViewUsername: TextView = itemView.findViewById(R.id.textViewUsername)
        private val textViewRank: TextView = itemView.findViewById(R.id.textViewRank)
        private val textViewStars: TextView = itemView.findViewById(R.id.textViewStars)
        private val buttonStartGame: Button = itemView.findViewById(R.id.buttonStartGame)

        fun bind(friend: Player, position: Int) {

            textViewUsername.text = friend.username
            textViewRank.text = "Rank:${(position + 1)}"
            textViewStars.text = friend.score.toString()


        }

    }

}