package com.criterion.nativevitalio.adapter

import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.FluidOutputItemLayoutBinding
import com.critetiontech.ctvitalio.model.FluidOutputSummary

class FluidOutputRangeAdapter (
    private val items: List<FluidOutputSummary>,
) : RecyclerView.Adapter<FluidOutputRangeAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: FluidOutputItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            FluidOutputItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            colorName.text=item.outputDate
            outputVolume.text=item.quantity.toString()+" ml"
            outputTime.visibility=GONE
            colorDot.visibility=GONE


        }
    }

    override fun getItemCount() = items.size
}

