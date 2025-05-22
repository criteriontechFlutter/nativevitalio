package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.model.BloodGroup
import com.critetiontech.ctvitalio.databinding.BloodGroupItemBinding

class BloodGroupAdapter(
    private val items: List<BloodGroup>,
    private val selected: BloodGroup?,
    private val onSelected: (BloodGroup) -> Unit
) : RecyclerView.Adapter<BloodGroupAdapter.BloodGroupViewHolder>() {

    private var selectedPosition = items.indexOfFirst { it.groupName == selected?.groupName }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BloodGroupViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BloodGroupItemBinding.inflate(inflater, parent, false)
        return BloodGroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BloodGroupViewHolder, position: Int) {
        val item = items[position]
        val isSelected = position == selectedPosition
        holder.bind(item.groupName, isSelected)

        holder.itemView.setOnClickListener {
            val currentPos = holder.adapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                val previous = selectedPosition
                selectedPosition = currentPos
                notifyItemChanged(previous)
                notifyItemChanged(selectedPosition)
                onSelected(items[currentPos])
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class BloodGroupViewHolder(private val binding:  BloodGroupItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(itemText: String, isSelected: Boolean) {
            binding.item = itemText
            binding.isSelected = isSelected
            binding.executePendingBindings()
        }
    }
}