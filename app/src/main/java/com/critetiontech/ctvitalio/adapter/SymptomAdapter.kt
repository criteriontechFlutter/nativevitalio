package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.ItemSymptomBinding
import com.critetiontech.ctvitalio.interfaces.AdapterInterface
import com.critetiontech.ctvitalio.model.ProblemWithIcon
import com.critetiontech.ctvitalio.utils.MyApplication

class SymptomAdapter(
    private var items: List<ProblemWithIcon>,
    private var selectedItems: List<ProblemWithIcon>,
    private val listener: AdapterInterface<ProblemWithIcon>,
    private val isSearchMode: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_GRID = 0
        private const val TYPE_SEARCH = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (isSearchMode) TYPE_SEARCH else TYPE_GRID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SEARCH) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.searched_item_selected_chip, parent, false)
            SearchSuggestionViewHolder(view)
        } else {
            val binding = ItemSymptomBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            SymptomViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is SymptomViewHolder -> {
                holder.bind(item, selectedItems.contains(item))
                holder.itemView.setOnClickListener {
                    listener.onClick(position, item)
                }
            }
            is SearchSuggestionViewHolder -> {
                holder.bind(item)
                holder.itemView.setOnClickListener {
                    listener.onClick(position, item)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ProblemWithIcon>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun updateSelectedItems(newSelectedItems: List<ProblemWithIcon>) {
        selectedItems = newSelectedItems
        notifyDataSetChanged()
    }

    inner class SymptomViewHolder(
        private val binding: ItemSymptomBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProblemWithIcon, isSelected: Boolean) {
            binding.symptom = item

            val bgColor = if (isSelected) R.color.primaryBlue else R.color.dashboardIconBg
            val textColor = if (isSelected) android.R.color.white else R.color.textColor

            binding.root.setBackgroundColor(binding.root.context.getColor(bgColor))
            binding.symptomText.setTextColor(MyApplication.appContext.getColor(textColor))
            Glide.with(MyApplication.appContext) // or `this` if inside Activity
                .load(item.displayIcon) // or R.drawable.image
                .placeholder(R.drawable.baseline_person_24)
                .circleCrop() // optional: makes it circular
                .into(binding.problemICon)
            binding.executePendingBindings()
        }
    }



    inner class SearchSuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val symptomName: TextView = itemView.findViewById(R.id.searchChipText)

        fun bind(item: ProblemWithIcon) {
            symptomName.text = item.problemName
            val isSelected = selectedItems.contains(item)
            val bgRes = if (isSelected) R.drawable.chip_background_selected else R.drawable.chip_background_unselected
            val textColor = if (isSelected) android.R.color.white else R.color.terms_color
            symptomName.setBackgroundResource(bgRes)
            symptomName.setTextColor(MyApplication.appContext.getColor(textColor))
        }
    }
}