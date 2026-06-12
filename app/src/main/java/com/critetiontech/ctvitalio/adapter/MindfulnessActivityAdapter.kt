package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.MindfulnessItemLayoutBinding
import com.critetiontech.ctvitalio.model.MindfulnessExercise

class MindfulnessActivityAdapter(
    private val onItemClick: (MindfulnessExercise) -> Unit = {}
) : ListAdapter<MindfulnessExercise, MindfulnessActivityAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = MindfulnessItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    class ViewHolder(private val binding: MindfulnessItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MindfulnessExercise, onItemClick: (MindfulnessExercise) -> Unit) {
            binding.protocolIcon.setImageResource(getIconFor(item))
            binding.protocolTitle.text = item.exerciseName
            binding.protocolTagDuration.text = "${item.title}  •  ${item.durationMinutes} mins"
            binding.protocolDescription.text = item.subTitle
            binding.root.setOnClickListener { onItemClick(item) }
        }

        private fun getIconFor(exercise: MindfulnessExercise): Int = when (exercise.exerciseName) {
            "5-4-3-2-1 Technique" -> R.drawable.grounding_icon_gradient
            "Color Scavenger Hunt" -> R.drawable.ic_color_scavenger
            "3x3 BINGO Card" -> R.drawable.ic_bingo_grid
            "Deep Breathing" -> R.drawable.deep_breathing
            "Shamanic Breathing" -> R.drawable.shamanic_breathing
            "Box Breathing" -> R.drawable.box_breathing
            "Criss-Cross Focus" -> R.drawable.criss_cross_focus
            "Figure 8 Flow" -> R.drawable.figure_8_flow
            "Clock Circle" -> R.drawable.clock_circle
            else -> when (exercise.exerciseType) {
                "Grounding" -> R.drawable.grounding_icon_gradient
                "Bingo" -> R.drawable.ic_bingo_grid
                "Breathing" -> R.drawable.deep_breathing
                "EyeMovement" -> R.drawable.criss_cross_focus
                else -> R.drawable.mindfullness
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<MindfulnessExercise>() {
        override fun areItemsTheSame(oldItem: MindfulnessExercise, newItem: MindfulnessExercise) =
            oldItem.exerciseId == newItem.exerciseId

        override fun areContentsTheSame(oldItem: MindfulnessExercise, newItem: MindfulnessExercise) =
            oldItem == newItem
    }
}