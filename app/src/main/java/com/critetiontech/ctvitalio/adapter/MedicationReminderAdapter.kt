package com.critetiontech.ctvitalio.adapter


import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.databinding.ItemMedicationReminderBinding
import com.critetiontech.ctvitalio.model.Medicine
import java.text.SimpleDateFormat
import java.util.*

class MedicationReminderAdapter(
    private val medicines: List<Medicine>,
    private val onMarkTaken: (Medicine) -> Unit
) : RecyclerView.Adapter<MedicationReminderAdapter.ReminderViewHolder>() {

    inner class ReminderViewHolder(val binding: ItemMedicationReminderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemMedicationReminderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val medicine = medicines[position]
        val binding = holder.binding

        // --- Format Scheduled Time ---
        val displayTime = medicine.scheduledDateTime?.let { datetime ->
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val date = inputFormat.parse(datetime)
                date?.let { outputFormat.format(it) } ?: "--:--"
            } catch (e: Exception) {
                "--:--"
            }
        } ?: "--:--"

        // --- Compute "Next Dose" message ---
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val now = Calendar.getInstance().time

        val message = medicine.scheduledDateTime?.let { dateString ->
            try {
                val scheduled = sdf.parse(dateString)
                if (scheduled != null) {
                    val diffMillis = scheduled.time - now.time
                    val diffMinutes = diffMillis / (1000 * 60)
                    when {
                        diffMinutes > 60 -> {
                            val hours = diffMinutes / 60
                            "Next dose in $hours hour${if (hours > 1) "s" else ""} — stay on track"
                        }
                        diffMinutes in 1..59 -> {
                            "Next dose in $diffMinutes min${if (diffMinutes > 1) "s" else ""} — stay on track"
                        }
                        diffMinutes in -59..0 -> {
                            "It's time to take your dose!"
                        }
                        else -> {
                            val ago = -diffMinutes / 60
                            "You missed this dose $ago hour${if (ago > 1) "s" else ""} ago"
                        }
                    }
                } else "Stay on track with your dose"
            } catch (e: Exception) {
                "Stay on track with your dose"
            }
        } ?: "Stay on track with your dose"

        binding.tvReminderTitle.text = message
        binding.tvMedication.text = "${medicine.medicineName ?: "-"} ${medicine.dosageStrength ?: ""} ${medicine.unit ?: ""}"
        binding.tvFrequency.text = "${medicine.instructions ?: ""} | $displayTime"

        // --- Style Custom "Mark Taken" Button ---
        if (medicine.isTaken == 1) {
            styleButton(binding, "Taken", "#E6F4EA", "#1A7F37")
        } else {
            styleButton(binding, "Mark Taken", "#007BFF", "#FFFFFF")
        }

        // --- Handle Click ---
        binding.btnMarkTaken.setOnClickListener {
            if (medicine.isTaken == 0) {
                medicine.isTaken = 1
                notifyItemChanged(position)
                onMarkTaken(medicine)
            }
        }
    }
//override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
//    val medicine = medicines[position]
//    val binding = holder.binding
//
//    // --- Format Scheduled Time (for display) ---
//    val displayTime = medicine.scheduledDateTime?.let { datetime ->
//        try {
//            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//            val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
//            val date = inputFormat.parse(datetime)
//            date?.let { outputFormat.format(it) } ?: "--:--"
//        } catch (e: Exception) {
//            "--:--"
//        }
//    } ?: "--:--"
//
//    // --- Compute "Next Dose" message ---
//    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//    val now = Calendar.getInstance().time
//
//    val message = medicine.scheduledDateTime?.let { dateString ->
//        try {
//            val scheduled = sdf.parse(dateString)
//            if (scheduled != null) {
//                val diffMillis = scheduled.time - now.time
//                val diffMinutes = diffMillis / (1000 * 60)
//
//                when {
//                    diffMinutes > 60 -> {
//                        val hours = diffMinutes / 60
//                        "Next dose in $hours hour${if (hours > 1) "s" else ""} — stay on track"
//                    }
//                    diffMinutes in 1..59 -> {
//                        "Next dose in $diffMinutes min${if (diffMinutes > 1) "s" else ""} — stay on track"
//                    }
//                    diffMinutes in -59..0 -> {
//                        "It's time to take your dose!"
//                    }
//                    else -> {
//                        val ago = -diffMinutes / 60
//                        "You missed this dose $ago hour${if (ago > 1) "s" else ""} ago"
//                    }
//                }
//            } else "Stay on track with your dose"
//        } catch (e: Exception) {
//            "Stay on track with your dose"
//        }
//    } ?: "Stay on track with your dose"
//
//    // --- Bind UI ---
//    binding.tvReminderTitle.text = message
//    binding.tvMedication.text =
//        "${medicine.medicineName ?: "-"} ${medicine.dosageStrength ?: ""} ${medicine.unit ?: ""}"
//    binding.tvFrequency.text = "${medicine.instructions ?: ""} | $displayTime"
//
//    // --- Style Custom "Mark Taken" Button ---
//    if (medicine.isTaken == 1) {
//        styleButton(binding, "Taken", "#E6F4EA", "#1A7F37")
//    } else {
//        styleButton(binding, "Mark Taken", "#007BFF", "#FFFFFF")
//    }
//
//    // --- Handle Click ---
//    binding.btnMarkTaken.setOnClickListener {
//        if (medicine.isTaken == 0) {
//            medicine.isTaken = 1
//            notifyItemChanged(position)
//            onMarkTaken(medicine)
//        }
//    }
//}
    private fun styleButton(
        binding: ItemMedicationReminderBinding,
        text: String,
        bgColor: String,
        textColor: String
    ) {
        val btnLayout = binding.btnMarkTaken
        val btnText = binding.btnText
        val btnIcon = binding.ivCheckMother

        btnText.text = text
        btnText.setTextColor(Color.parseColor(textColor))
        btnIcon.setColorFilter(Color.parseColor(textColor))

        val shape = GradientDrawable().apply {
            cornerRadius = 40f
            setColor(Color.parseColor(bgColor))
        }
        btnLayout.background = shape
    }

    override fun getItemCount() = medicines.size

}