package com.criterion.nativevitalio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.criterion.nativevitalio.databinding.BloodGroupItemBinding

class BloodGroupAdapter(
    private val items: List<String>,
    private val onSelected: (String) -> Unit
) : RecyclerView.Adapter<BloodGroupAdapter.BloodViewHolder>() {

    private var selectedPos = RecyclerView.NO_POSITION

    inner class BloodViewHolder(val binding: BloodGroupItemBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String, isSelected: Boolean) {
            binding.item = item
            binding.isSelected = isSelected
            binding.root.isSelected = isSelected
            binding.root.setOnClickListener {
                val previous = selectedPos
                selectedPos = position
                notifyItemChanged(previous)
                notifyItemChanged(selectedPos)
                onSelected(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BloodViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BloodGroupItemBinding.inflate(inflater, parent, false)
        return BloodViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: BloodViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPos)
    }
}
