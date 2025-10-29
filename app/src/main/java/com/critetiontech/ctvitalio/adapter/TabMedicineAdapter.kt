package com.critetiontech.ctvitalio.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.Medicine

class TabMedicineAdapter(
private val medicines: List<Medicine>,
private val onItemClick: (Medicine) -> Unit
) : RecyclerView.Adapter<TabMedicineAdapter.MedicineViewHolder>() {

    inner class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicineCard: LinearLayout = itemView.findViewById(R.id.medicine_card)
        val medicineTime: ImageView = itemView.findViewById(R.id.medicine_time)
        val medicineName: TextView = itemView.findViewById(R.id.medicine_name)
        val medicineDose: TextView = itemView.findViewById(R.id.medicine_dose)
       // val medicineNote: TextView = itemView.findViewById(R.id.medicine_note)
        val btnMarkTaken: TextView = itemView.findViewById(R.id.btn_taken)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.medicine_reminder_tab_page, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val medicine = medicines[position]
//        holder.medicineTime.text = medicine.time
        holder.medicineName.text = medicine.name
        holder.medicineDose.text = "${medicine.dose} • ${medicine.frequency}"
      //  holder.medicineNote.text = medicine.note

        // Update UI based on taken state
        if (medicine.isTaken) {
            holder.medicineCard.backgroundTintList =
                ColorStateList.valueOf(Color.parseColor("#DFF2DF"))
//            holder.medicineTime.setTextColor(Color.GRAY)
            holder.btnMarkTaken.text = "Taken"
//            holder.btnMarkTaken.setBackgroundColor(Color.GRAY)
        } else {
            holder.medicineCard.backgroundTintList =
                ColorStateList.valueOf(Color.WHITE)
//            holder.medicineTime.setTextColor(Color.parseColor("#55CF72"))
            holder.btnMarkTaken.text = "Mark Taken"
//            holder.btnMarkTaken.setBackgroundColor(Color.parseColor("#55CF72"))
        }
        holder.medicineCard.setOnClickListener {
            onItemClick(medicine)
        }
        // Handle button click
        holder.btnMarkTaken.setOnClickListener {
            medicine.isTaken = !medicine.isTaken
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = medicines.size
}