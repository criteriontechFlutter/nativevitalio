package com.criterion.nativevitalio.adapter

import Vital
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.viewpager.widget.PagerAdapter
import com.criterion.nativevitalio.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DashboardAdapter(
    private val context: Context,
    private val vitalList: List<Vital>,
    private val onVitalCardClick: (String) -> Unit
) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_vital_card, container, false)

        val vital = vitalList[position]
        val iconView = view.findViewById<ImageView>(R.id.vital_icon)
        val titleView = view.findViewById<TextView>(R.id.vital_title)
        val valueView = view.findViewById<TextView>(R.id.vital_value)
        val unitView = view.findViewById<TextView>(R.id.vital_unit)
        val timeView = view.findViewById<TextView>(R.id.vital_time)
        val addVitalButton = view.findViewById<Button>(R.id.add_vital_button)

        val zoomOut = AnimationUtils.loadAnimation(context, R.anim.zoom_out)
        valueView.startAnimation(zoomOut)

        // ✅ Map proper title
        val title = when (vital.vitalName) {
            "HeartRate" -> "Heart Rate"
            "Spo2" -> "Blood Oxygen (SpO2)"
            "Temperature" -> "Body Temperature"
            "RespRate" -> "Respiratory Rate"
            "RBS" -> "RBS"
            "Pulse" -> "Pulse Rate"
            "Weight" -> "Body Weight"
            "Blood Pressure" -> "Blood Pressure"
            else -> vital.vitalName ?: "--"
        }


        titleView.text = title

        // ✅ Format timestamp
        timeView.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && vital.vitalDateTime != null)
            getTimeAgo(vital.vitalDateTime!!)
        else
            vital.vitalDateTime ?: "--"

        // ✅ Show values or add button
        if (vital.vitalName.equals("Blood Pressure", true)) {
            if (vital.unit.isNullOrEmpty() || vital.unit.equals("0/0 mm/Hg", true)) {
                valueView.visibility = View.GONE
                unitView.visibility = View.GONE
                addVitalButton.visibility = View.VISIBLE
            } else {
                valueView.visibility = View.GONE
                unitView.visibility = View.VISIBLE
                unitView.text = vital.unit
                addVitalButton.visibility = View.GONE
            }
        } else if (vital.vitalValue == 0.0) {
            valueView.visibility = View.GONE
            unitView.visibility = View.GONE
            addVitalButton.visibility = View.VISIBLE
        } else {
            valueView.visibility = View.VISIBLE
            unitView.visibility = View.VISIBLE
            valueView.text = vital.vitalValue.toInt().toString()
            unitView.text = vital.unit ?: ""
            addVitalButton.visibility = View.GONE
        }

        // ✅ Icon assignment based on vital name
        val iconRes = when (vital.vitalName?.lowercase()) {
            "spo2" -> R.drawable.spo2
            "heartrate" -> R.drawable.heart_rate_1
            "temperature" -> R.drawable.body_temperature
            "pulse" -> R.drawable.spo2
            "weight" -> R.drawable.doctors
            "blood pressure" -> R.drawable.bloodpressure
            else -> R.drawable.doctors
        }
        iconView.setImageResource(iconRes)

        // ✅ Card click callback
        view.setOnClickListener {
            onVitalCardClick.invoke(title)
        }

        container.addView(view)
        return view
    }

    override fun getCount(): Int = vitalList.size
    override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj
    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTimeAgo(dateTimeStr: String): String {
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