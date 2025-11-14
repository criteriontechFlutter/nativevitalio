package com.example.vitalio_pragya.fragment

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentBikingBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class BikingFragment : Fragment() {

    private var _binding: FragmentBikingBinding? = null
    private val binding get() = _binding!!

    private var startTime: String = ""
    private var endTime: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBikingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toolbar setup
        binding.titleToolbar.text = "Add Activity"
        binding.title.text = "Biking"

        // Show current time
        startTime = getCurrentTime()
        endTime = getCurrentTime()
        binding.startTimeButton.text = startTime
        binding.endTimeButton.text = endTime

        // Back button
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Edit button
        binding.editActivity.setOnClickListener {
            showInfoDialog("Edit Activity", "This will allow editing biking activity details.")
        }

        // Start time picker
        binding.startTimeContainer.setOnClickListener {
            pickTime { time ->
                startTime = time
                binding.startTimeButton.text = time
            }
        }

        // End time picker
        binding.endTimeContainer.setOnClickListener {
            pickTime { time ->
                endTime = time
                binding.endTimeButton.text = time
            }
        }

        // Save button
        binding.saveButton.setOnClickListener {
            showSuccessDialog()
        }
    }

    // Time picker
    private fun pickTime(onTimeSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        val dialog = TimePickerDialog(requireContext(), { _, h, m ->
            val amPm = if (h < 12) "AM" else "PM"
            val hourFormatted = if (h % 12 == 0) 12 else h % 12
            val timeString = String.format("%d:%02d %s", hourFormatted, m, amPm)
            onTimeSelected(timeString)
        }, hour, minute, false)

        dialog.show()
    }

    // Get current time
    private fun getCurrentTime(): String {
        val cal = Calendar.getInstance()
        val format = SimpleDateFormat("h:mm a", Locale.getDefault())
        return format.format(cal.time)
    }

    // ✅ Success Dialog (stays until OK is clicked)
    private fun showSuccessDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_success_bottom, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(dialogView)

        // Prevent dismissing by touching outside or pressing back
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setCanceledOnTouchOutside(false)

        // Load GIF
        val successIcon = dialogView.findViewById<ImageView>(R.id.successIcon)
        Glide.with(this)
            .asGif()
            .load(R.drawable.sucesses_dia)
            .into(successIcon)

        // OK button closes manually
        val okButton = dialogView.findViewById<Button>(R.id.okButton)
        okButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }


//         OK button closes manually
        val closeButton = dialogView.findViewById<ImageView>(R.id.closeButton)
        closeButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showInfoDialog(title: String, message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
