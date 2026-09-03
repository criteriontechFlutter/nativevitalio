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
            holder.bpContainer.visibility = View.VISIBLE

            holder.unit.visibility = View.VISIBLE
            holder.unit.text = " mmHg"
            holder.sysValue.text = item.vitalValue?.toInt()?.toString() ?: item.vitalValue?.toString() ?: "-"

            // ✅ Get corresponding DIA from original list
            val diaItem = list.find {
                it.vitalName?.contains("BP_Dias", ignoreCase = true) == true
            }

            holder.diaValue.text = diaItem?.vitalValue?.toInt()?.toString() ?: diaItem?.vitalValue?.toString() ?: "-"
        } else {

            val rawTitle = item.vitalName ?: ""
            val formattedTitle = formatVitalTitle(rawTitle)
            holder.title.text = formattedTitle

            // Normal vital
            holder.value.visibility = View.VISIBLE
            holder.bpContainer.visibility = View.GONE

            val displayValue = item.vmValueText ?: item.vitalValue?.toString() ?: "-"
            holder.value.text = displayValue

            val unitStr = item.unit?.trim() ?: ""
            val rawNorm = normalizeString(rawTitle)
            val formattedNorm = normalizeString(formattedTitle)
            val unitNorm = normalizeString(unitStr)

            val isSameAsTitle = unitNorm.isNotEmpty() && (unitNorm == rawNorm || unitNorm == formattedNorm)
            val isAlreadyInValue = unitStr.isNotEmpty() && (displayValue.trim().endsWith(unitStr) || (unitStr == "%" && displayValue.contains("%")))

            if (unitStr.isEmpty() || isSameAsTitle || isAlreadyInValue) {
                holder.unit.visibility = View.GONE
                holder.unit.text = ""
            } else {
                holder.unit.visibility = View.VISIBLE
                holder.unit.text = " $unitStr"
            }
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

    private fun formatVitalTitle(name: String?): String {
        if (name.isNullOrBlank()) return "-"
        return when (name) {
            "TimeInBed" -> "Time In Bed"
            "TotalSleep" -> "Total Sleep"
            "RestorativeSleep" -> "Restorative Sleep"
            "Morningalertness", "MorningAlertness" -> "Morning Alertness"
            "SleepScore" -> "Sleep Score"
            "WaterIntake" -> "Water Intake"
            "TossTurn" -> "Toss & Turn"
            "ActiveHours" -> "Active Hours"
            "ActiveMinutes" -> "Active Minutes"
            "WeeklyActiveMinutes" -> "Weekly Active Minutes"
            "SleepCycles" -> "Sleep Cycles"
            "MovementIndex" -> "Movement Index"
            "RecoveryIndex" -> "Recovery Index"
            "StressScore" -> "Stress Score"
            "HeartRate" -> "Heart Rate"
            "RespRate" -> "Respiratory Rate"
            "TotalSteps" -> "Total Steps"
            "Sleep efficiency", "SleepEfficiency" -> "Sleep Efficiency"
            "REM Sleep", "REMSleep" -> "REM Sleep"
            "Deep Sleep", "DeepSleep" -> "Deep Sleep"
            "Light Sleep", "LightSleep" -> "Light Sleep"
            "NightRHR", "Night RHR" -> "Night RHR"
            else -> {
                name.replace("(?<=[a-z])(?=[A-Z])".toRegex(), " ")
                    .replace("(?<=[A-Z])(?=[A-Z][a-z])".toRegex(), " ")
                    .trim()
            }
        }
    }

    private fun normalizeString(s: String?): String {
        return s?.lowercase()?.replace(Regex("[^a-z0-9]"), "") ?: ""
    }

    override fun getItemCount() = filteredList.size   // ✅ FIXED
}

// ✅ Extension (safe null handling)
fun Boolean?.orDefault(default: Boolean) = this ?: default