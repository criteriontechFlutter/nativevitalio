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
        binding.medicineName.text = medicine.medicineName ?: "-"
        binding.medicineDose.text =
            "${medicine.dosageStrength} ${medicine.unit} ${medicine.dosageType ?: ""} • ${medicine.instructions ?: ""}"

        binding.medicineTime1.text = medicine.scheduledDateTime ?: "--:--"

        // ---- Use API DayPeriod Directly ----
        val period = medicine.dayPeriod ?: "Unknown"
        val iconRes = when (period.lowercase()) {
            "morning" -> R.drawable.morning_icon
            "afternoon" -> R.drawable.ic_afternoon
            "evening" -> R.drawable.morning_icon
            "night" -> R.drawable.ic_night
            else -> R.drawable.morning_icon
        }

        // ---- Show Header only when new period starts ----
        val showHeader = position == 0 || medicines[position - 1].dayPeriod != period
        binding.periodSection.visibility = if (showHeader) View.VISIBLE else View.INVISIBLE
        binding.periodName.text = period
        binding.periodIcon.setImageResource(iconRes)

        // ---- Card Background ----
//        val bgDrawable = GradientDrawable().apply {
//            cornerRadius = 30f
//            setColor(Color.WHITE)
//        }
//        binding.medicineCard.background = bgDrawable

        // ---- Button Styling ----
        when {
            medicine.isTaken == 1 -> {
                styleButton(binding, "Taken", "#E6F4EA", "#1A7F37")
            }
            else -> {
                styleButton(binding, "Mark Taken", "#E6F4FF", "#1976D2")
            }
        }

        // ---- Clicks ----
        binding.medicineCard.setOnClickListener {

        }
        binding.btnTaken.setOnClickListener {
            medicine.isTaken = 1
            notifyItemChanged(position)
            onItemClick(medicine)
        }
    }

    private fun styleButton(binding: MedicineReminderTabPageBinding, text: String, bgColor: String, textColor: String) {
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
