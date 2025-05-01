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

//    fun submitVitals(list: List<Vital>) {
//        groupedVitals.clear()
//
//        val sys = list.find { it.vitalName == "BP_Sys" }
//        val dias = list.find { it.vitalName == "BP_Dias" }
//        if (sys != null && dias != null) {
//            groupedVitals.add(
//                Triple(
//                    "Blood Pressure",
//                    "${sys.vitalValue.toInt()}/${dias.vitalValue.toInt()} ${sys.unit}",
//                    sys.vitalDateTime.toString()
//                )
//            )
//        }
//
//        list.forEach { vital ->
//            val title = when (vital.vitalName) {
//                "HeartRate" -> "Heart Rate"
//                "Spo2" -> "Blood Oxygen (SpO2)"
//                "Temperature" -> "Body Temperature"
//                "RespRate" -> "Respiratory Rate"
//                "RBS" -> "RBS"
//                "Pulse" -> "Pulse Rate"
//                "Weight" -> "Body Weight"
//                else -> null
//            }
//
//            title?.let { t ->
//                val valueText = when (vital.vitalName) {
//                    "HeartRate" -> "${vital.vitalValue.toInt()} BPM"
//                    "Spo2" -> "${vital.vitalValue.toInt()}%"
//                    "Temperature" -> "${vital.vitalValue} °F"
//                    "RespRate" -> "${vital.vitalValue.toInt()} /min"
//                    "RBS" -> "${vital.vitalValue.toInt()} mg/dL"
//                    "Pulse" -> "${vital.vitalValue.toInt()} /min"
//                    "Weight" -> "${vital.vitalValue} kg"
//                    else -> ""
//                }
//
//                groupedVitals.add(Triple(t, valueText, vital.vitalDateTime.toString()))
//            }
//        }
//
//        notifyDataSetChanged()
//    }

    fun submitVitals(list: List<Vital>) {
        groupedVitals.clear()

        // Prepare a map from API list
        val vitalMap = list.associateBy { it.vitalName }

        // 1. Blood Pressure (Sys/Dias) check
        val sys = vitalMap["BP_Sys"]
        val dias = vitalMap["BP_Dias"]
        if (sys != null && dias != null) {
            groupedVitals.add(
                Triple(
                    "Blood Pressure",
                    "${sys.vitalValue.toInt()}/${dias.vitalValue.toInt()} ${sys.unit}",
                    sys.vitalDateTime ?: "-"
                )
            )
        } else {
            groupedVitals.add(Triple("Blood Pressure", "--/-- mm/Hg", "-"))
        }

        // 2. Other vitals
        val expectedVitals = listOf(
            "HeartRate" to "Heart Rate",
            "Spo2" to "Blood Oxygen (spo2)",
            "Temperature" to "Body Temperature",
            "RespRate" to "Respiratory Rate",
            "RBS" to "RBS",
            "Pulse" to "Pulse Rate",
            "Weight" to "Body Weight"
        )

        for ((vitalName, displayTitle) in expectedVitals) {
            val vital = vitalMap[vitalName]
            val valueText = when (vitalName) {
                "HeartRate" -> vital?.let { "${it.vitalValue.toInt()} BPM" } ?: "-- BPM"
                "Spo2" -> vital?.let { "${it.vitalValue.toInt()}%" } ?: "--%"
                "Temperature" -> vital?.let { "${it.vitalValue} °F" } ?: "-- °F"
                "RespRate" -> vital?.let { "${it.vitalValue.toInt()} /min" } ?: "-- min"
                "RBS" -> vital?.let { "${it.vitalValue.toInt()} mg/dL" } ?: "-- mg/dL"
                "Pulse" -> vital?.let { "${it.vitalValue.toInt()} /min" } ?: "-- /min"
                "Weight" -> vital?.let { "${it.vitalValue} kg" } ?: "-- kg"
                else -> "--"
            }

            val dateTime = vital?.vitalDateTime ?: "-"
            groupedVitals.add(Triple(displayTitle, valueText, dateTime))
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

        if (dateTime == "-" || valueText.contains("--")) {
            holder.vitalTime.text = "-"
            holder.vitalValue.setTextColor(Color.GRAY)
            holder.vitalValue.text = valueText  // No Spannable if missing
        } else {
            holder.vitalTime.text = getTimeAgo(dateTime)

            // Find number part and unit part separately
            val regex = Regex("^([\\d\\.\\/]+)\\s*(.*)$")
            val matchResult = regex.find(valueText)

            if (matchResult != null) {
                val (numberPart, unitPart) = matchResult.destructured

                val fullText = "$numberPart $unitPart"
                val spannable = android.text.SpannableString(fullText)

                val status = getVitalRangeStatus(title, valueText)
                val color = when (status) {
                    RangeStatus.NORMAL -> colorNormal
                    RangeStatus.BORDERLINE -> colorBorderline
                    RangeStatus.CRITICAL -> colorCritical
                }

                // 🎯 Color and Bold for number part
                spannable.setSpan(
                    android.text.style.ForegroundColorSpan(color),
                    0,
                    numberPart.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannable.setSpan(
                    android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                    0,
                    numberPart.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // 🎯 Make unit part smaller
                spannable.setSpan(
                    android.text.style.RelativeSizeSpan(0.7f),  // 70% size
                    numberPart.length + 1,  // +1 for space
                    fullText.length,
                    android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                holder.vitalValue.text = spannable
            } else {
                holder.vitalValue.text = valueText
            }
        }

        holder.vitalText.setOnClickListener {
            onVitalButtonClick(title)
        }

        holder.vitalLayout.setOnClickListener {
            val bundle = Bundle().apply {
                putString("vitalType", title)
                putString("itemData", groupedVitals[position].toString())
            }
            navController.navigate(R.id.action_vitalDetail_to_vitalHistoryFragment, bundle)
        }
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
