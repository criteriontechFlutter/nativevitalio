package com.criterion.nativevitalio.adapter

import Vital
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.criterion.nativevitalio.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class VitalDetailsAdapter(
    private val onVitalButtonClick: (String) -> Unit,
    private val navController: NavController
) : RecyclerView.Adapter<VitalDetailsAdapter.VitalViewHolder>() {


    private val groupedVitals = mutableListOf<Triple<String, String, String>>()
    private val colorNormal = Color.parseColor("#1753DA") // Orange
    private val colorBorderline = Color.parseColor("#FFA500") // Orange
    private val colorCritical = Color.RED

    enum class RangeStatus {
        NORMAL, BORDERLINE, CRITICAL
    }

    fun submitVitals(list: List<Vital>) {
        groupedVitals.clear()

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

        list.forEach { vital ->
            val title = when (vital.vitalName) {
                "HeartRate" -> "Heart Rate"
                "Spo2" -> "Blood Oxygen (SpO2)"
                "Temperature" -> "Body Temperature"
                "RespRate" -> "Respiratory Rate"
                "RBS" -> "RBS"
                "Pulse" -> "Pulse Rate"
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
                    "Pulse" -> "${vital.vitalValue.toInt()} /min"
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

        val status = getVitalRangeStatus(title, valueText)

        holder.vitalValue.setTextColor(
            when (status) {
                RangeStatus.NORMAL -> colorNormal
                RangeStatus.BORDERLINE -> colorBorderline
                RangeStatus.CRITICAL -> colorCritical
            }
        )

        holder.vitalText.setOnClickListener {
            onVitalButtonClick(title)
        }
        holder.vitalLayout.setOnClickListener {
            val bundle = Bundle().apply {
                putString("vitalType", title)
                putString("itemData", groupedVitals[position].toString())
            }
            navController.navigate(R.id.action_vitalDetail_to_vitalHistoryFragment,bundle)
        }

//        holder.warningIcon.visibility =
//            if (status == RangeStatus.BORDERLINE || status == RangeStatus.CRITICAL) View.VISIBLE
//            else View.GONE
    }

    override fun getItemCount(): Int = groupedVitals.size

    inner class VitalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vitalTitle: TextView = view.findViewById(R.id.vitalTitle)
        val vitalValue: TextView = view.findViewById(R.id.vitalValue)
        val vitalTime: TextView = view.findViewById(R.id.vitalTime)
        val vitalText: TextView = view.findViewById(R.id.addVitalText)
        val vitalLayout: CardView = view.findViewById(R.id.vitalsLayout)
        //val warningIcon: ImageView = view.findViewById(R.id.warningIcon)
    }

    private fun getVitalRangeStatus(title: String, valueText: String): RangeStatus {
        return when (title) {
            "Blood Pressure" -> {
                val match = Regex("(\\d{2,3})/(\\d{2,3})").find(valueText)
                val (sys, dia) = match?.destructured?.let {
                    it.component1().toInt() to it.component2().toInt()
                } ?: return RangeStatus.NORMAL

                when {
                    sys in 90..120 && dia in 60..80 -> RangeStatus.NORMAL
                    sys in 121..139 || dia in 81..89 -> RangeStatus.BORDERLINE
                    else -> RangeStatus.CRITICAL
                }
            }

            "Heart Rate", "Pulse Rate" -> {
                val value = valueText.replace(Regex("[^\\d]"), "").toIntOrNull() ?: return RangeStatus.NORMAL
                when {
                    value in 60..100 -> RangeStatus.NORMAL
                    value in 50..59 || value in 101..110 -> RangeStatus.BORDERLINE
                    else -> RangeStatus.CRITICAL
                }
            }

            "Blood Oxygen (SpO2)" -> {
                val value = valueText.replace("%", "").toIntOrNull() ?: return RangeStatus.NORMAL
                when {
                    value >= 95 -> RangeStatus.NORMAL
                    value in 90..94 -> RangeStatus.BORDERLINE
                    else -> RangeStatus.CRITICAL
                }
            }

            "Body Temperature" -> {
                val value = valueText.replace(Regex("[^\\d.]"), "").toFloatOrNull() ?: return RangeStatus.NORMAL
                when {
                    value in 97.0..99.5 -> RangeStatus.NORMAL
                    value in 99.6..100.4 -> RangeStatus.BORDERLINE
                    else -> RangeStatus.CRITICAL
                }
            }

            "Respiratory Rate" -> {
                val value = valueText.replace(Regex("[^\\d]"), "").toIntOrNull() ?: return RangeStatus.NORMAL
                when {
                    value in 12..20 -> RangeStatus.NORMAL
                    value in 10..11 || value in 21..24 -> RangeStatus.BORDERLINE
                    else -> RangeStatus.CRITICAL
                }
            }

            "RBS" -> {
                val value = valueText.replace(Regex("[^\\d]"), "").toIntOrNull() ?: return RangeStatus.NORMAL
                when {
                    value in 70..140 -> RangeStatus.NORMAL
                    value in 141..180 -> RangeStatus.BORDERLINE
                    else -> RangeStatus.CRITICAL
                }
            }

            "Body Weight" -> RangeStatus.NORMAL

            else -> RangeStatus.NORMAL
        }
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
