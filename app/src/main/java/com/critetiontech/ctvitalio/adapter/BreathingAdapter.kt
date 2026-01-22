package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.MindfulnessItemLayoutBinding
import com.critetiontech.ctvitalio.model.BreathingProtocol

class BreathingAdapter : ListAdapter<BreathingProtocol, BreathingAdapter.BreathingViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreathingViewHolder {
        val binding = MindfulnessItemLayoutBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BreathingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BreathingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BreathingViewHolder(private val binding: MindfulnessItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(protocol: BreathingProtocol) {
            binding.apply {
                protocolIcon.setImageResource(protocol.icon)
                protocolTitle.text = protocol.title
                protocolDescription.text = protocol.description
                protocolDuration.text = protocol.duration

                val benefitText = protocol.benefits.joinToString("  •  ")
                protocolBenefits.text = benefitText
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<BreathingProtocol>() {
        override fun areItemsTheSame(oldItem: BreathingProtocol, newItem: BreathingProtocol) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BreathingProtocol, newItem: BreathingProtocol) =
            oldItem == newItem
    }
}