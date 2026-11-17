package com.critetiontech.vitalioCorporate.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.vitalioCorporate.databinding.ItemGlucoseLogBinding
import com.critetiontech.vitalioCorporate.model.GlucoseLog

class GlucoseAdapter(
    private var list: List<GlucoseLog>
) : RecyclerView.Adapter<GlucoseAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemGlucoseLogBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGlucoseLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]

        holder.binding.tvGlucoseValue.text =
            "${item.value} mg/dL"

        holder.binding.tvTime.text = item.time
        holder.binding.tvMealType.text = item.status
    }

    override fun getItemCount() = list.size

    fun updateData(newList: List<GlucoseLog>) {
        list = newList
        notifyDataSetChanged()
    }
}