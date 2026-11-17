package com.critetiontech.vitalioCorporate.adapter

import com.critetiontech.vitalioCorporate.model.LeaderboardItem
import com.critetiontech.vitalioCorporate.utils.PrefsManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.critetiontech.vitalioCorporate.R
import com.critetiontech.vitalioCorporate.UI.fragments.User
import com.critetiontech.vitalioCorporate.databinding.ItemPlayerBinding
import com.critetiontech.vitalioCorporate.networking.RetrofitInstance
import java.util.Locale
import java.util.Locale.getDefault

class LeaderboardAdapter(
    private val users: List<LeaderboardItem>
) : RecyclerView.Adapter<LeaderboardAdapter.PlayerViewHolder>() {

    class PlayerViewHolder(val binding: ItemPlayerBinding) :
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
            if (user.empId.lowercase(getDefault()) == PrefsManager().getPatient()?.empId?.lowercase(getDefault())
            ) {
                itemRoot.setBackgroundResource(R.drawable.bg_current_user)
            } else {
                itemRoot.setBackgroundResource(R.drawable.bg_item_player)
            }

            // Profile image
            if (user.imageURL != "0") {
                Glide.with(profileImage.context)
                    .load(RetrofitInstance.StaggingbaseUrl.toString()+":5082/"+ user.imageURL.replace("\\", "/"))

                    .circleCrop().placeholder(R.drawable.person )
                    .error(R.drawable.person )
                    .into(profileImage)
            } else {
                profileImage.setImageResource(R.drawable.person)
            }
        }
    }

    override fun getItemCount(): Int = users.size
}