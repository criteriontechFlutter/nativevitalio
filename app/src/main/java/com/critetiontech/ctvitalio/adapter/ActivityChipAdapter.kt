package com.example.vitalio_pragya.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.EmployeeActivity
import kotlin.collections.get

class ActivityChipAdapter(
    private var activities: List<EmployeeActivity>,
    private val onActivityClick: (EmployeeActivity) -> Unit
) : RecyclerView.Adapter<ActivityChipAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.chipText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_activity_chip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val activity = activities[position]
        holder.textView.text = activity.activityName.toString()
        holder.itemView.setOnClickListener { onActivityClick( activity ) }
    }

    override fun getItemCount() = activities.size

    fun updateList(newList: List<EmployeeActivity>) {
        activities = newList
        notifyDataSetChanged()
    }
}