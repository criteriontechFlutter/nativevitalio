package com.criterion.nativevitalio.adapter

import DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.criterion.nativevitalio.databinding.FluidHistoryItemLayoutBinding
import com.criterion.nativevitalio.model.ManualFoodItem
import java.util.Locale

class FluidIntakeLogAdapter (
    private val items: List<ManualFoodItem>,
) : RecyclerView.Adapter<FluidIntakeLogAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: FluidHistoryItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FluidHistoryItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            fluidType.text = item.foodName.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }

            fluidTime.text =  DateUtils.formatDate(item.givenFoodDate, "yyyy-MM-dd", "dd MMM yyyy")
            fluidAmount.text = buildString {
                append(item.quantity)
                append(" ml")
            }


        }
    }

    override fun getItemCount() = items.size

}