package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.ItemBotMessageBinding
import com.critetiontech.ctvitalio.databinding.ItemBotWithOptionsBinding
import com.critetiontech.ctvitalio.databinding.ItemUserMessageBinding
import com.critetiontech.ctvitalio.model.BotMessage

class ChatBotAdapter(private val onOptionSelected: (String) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var messages = listOf<BotMessage>()

    fun submitList(newList: List<BotMessage>) {
        messages = newList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            messages[position].isUser -> 0
            messages[position].options != null -> 2
            else -> 1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            0 -> UserViewHolder(ItemUserMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            1 -> BotViewHolder(ItemBotMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> BotOptionsViewHolder(ItemBotWithOptionsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when(holder) {
            is UserViewHolder -> holder.bind(msg)
            is BotViewHolder -> holder.bind(msg)
            is BotOptionsViewHolder -> holder.bind(msg, onOptionSelected)
        }
    }

    class UserViewHolder(val binding: ItemUserMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: BotMessage) { binding.tvUserMsg.text = msg.message }
    }

    class BotViewHolder(val binding: ItemBotMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: BotMessage) { binding.tvBotMsg.text = msg.message }
    }

    class BotOptionsViewHolder(val binding: ItemBotWithOptionsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: BotMessage, onOptionSelected: (String) -> Unit) {
            binding.tvBotMsg.text = msg.message
            binding.chipGroup.removeAllViews()
            msg.options?.forEach { option ->
                val chip = com.google.android.material.chip.Chip(binding.root.context).apply {
                    text = option
                    isClickable = true
                    isCheckable = false
                    setOnClickListener { onOptionSelected(option) }
                }
                binding.chipGroup.addView(chip)
            }
        }
    }}