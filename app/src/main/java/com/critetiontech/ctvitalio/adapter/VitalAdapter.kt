package com.critetiontech.ctvitalio.adapter

import Vital
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R

class VitalAdapter(private val list: List<Vital>) :
    RecyclerView.Adapter<VitalAdapter.VitalViewHolder>() {

    // ✅ Filter once (BP_Dias remove)
    private val filteredList = list.filter {
        !it.vitalName?.contains("BP_Dias", ignoreCase = true).orDefault(false)
    }

    class VitalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.vitalTitle)
        val value: TextView = view.findViewById(R.id.vitalValue)
        val unit: TextView = view.findViewById(R.id.vitalUnit)
        val time: TextView = view.findViewById(R.id.vitalTime)

        val bpContainer: LinearLayout = view.findViewById(R.id.bpContainer)
        val sysValue: TextView = view.findViewById(R.id.sysValue)
        val diaValue: TextView = view.findViewById(R.id.diaValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VitalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vital_item, parent, false)
        return VitalViewHolder(view)
    }

    override fun onBindViewHolder(holder: VitalViewHolder, position: Int) {

        val item = filteredList[position]   // ✅ always filtered list


        if (item.vitalName?.contains("BP_Sys", ignoreCase = true) == true) {

            holder.title.text = "BP"
            // Show BP UI
            holder.value.visibility = View.GONE
//            holder.unit.visibility = View.GONE
            holder.bpContainer.visibility = View.VISIBLE

            holder.unit.text = " mmHg"
            holder.sysValue.text = item.vitalValue?.toString() ?: "-"

            // ✅ Get corresponding DIA from original list
            val diaItem = list.find {
                it.vitalName?.contains("BP_Dias", ignoreCase = true) == true
            }

            holder.diaValue.text = diaItem?.vitalValue?.toString() ?: "-"
        } else {

            holder.title.text = item.vitalName ?: "-"
            // Normal vital
            holder.value.visibility = View.VISIBLE
            holder.bpContainer.visibility = View.GONE
            holder.unit.visibility =
                if (item.unit.isNullOrEmpty()) View.GONE else View.VISIBLE

            val displayValue = item.vmValueText ?: item.vitalValue?.toString() ?: "-"
            holder.value.text = displayValue
            holder.unit.text = " " +item.unit ?: ""
        }

        // Severity / Time
        holder.time.text = item.severityLevel ?: "-"

        // Optional color handling
        item.vitalColor?.let { colorHex ->
            try {
                val color = Color.parseColor(colorHex)

                // 👇 opacity background
                val bgColor = Color.argb(
                    50,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )

                // 👇 drawable for radius
                val drawable = android.graphics.drawable.GradientDrawable()
                drawable.setColor(bgColor)
                drawable.cornerRadius = 20f   // 👈 radius (increase/decrease as needed)

                holder.time.background = drawable

                // text color
                holder.time.setTextColor(color)

            } catch (e: Exception) {
                holder.time.setTextColor(Color.DKGRAY)
            }
        }
    }

    override fun getItemCount() = filteredList.size   // ✅ FIXED
}

// ✅ Extension (safe null handling)
fun Boolean?.orDefault(default: Boolean) = this ?: default