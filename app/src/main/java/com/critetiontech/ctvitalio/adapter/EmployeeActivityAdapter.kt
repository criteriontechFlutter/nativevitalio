package com.critetiontech.ctvitalio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.EmployeeActivity

class EmployeeActivityAdapter(
    private var list: List<EmployeeActivity>,
    private val onClick: (EmployeeActivity) -> Unit
) : RecyclerView.Adapter<EmployeeActivityAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chip: TextView = view.findViewById(R.id.chipText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_chip, parent, false))

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.chip.text = item.activityName
        holder.itemView.setOnClickListener { onClick(item) }
    }

    fun updateList(newList: List<EmployeeActivity>) {
        list = newList
        notifyDataSetChanged()
    }
}