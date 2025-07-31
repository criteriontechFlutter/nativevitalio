package com.critetiontech.ctvitalio.adapter

import Vital
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.alpha
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.critetiontech.ctvitalio.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Random

class DashboardAdapter(
    private val context: Context,
    private val vitalList: List<Vital>,
    private val onVitalCardClick: (String) -> Unit
) : RecyclerView.Adapter<DashboardAdapter.VitalViewHolder>() {

inner class VitalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val iconView: ImageView = itemView.findViewById(R.id.vital_icon)
    val titleView: TextView = itemView.findViewById(R.id.vital_title)
    val valueView: TextView = itemView.findViewById(R.id.vital_value)
    val cardLayout: ConstraintLayout = itemView.findViewById(R.id.vitalCard)
    val arrowIcon: ImageView = itemView.findViewById(R.id.arrow_icon)
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VitalViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_vital_card, parent, false)
        return VitalViewHolder(view)
    }

    override fun onBindViewHolder(holder: VitalViewHolder, position: Int) {
        val vital = vitalList[position]

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
        val randomColor = getVeryLightPastelColor()
        holder.itemView.backgroundTintList = ColorStateList.valueOf(randomColor)




        val shape = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            setColor(getSlightlyDarkerPastelColor()) // Set based on your logic
        }

        val itemContainer = holder.itemView.findViewById<FrameLayout>(R.id.icon_container)
        itemContainer.background = shape
        holder.arrowIcon.setOnClickListener {
            onVitalCardClick.invoke(title)
        }

        holder.titleView.text = title

        val isBloodPressure = vital.vitalName.equals("Blood Pressure", ignoreCase = true)

          // Logic to show/hide the value view
        val shouldShowValue = if (isBloodPressure) {
            !vital.unit.isNullOrEmpty() && !vital.unit.equals("0/0 mm/Hg", ignoreCase = true)
        } else {
            vital.vitalValue != 0.0
        }

        holder.valueView.visibility = if (shouldShowValue) View.VISIBLE else View.GONE

          // Set the text based on the vital type
        if (shouldShowValue) {
            holder.valueView.text = if (isBloodPressure) {
                vital.unit.toString()
            } else {
                vital.vitalValue.toInt().toString()+vital.unit
            }
        }

        val iconRes = when (vital.vitalName?.lowercase()) {
            "spo2" -> R.drawable.spo_2_new_icon
            "heartrate" -> R.drawable.heart_icon_new
            "temperature" -> R.drawable.body_temprature
            "pulse" -> R.drawable.spo2
            "weight" -> R.drawable.doctors
            "blood pressure" -> R.drawable.bp_new_icon
            else -> R.drawable.doctors
        }



        holder.iconView.setImageResource(iconRes)

        holder.cardLayout.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))
        holder.valueView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.zoom_out))

        holder.itemView.setOnClickListener {
            onVitalCardClick(title)
        }
    }
    private fun getSlightlyDarkerPastelColor(): Int {
        val rnd = Random()
        val red = 240 + rnd.nextInt(16)   // 240–255
        val green = 240 + rnd.nextInt(16)
        val blue = 240 + rnd.nextInt(16)
        val alpha = 255

        val darkeningFactor = 0.8f  // between 0.0 (black) and 1.0 (no change)

        return android.graphics.Color.argb(
            alpha,
            (red * darkeningFactor).toInt().coerceIn(0, 255),
            (green * darkeningFactor).toInt().coerceIn(0, 255),
            (blue * darkeningFactor).toInt().coerceIn(0, 255)
        )
    }


    private fun getVeryLightPastelColor(): Int {
        val rnd = Random()
        val red = 240 + rnd.nextInt(16)   // 240–255
        val green = 240 + rnd.nextInt(16) // 240–255
        val blue = 240 + rnd.nextInt(16)  // 240–255
        val alpha = 255

        return android.graphics.Color.argb(alpha, red, green, blue)
    }

    override fun getItemCount(): Int = vitalList.size
}