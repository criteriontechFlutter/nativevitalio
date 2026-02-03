package com.critetiontech.ctvitalio.adapter

import LeaderboardItem
import PrefsManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.fragments.User
import com.critetiontech.ctvitalio.databinding.ItemPlayerBinding

class LeaderboardAdapter(
    private val users: List<LeaderboardItem>
) : RecyclerView.Adapter<LeaderboardAdapter.PlayerViewHolder>() {

    inner class PlayerViewHolder(val binding: ItemPlayerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding =
            ItemPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val user = users[position]

        with(holder.binding) {
            // Rank
            rankText.text = user.rank.toString()

            // Name
            nameText.text = user.empName

            // Points / Gems
            gemText.text = user.totalPoints.toString()

            // Highlight logged-in user (recommended way)
            if (user.empId == PrefsManager().getPatient()?.empId) {
                itemRoot.setBackgroundResource(R.drawable.bg_current_user)
            } else {
                itemRoot.setBackgroundResource(R.drawable.bg_item_player)
            }

            // Profile image
            if (user.imageURL != "0") {
                Glide.with(profileImage.context)
                    .load("http://182.156.200.177:5082/"+ user.imageURL.replace("\\", "/"))
                    .placeholder(R.drawable.person_badge)
                    .error(R.drawable.person_badge)
                    .circleCrop()
                    .into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.person_badge)
            }
        }
    }

    override fun getItemCount(): Int = users.size
}