package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.fragments.User
import com.critetiontech.ctvitalio.databinding.ItemPlayerBinding

class LeaderboardAdapter(   private val users: List<User>
) : RecyclerView.Adapter<LeaderboardAdapter.PlayerViewHolder>() {

    inner class PlayerViewHolder(val binding: ItemPlayerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding = ItemPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val user = users[position]
        with(holder.binding) {
            rankText.text = user.rank.toString()
            nameText.text = user.name
            gemText.text = user.gems.toString()

            // Highlight current user (e.g., Abhay Sharma with rank 4)
            if (user.rank == 4) {
                itemRoot.setBackgroundResource(R.drawable.bg_current_user)
            } else {
                itemRoot.setBackgroundResource(R.drawable.bg_item_player)
            }

            // Optional: load profile image from resource or URL if needed
            // profileImage.setImageResource(...)
        }
    }

    override fun getItemCount() = users.size
}