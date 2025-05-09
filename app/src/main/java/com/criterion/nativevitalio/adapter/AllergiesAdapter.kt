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
import com.criterion.nativevitalio.databinding.ItemAllergyCardBinding


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
    }

    class DiffCallback : DiffUtil.ItemCallback<AllergyHistoryItem>() {
        override fun areItemsTheSame(oldItem: AllergyHistoryItem, newItem: AllergyHistoryItem): Boolean {
            return oldItem.rowId != null && oldItem.rowId == newItem.rowId ||
                    oldItem.substance == newItem.substance  // fallback match
        }

        override fun areContentsTheSame(oldItem: AllergyHistoryItem, newItem: AllergyHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}