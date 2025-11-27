package com.critetiontech.ctvitalio.adapter

 import AllMedicine
 import Medicine
 import android.R
 import android.content.Context
 import android.view.LayoutInflater
 import android.view.View
 import android.view.ViewGroup
 import android.widget.ImageView
 import android.widget.TextView
 import androidx.annotation.NonNull
 import androidx.recyclerview.widget.RecyclerView
  import java.util.Locale


class MedicineAdapter(
    private val context: Context,
    private val list: List<AllMedicine>
) : RecyclerView.Adapter<MedicineAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(com.critetiontech.ctvitalio.R.layout.item_medicine_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val med = list[position]

        holder.tvName.text = med.medicineName
        holder.tvType.text = med.dosageType

        setMedicineIcon(holder.icon, med.dosageType)
    }

    private fun setMedicineIcon(imageView: ImageView, type: String?) {
        when (type?.trim()?.lowercase(Locale.getDefault())) {
            "tablet", "tablets" -> imageView.setImageResource(com.critetiontech.ctvitalio.R.drawable.table)
            "capsule", "capsules" -> imageView.setImageResource(com.critetiontech.ctvitalio.R.drawable.softgelcapsule)
            "syrup" -> imageView.setImageResource(com.critetiontech.ctvitalio.R.drawable.syrup)
            "injection" -> imageView.setImageResource(com.critetiontech.ctvitalio.R.drawable.injection)
            "drops" -> imageView.setImageResource(com.critetiontech.ctvitalio.R.drawable.tube)
            else -> imageView.setImageResource(com.critetiontech.ctvitalio.R.drawable.table) // default
        }
    }

    override fun getItemCount() = list.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(com.critetiontech.ctvitalio.R.id.tvMedName)
        val tvType: TextView = itemView.findViewById(com.critetiontech.ctvitalio.R.id.tvMedType)
        val icon: ImageView = itemView.findViewById(com.critetiontech.ctvitalio.R.id.ivIcon)
    }
}