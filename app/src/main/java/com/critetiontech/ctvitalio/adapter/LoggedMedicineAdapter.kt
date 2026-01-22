package com.critetiontech.ctvitalio.adapter

import LoggedMedicine
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
class LoggedMedicineAdapter(private val list: List<LoggedMedicine>) :
    RecyclerView.Adapter<LoggedMedicineAdapter.MedicineViewHolder>() {

    class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvMedicineName)
        val time: TextView = itemView.findViewById(R.id.tvTime)
        val status: ImageView = itemView.findViewById(R.id.imgStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_logged_medicine, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val item = list[position]

        holder.name.text = item.medicineName

        // Extract time from "2025-11-10T00:00:00"
        if (item.doseDate.length > 16) {
            holder.time.text = item.doseDate.substring(11, 16)    // HH:MM
        } else {
            holder.time.text = "--:--"
        }

        // Status UI
        if (item.isTaken == 1) {
            holder.status.setImageResource(R.drawable.ic_check)
            holder.status.setColorFilter(Color.parseColor("#2ECC71")) // Green = Taken
        } else {
            holder.status.setImageResource(R.drawable.ic_close)
            holder.status.setColorFilter(Color.parseColor("#FF3B30")) // Red = Missed
        }
    }

    override fun getItemCount() = list.size
}