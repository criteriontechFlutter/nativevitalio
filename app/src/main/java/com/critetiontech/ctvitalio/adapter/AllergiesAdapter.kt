package com.critetiontech.ctvitalio.adapter

import DateUtils.toCamelCase
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.model.AllergyHistoryItem
import com.critetiontech.ctvitalio.databinding.ItemAllergyCardBinding


class AllergiesAdapter : ListAdapter<AllergyHistoryItem, AllergiesAdapter.ViewHolder>(DiffCallback()) {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    interface SelectionListener {
        fun onSelectionChanged(selectedCount: Int)
    }

    var selectionListener: SelectionListener? = null

    inner class ViewHolder(val binding: ItemAllergyCardBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    selectPosition(pos)
                }
            }
        }
    }

    private fun selectPosition(position: Int) {
        if (selectedPosition != position) {
            val previousPosition = selectedPosition
            selectedPosition = position
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            selectionListener?.onSelectionChanged(1)
        } else {
            // If clicked again on the same item, deselect it (optional)
            val previousPosition = selectedPosition
            selectedPosition = RecyclerView.NO_POSITION
            notifyItemChanged(previousPosition)
            selectionListener?.onSelectionChanged(0)
        }
    }

    fun clearSelection() {
        val previousPosition = selectedPosition
        selectedPosition = RecyclerView.NO_POSITION
        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition)
        }
        selectionListener?.onSelectionChanged(0)
    }

    fun getSelectedItem(): AllergyHistoryItem? {
        return if (selectedPosition != RecyclerView.NO_POSITION) getItem(selectedPosition) else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAllergyCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.categoryText.isVisible = !item.category.isNullOrBlank()
        holder.binding.categoryText.text = item.category

        holder.binding.substanceText.text = item.substance?.let { toCamelCase(it) }
        holder.binding.remarkText.text = item.remark?.let { toCamelCase(it) }
        holder.binding.severityText.text = item.severityLevel?.let { toCamelCase(it) }

        val severityColor = when (item.severityLevel?.lowercase()) {
            "mild" -> Color.parseColor("#FFA500")
            "moderate" -> Color.parseColor("#FF5722")
            "severe" -> Color.parseColor("#FF0000")
            else -> Color.DKGRAY
        }

        holder.binding.severityText.setTextColor(severityColor)

        // Highlight selected item background
        holder.binding.root.setBackgroundColor(
            if (position == selectedPosition) Color.LTGRAY else Color.WHITE
        )
    }

    class DiffCallback : DiffUtil.ItemCallback<AllergyHistoryItem>() {
        override fun areItemsTheSame(oldItem: AllergyHistoryItem, newItem: AllergyHistoryItem): Boolean {
            return oldItem.rowId != null && oldItem.rowId == newItem.rowId
        }

        override fun areContentsTheSame(oldItem: AllergyHistoryItem, newItem: AllergyHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}