package com.critetiontech.ctvitalio.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.Medicine

class MedicineAdapter(private val medicines: List<Medicine>) :
    RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {

    inner class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.iconMedicine)
        val name: TextView = itemView.findViewById(R.id.tvMedicineName)
        val time: TextView = itemView.findViewById(R.id.tvMedicineTime)
        val status: TextView = itemView.findViewById(R.id.tvMedicineStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.medicine_card, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val medicine = medicines[position]

        holder.name.text = medicine.name
        holder.time.text = medicine.time
        holder.status.text = medicine.status

        // Optional: change pill color by status
        val context = holder.itemView.context
        if (medicine.status == "Taken") {
            holder.status.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.greyBG)
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.greyText))
        } else {
            holder.status.backgroundTintList =
                ContextCompat.getColorStateList(context, R.color.red)
            holder.status.setTextColor(Color.WHITE)
        }
    }

    override fun getItemCount(): Int = medicines.size
}