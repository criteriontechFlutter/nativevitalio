package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.ItemProgressCardBinding

data class ProgressCard(val emoji: String, val title: String, val subtitle: String)

class ProgressCardAdapter(private val items: List<ProgressCard>) :
    RecyclerView.Adapter<ProgressCardAdapter.CardViewHolder>() {

    inner class CardViewHolder(val binding: ItemProgressCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProgressCard) {
            binding.emoji.text = item.emoji
            binding.title.text = item.title
            binding.subtitle.text = item.subtitle
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemProgressCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}