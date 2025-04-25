package com.criterion.nativevitalio.adapter

import DateUtils.toCamelCase
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.criterion.nativevitalio.model.AllergyHistoryItem
import com.critetiontech.ctvitalio.databinding.ItemAllergyCardBinding


class AllergiesAdapter : ListAdapter<AllergyHistoryItem, AllergiesAdapter.ViewHolder>(
    DiffCallback()
) {


    inner class ViewHolder(val binding: ItemAllergyCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAllergyCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        // Only show category title (e.g., "Drug Allergies") for the first item of each group
        holder.binding.categoryText.isVisible = !item.category.isNullOrBlank()
        holder.binding.categoryText.text = item.category

        holder.binding.substanceText.text = toCamelCase(item.substance)
        holder.binding.remarkText.text = toCamelCase(item.remark)
        holder.binding.severityText.text = toCamelCase(item.severityLevel)

        // Apply severity color
        val severityColor = when (item.severityLevel.lowercase()) {
            "mild" -> Color.parseColor("#FFA500")    // Orange
            "moderate" -> Color.parseColor("#FF5722") // Deep Orange
            "severe" -> Color.parseColor("#FF0000")   // Red
            else -> Color.DKGRAY
        }
        holder.binding.severityText.setTextColor(severityColor)
    }

    class DiffCallback : DiffUtil.ItemCallback<AllergyHistoryItem>() {
        override fun areItemsTheSame(oldItem: AllergyHistoryItem, newItem: AllergyHistoryItem): Boolean {
            return oldItem.rowId == newItem.rowId
        }

        override fun areContentsTheSame(oldItem: AllergyHistoryItem, newItem: AllergyHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}