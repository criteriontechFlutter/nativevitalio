package com.critetiontech.ctvitalio.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.FluidOutputItemLayoutBinding
import com.critetiontech.ctvitalio.model.FluidOutput

class FluidOutputHistoryAdapter (
    private val items: List<FluidOutput>,
) : RecyclerView.Adapter<FluidOutputHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: FluidOutputItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FluidOutputItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding

        // Quantity
        binding.outputVolume.text = "${item.quantity.toInt()} ${item.unitName}"

        // Time
        binding.outputTime.text = item.outputTimeFormat

        // Color Code
        val colorCode = item.colour.takeIf { it.isNotBlank() } ?: "#EEEEEE"

        // Set dot background
        try {

            binding.colorDot.setBackgroundColor(Color.parseColor(colorCode))
        } catch (e: Exception) {
            binding.colorDot.setBackgroundColor(Color.LTGRAY)
        }

        // Set color name
        binding.colorName.text = colorNameMap[colorCode] ?: "Unknown"
    }

    override fun getItemCount() = items.size

}

private val colorNameMap = mapOf(
    "#FFFDE7" to "Light Yellow",
    "#FFF176" to "Yellow",
    "#FFEB3B" to "Dark Yellow",
    "#FFC107" to "Amber",
    "#A1887F" to "Brown",
    "#D32F2F" to "Red"
)
