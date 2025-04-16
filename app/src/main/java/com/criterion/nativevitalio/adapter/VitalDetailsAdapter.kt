package com.criterion.nativevitalio.adapter

import Vital
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.criterion.nativevitalio.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class VitalDetailsAdapter(
    private val onVitalButtonClick: (String) -> Unit
) : RecyclerView.Adapter<VitalDetailsAdapter.VitalViewHolder>() {

    // title, valueText, vitalDateTime
    private val groupedVitals = mutableListOf<Triple<String, String, String>>()
    private val colorCritical = Color.RED
    private val colorNormal = Color.BLACK

    fun submitVitals(list: List<Vital>) {
        groupedVitals.clear()

        // Group systolic and diastolic BP
        val sys = list.find { it.vitalName == "BP_Sys" }
        val dias = list.find { it.vitalName == "BP_Dias" }
        if (sys != null && dias != null) {
            groupedVitals.add(
                Triple(
                    "Blood Pressure",
                    "${sys.vitalValue.toInt()}/${dias.vitalValue.toInt()} ${sys.unit}",
                    sys.vitalDateTime.toString()
                )
            )
        }

        // Add other vitals
        list.forEach { vital ->
            val title = when (vital.vitalName) {
                "HeartRate" -> "Heart Rate"
                "Spo2" -> "Blood Oxygen (SpO2)"
                "Temperature" -> "Body Temperature"
                "RespRate" -> "Respiratory Rate"
                "RBS" -> "RBS"
                "Weight" -> "Body Weight"
                else -> null
            }

            title?.let { t ->
                val valueText = when (vital.vitalName) {
                    "HeartRate" -> "${vital.vitalValue.toInt()} BPM"
                    "Spo2" -> "${vital.vitalValue.toInt()}%"
                    "Temperature" -> "${vital.vitalValue} °F"
                    "RespRate" -> "${vital.vitalValue.toInt()} /min"
                    "RBS" -> "${vital.vitalValue.toInt()} mg/dL"
                    "Weight" -> "${vital.vitalValue} kg"
                    else -> ""
                }

                groupedVitals.add(Triple(t, valueText, vital.vitalDateTime.toString()))
            }
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VitalViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.vital_item, parent, false)
        return VitalViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: VitalViewHolder, position: Int) {
        val (title, valueText, dateTime) = groupedVitals[position]

        holder.vitalTitle.text = title
        holder.vitalValue.text = valueText
        holder.vitalTime.text = getTimeAgo(dateTime)

        // Highlight critical values
        holder.vitalValue.setTextColor(
            if (title == "Body Temperature" && valueText.contains("102") || valueText.contains("88/54"))
                colorCritical
            else colorNormal
        )

        holder.vitalButton.setOnClickListener {
            onVitalButtonClick(title)
        }
    }

    override fun getItemCount(): Int = groupedVitals.size

    inner class VitalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vitalTitle: TextView = view.findViewById(R.id.vitalTitle)
        val vitalValue: TextView = view.findViewById(R.id.vitalValue)
        val vitalTime: TextView = view.findViewById(R.id.vitalTime)
        val vitalButton: TextView = view.findViewById(R.id.vitalButton)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeAgo(dateTimeStr: String): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val dateTime = LocalDateTime.parse(dateTimeStr, formatter)
            val now = LocalDateTime.now()

            val minutes = ChronoUnit.MINUTES.between(dateTime, now)
            val hours = ChronoUnit.HOURS.between(dateTime, now)
            val days = ChronoUnit.DAYS.between(dateTime, now)
            val months = ChronoUnit.MONTHS.between(dateTime.toLocalDate(), now.toLocalDate())

            when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "$minutes min ago"
                hours < 24 -> "$hours hr ago"
                days < 30 -> "$days day${if (days > 1) "s" else ""} ago"
                else -> "$months month${if (months > 1) "s" else ""} ago"
            }
        } catch (e: Exception) {
            "-"
        }
    }
}