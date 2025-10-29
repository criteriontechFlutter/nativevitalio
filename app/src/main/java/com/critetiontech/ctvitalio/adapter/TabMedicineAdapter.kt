package com.critetiontech.ctvitalio.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.transition.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.MedicineReminderTabPageBinding
import com.critetiontech.ctvitalio.model.Medicine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TabMedicineAdapter(
    private val medicines:  MutableList<Medicine>,
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

        // --- Bind data ---
        binding.medicineName.text = medicine.medicinename
        binding.medicineDose.text = "${medicine.dosageType} • ${medicine.frequency}"
        binding.medicineTime1.text = medicine.time

        // --- Parse medicine time ---
        val medicineTime = try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.parse(medicine.time)
        } catch (e: Exception) {
            null
        }

        val calendar = Calendar.getInstance()
        val currentTime = calendar.time

        // --- Determine time-of-day bucket based on medicine.time ---
        val backgroundColor: String
        val buttonColor: String
        if (medicineTime != null) {
            calendar.time = medicineTime
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            when (hour) {
                in 5..11 -> { // Morning
                    backgroundColor = "#E8F5E9" // Light green
                    buttonColor = "#EF6C00"
                }
                in 12..16 -> { // Afternoon
                    backgroundColor = "#FFF3E0" // Light orange
                    buttonColor = "#EF6C00"
                }
                in 17..20 -> { // Evening
                    backgroundColor = "#E3F2FD" // Light blue
                    buttonColor = "#1976D2"
                }
                else -> { // Night
                    backgroundColor = "#F3E5F5" // Light purple
                    buttonColor = "#7B1FA2"
                }
            }
        } else {
            // Default color if time parsing fails
            backgroundColor = "#FFFFFF"
            buttonColor = "#55CF72"
        }

        // --- Determine if medicine time has passed or upcoming ---
        val isUpcoming = medicineTime != null && currentTime.before(medicineTime)
        val isPast = medicineTime != null && currentTime.after(medicineTime)

        // --- UI update logic ---
        when {
            medicine.isTaken -> {
                binding.medicineCard.backgroundTintList =
                    ColorStateList.valueOf("#DFF2DF".toColorInt())
                binding.btnTaken.visibility = View.GONE
                binding.icReminder.visibility = View.VISIBLE
                binding.btnTaken.setBackgroundColor(Color.GRAY)
                binding.medicineTime1.setTextColor(Color.GRAY)
            }

            isPast  -> {
                // Missed medicine
                binding.medicineSection.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#E2E7F0"))

                binding.btnTaken.text = "Missed"

                val radius = 30f
                val backgroundDrawable = GradientDrawable().apply {
                    cornerRadius = radius
                    setColor(Color.parseColor(buttonColor))
                }
                binding.btnTaken.background = backgroundDrawable

                binding.medicineTime1.setTextColor(Color.BLACK)

            }

            isUpcoming -> {
                // Upcoming medicine (show time-based style)
                binding.medicineSection.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#E3F2FD"))
                binding.btnTaken.text = "UpComing"
                binding.btnTaken.setBackgroundColor(Color.parseColor(buttonColor))
                binding.medicineTime1.setTextColor(Color.BLACK)
            }

            else -> {
                // Default fallback
                binding.medicineCard.backgroundTintList =
                    ColorStateList.valueOf(Color.WHITE)
                binding.btnTaken.text = "Mark Taken"
                binding.btnTaken.setBackgroundColor(Color.parseColor("#55CF72"))
            }
        }

        // Handle card click
        binding.medicineCard.setOnClickListener {
            onItemClick(medicine)
        }

        // Handle Mark Taken click
        binding.btnTaken.setOnClickListener {
            medicine.isTaken = !medicine.isTaken
            notifyItemChanged(position)
        }
    }
    fun updateList(newList: List<Medicine>) {
        medicines.clear()
        medicines.addAll(newList)
        notifyDataSetChanged()
    }
    override fun getItemCount() = medicines.size
}
