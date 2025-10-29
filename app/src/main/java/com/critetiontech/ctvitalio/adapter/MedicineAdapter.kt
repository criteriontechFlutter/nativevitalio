package com.critetiontech.ctvitalio.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.model.Medicine

class MedicineAdapter(private val medicines: List<Medicine>) :
    RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder>() {
    inner class MedicineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val medicineCard: LinearLayout = itemView.findViewById(R.id.medicine_card)
        val medicineTime: TextView = itemView.findViewById(R.id.medicine_time)
        val medicineName: TextView = itemView.findViewById(R.id.medicine_name)
        val medicineDose: TextView = itemView.findViewById(R.id.medicine_dose)
     //   val medicineNote: TextView = itemView.findViewById(R.id.medicine_note)
        val btnMarkTaken: TextView = itemView.findViewById(R.id.btn_taken)
       // val btnSkip: TextView = itemView.findViewById(R.id.btn_skip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.medicine_reminder_tab_page, parent, false)
        return MedicineViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val medicine = medicines[position]

        // Bind data
        holder.medicineTime.text = medicine.time
        holder.medicineName.text = medicine.name
        holder.medicineDose.text = "${medicine.dose} • ${medicine.frequency}"
       // holder.medicineNote.text = medicine.note

        // Update UI based on taken/skipped state
        if (medicine.isTaken) {
            holder.medicineCard.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#DFF2DF"))
            holder.medicineTime.setTextColor(Color.GRAY)
            holder.btnMarkTaken.text = "Taken"
            holder.btnMarkTaken.setBackgroundColor(Color.GRAY)
        } else if (medicine.isSkipped) {
            holder.medicineCard.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFEAEA"))
            holder.medicineTime.setTextColor(Color.RED)
            holder.btnMarkTaken.text = "Mark Taken"
            holder.btnMarkTaken.setBackgroundColor(Color.parseColor("#55CF72"))
        } else {
            holder.medicineCard.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            holder.medicineTime.setTextColor(Color.parseColor("#55CF72"))
            holder.btnMarkTaken.text = "Mark Taken"
            holder.btnMarkTaken.setBackgroundColor(Color.parseColor("#55CF72"))
        }

        // Mark Taken click
        holder.btnMarkTaken.setOnClickListener {
            medicine.isTaken = !medicine.isTaken
            if (medicine.isTaken) medicine.isSkipped = false
            notifyItemChanged(position)
        }

        // Skip click
//        holder.btnSkip.setOnClickListener {
//            medicine.isSkipped = !medicine.isSkipped
//            if (medicine.isSkipped) medicine.isTaken = false
//            notifyItemChanged(position)
//        }
    }

    override fun getItemCount() = medicines.size
}
