package com.critetiontech.ctvitalio.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.BloodPressureLogItemBinding
import com.critetiontech.ctvitalio.model.BloodPressureReading

class BPReadingAdapter(
    private val readings: List<BloodPressureReading>
) : RecyclerView.Adapter<BPReadingAdapter.BPViewHolder>() {

    inner class BPViewHolder(val binding: BloodPressureLogItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BPViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BloodPressureLogItemBinding.inflate(inflater, parent, false)
        return BPViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BPViewHolder, position: Int) {
        val item = readings[position]
        with(holder.binding) {
            holder.binding.vitalTime.text = item.time
            holder.binding.bpValue.text = item.bp

            // Color logic
            holder.binding.bpValue.setTextColor(
                when {
                    item.sys < 90 || item.dia < 60 -> Color.RED
                    item.sys > 140 || item.dia > 90 -> Color.YELLOW
                    else -> Color.parseColor("#2D5BE3")
                }
            )

            // Static for now
            holder.binding.postureText.text = "Standing"
        }
    }

    override fun getItemCount() = readings.size
}
