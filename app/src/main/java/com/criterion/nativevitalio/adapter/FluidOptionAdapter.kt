package com.criterion.nativevitalio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.ItemFluidOptionBinding
import com.criterion.nativevitalio.model.ManualFoodItem

class FluidOptionAdapter(
    private val items: List<ManualFoodItem>,
    private val selectedItemId: Int?,
    private val onSelect: (ManualFoodItem) -> Unit
) : RecyclerView.Adapter<FluidOptionAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemFluidOptionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFluidOptionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val isSelected = item.foodID == selectedItemId

        with(holder.binding) {
            fluidName.text = item.foodName
            fluidIcon.setImageResource(getIconForFood(item.foodName))

            fluidIcon.setColorFilter(
                ContextCompat.getColor(root.context,
                    if (isSelected) R.color.primaryBlue else R.color.gray
                )
            )
            fluidName.setTextColor(
                ContextCompat.getColor(root.context,
                    if (isSelected) R.color.primaryBlue else R.color.gray
                )
            )
            container.setBackgroundResource(
                if (isSelected) R.drawable.bg_card_selected else R.drawable.bg_card_unselected
            )

            container.setOnClickListener {
                onSelect(item)
            }
        }
    }

    override fun getItemCount() = items.size

    private fun getIconForFood(name: String): Int {
        return when (name.trim().lowercase()) {
            "water" -> R.drawable.ic_water
            "milk" -> R.drawable.ic_milk
            "tea" -> R.drawable.ic_tea
            "coffee" -> R.drawable.ic_coffee
            "juice", "fruit juice" -> R.drawable.ic_fruite_juice
            "beverage" -> R.drawable.ic_water
            else -> R.drawable.ic_water
        }
    }
}