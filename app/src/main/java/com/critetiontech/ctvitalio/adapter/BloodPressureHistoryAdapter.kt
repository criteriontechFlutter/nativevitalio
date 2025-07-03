package com.critetiontech.ctvitalio.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Visibility
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
            Log.d("TAG", "onBindViewHolder: "+ item.position)
            holder.binding.postureText.text=item.position
            if(item.dia==0 && item.sys==0){
                holder.binding.postureText.visibility=GONE
                holder.binding.bpUnit.text="%"
                holder.binding.bpLabel.visibility=GONE
            }


            // Color logic
            holder.binding.bpValue.setTextColor(
                when {
                    item.sys < 90 || item.dia < 60 -> Color.RED
                    item.sys > 140 || item.dia > 90 -> Color.parseColor("#FFA500")
                    else -> Color.parseColor("#2D5BE3")
                }
            )

        }
    }

    override fun getItemCount() = readings.size
}
