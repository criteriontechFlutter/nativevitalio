package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class BottomSheetMapAdapter(
    private val items: List<Map<String, Any>>, // or Map<String, String> based on your data
    private val displayKey: String,
    private val onItemClick: (Map<String, Any>) -> Unit
) : RecyclerView.Adapter<BottomSheetMapAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tvItem: TextView = view.findViewById(android.R.id.text1)

        init {
            view.setOnClickListener {
                val item = items[adapterPosition]
                onItemClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val tv = LayoutInflater.from(parent.context).inflate(
            android.R.layout.simple_list_item_1,
            parent,
            false
        )
        return ViewHolder(tv)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvItem.text = item[displayKey]?.toString() ?: "N/A"
    }

    override fun getItemCount(): Int = items.size
}