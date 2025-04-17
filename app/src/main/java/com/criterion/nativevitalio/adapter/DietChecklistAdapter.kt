package com.criterion.nativevitalio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.model.DietItemModel
import com.criterion.nativevitalio.model.DietListItem


class DietChecklistAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<DietListItem>()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    fun setData(data: List<DietListItem>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DietListItem.Header -> TYPE_HEADER
            is DietListItem.Item -> TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_diet_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_diet_entry, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DietListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is DietListItem.Item -> (holder as ItemViewHolder).bind(item.diet)
        }
    }

    // ViewHolder for header (time slot)
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvHeader: TextView = itemView.findViewById(R.id.tvHeader)
        fun bind(header: DietListItem.Header) {
            tvHeader.text = header.title
        }
    }

    // ViewHolder for each diet entry
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvFoodName: TextView = itemView.findViewById(R.id.tvFoodName)
        private val tvQtyUnit: TextView = itemView.findViewById(R.id.tvQtyUnit)
        private val imgGivenStatus: ImageView = itemView.findViewById(R.id.imgGivenStatus)

        fun bind(model: DietItemModel) {
            tvFoodName.text = model.foodName.trim()
            tvQtyUnit.text = "${model.foodQty} ${model.unitName}"

            imgGivenStatus.setImageResource(
                if (model.isGiven == 1  )
                    R.drawable.ic_checkbox_checked
                else
                    R.drawable.ic_checkbox_square
            )
        }
    }
}