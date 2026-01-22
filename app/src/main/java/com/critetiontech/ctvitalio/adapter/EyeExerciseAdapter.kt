package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.ItemEyeExerciseBinding
import com.critetiontech.ctvitalio.model.EyeExercise

class EyeExerciseAdapter : ListAdapter<EyeExercise, EyeExerciseAdapter.EyeExerciseViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EyeExerciseViewHolder {
        val binding = ItemEyeExerciseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EyeExerciseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EyeExerciseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EyeExerciseViewHolder(private val binding: ItemEyeExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(exercise: EyeExercise) {
            binding.apply {
                exerciseIcon.setImageResource(exercise.icon)
                exerciseTitle.text = exercise.title
                exerciseDescription.text = exercise.description
                exerciseDuration.text = exercise.duration
                exerciseReps.text = exercise.reps

                val benefitText = exercise.benefits.joinToString("  •  ")
                exerciseBenefits.text = benefitText
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<EyeExercise>() {
        override fun areItemsTheSame(oldItem: EyeExercise, newItem: EyeExercise) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: EyeExercise, newItem: EyeExercise) =
            oldItem == newItem
    }
}