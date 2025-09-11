//package com.critetiontech.ctvitalio.adapter
//
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.ItemMoodBinding

class MoodAdapter(private val moods: List<MoodData>) :
    RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    inner class MoodViewHolder(val binding: ItemMoodBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(mood: MoodData) {
            binding.moodEmoji.setImageResource(mood.emojiRes)
            binding.moodTitle.text = mood.name
            binding.root.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val binding = ItemMoodBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moods[position])
    }

    override fun getItemCount(): Int = moods.size
}

data class MoodData(
    val name: String,
    val color: String,
    val emojiRes: Int,
    val textColor: String
)