package com.critetiontech.ctvitalio.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.MedicineReminderTabPageBinding
import com.critetiontech.ctvitalio.model.Medicine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class TabMedicineAdapter(
    private val medicines: MutableList<Medicine>,
    private val onItemClick: (Medicine) -> Unit
) : RecyclerView.Adapter<TabMedicineAdapter.MedicineViewHolder>() {

    inner class MedicineViewHolder(val binding: MedicineReminderTabPageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicineViewHolder {
        val binding = MedicineReminderTabPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MedicineViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MedicineViewHolder, position: Int) {
        val medicine = medicines[position]
        val binding = holder.binding

        // ---- Basic Info ----
        binding.medicineName.text = medicine.instructions ?: "-"
        binding.medicineDose.text =
            "${medicine.dosageStrength ?: 0} ${medicine.dosageType ?: ""} • ${medicine.instructions ?: ""}"

        // ---- Parse Time ----
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val medicineTime = try { timeFormat.parse(medicine.scheduledDateTime ?: "") } catch (e: Exception) { null }
        binding.medicineTime1.text = medicine.scheduledDateTime ?: "--:--"

        // ---- Determine Period ----
        val calendar = Calendar.getInstance()
        if (medicineTime != null) calendar.time = medicineTime
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val (period, iconRes, bgTint) = when (hour) {
            in 5..11 -> Triple("Morning", R.drawable.morning_icon, "#E6F4FF")
            in 12..16 -> Triple("Afternoon", R.drawable.morning_icon, "#FFF3E0")
            in 17..20 -> Triple("Evening", R.drawable.morning_icon, "#E3F2FD")
            else -> Triple("Night", R.drawable.morning_icon, "#ECEAFF")
        }

        // ---- Header Visibility ----
        val showHeader = position == 0 || getPeriodForPosition(position - 1) != period
        binding.periodSection.visibility = if (showHeader) View.VISIBLE else View.GONE
        binding.periodName.text = period
        binding.periodIcon.setImageResource(iconRes)

        // ---- Card Background ----
        val bgDrawable = GradientDrawable().apply {
            cornerRadius = 30f
            setColor(Color.parseColor("#FFFFFF"))
        }
        binding.medicineCard.background = bgDrawable

        // ---- Button States ----
        val currentTime = Calendar.getInstance().time
        val isUpcoming = medicineTime != null && currentTime.before(medicineTime)
        val isPast = medicineTime != null && currentTime.after(medicineTime)

        when {
            medicine.isTaken == 1 -> {
                styleButton(binding, "Taken", "#E6F4EA", "#1A7F37", 1)
            }
            isPast -> {
                styleButton(binding, "Missed", "#FEECEC", "#D32F2F", 0)
            }
            isUpcoming -> {
                styleButton(binding, "Upcoming", "#E3F2FD", "#1976D2", 0)
            }
            else -> {
                styleButton(binding, "Mark Taken", "#DFF2DF", "#388E3C", 0)
            }
        }


        // ---- Click Actions ----
        binding.medicineCard.setOnClickListener { onItemClick(medicine) }

        binding.btnTaken.setOnClickListener {
            medicine.isTaken != 0
            notifyItemChanged(position)
        }
    }

    private fun getPeriodForPosition(position: Int): String {
        val med = medicines[position]
        val time = med.scheduledDateTime ?: return "Unknown"
        return try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = sdf.parse(time)
            val cal = Calendar.getInstance()
            cal.time = date ?: return "Unknown"
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            when (hour) {
                in 5..11 -> "Morning"
                in 12..16 -> "Afternoon"
                in 17..20 -> "Evening"
                else -> "Night"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun styleButton(binding: MedicineReminderTabPageBinding, text: String, bgColor: String, textColor: String, filled: Int) {
        binding.btnTaken.text = text
        val shape = GradientDrawable().apply {
            cornerRadius = 50f
            setColor(Color.parseColor(bgColor))
        }
        binding.btnTaken.background = shape
        binding.btnTaken.setTextColor(Color.parseColor(textColor))
    }

    override fun getItemCount() = medicines.size

    fun updateList(newList: List<Medicine>) {
        medicines.clear()
        medicines.addAll(newList)
        notifyDataSetChanged()
    }
}