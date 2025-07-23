package com.critetiontech.ctvitalio.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.JoinedChallengeItemBinding
import com.critetiontech.ctvitalio.model.JoinedChallenge
import java.util.Random

class JoinedChallengesAdapter (
    private val items: List<JoinedChallenge>,
) : RecyclerView.Adapter<JoinedChallengesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: JoinedChallengeItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = JoinedChallengeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding

        val randomColor = getRandomPastelColor()
        holder.itemView.backgroundTintList = ColorStateList.valueOf(randomColor)

        binding.textView3.text=item.title
        binding.textView4.text=item.description
        binding.scoreText.text= item.rewardPoints.toString()
        binding.participantsRecyclerView.adapter=ParticipantsAdapter(item.getPeopleJoinedList())
        binding.progressBar.progress=item.durationDays
        binding.progressBar.max=item.daysLeft
        binding.tvDaysAchieved.text=item.daysAchieved1





    }
    fun getRandomPastelColor(): Int {
        val rnd = Random()
        val red = 200 + rnd.nextInt(56)  // 200-255
        val green = 200 + rnd.nextInt(56)
        val blue = 200 + rnd.nextInt(56)
        val alpha = 255  // Same opacity as #F5F0F5

        return Color.argb(alpha, red, green, blue)
    }


    override fun getItemCount() = items.size

}