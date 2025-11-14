package com.example.vitalio_pragya.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.google.android.material.bottomsheet.BottomSheetDialog

import java.util.*

class AddMedicineReminderFragment : Fragment() {

    private lateinit var frequencySpinner: Spinner
    private lateinit var timeTextView: TextView
    private lateinit var startDateEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var containerEveryXDay: LinearLayout
    private lateinit var containerDaysOfWeek: LinearLayout
    private lateinit var containerSpecificDates: LinearLayout
    private lateinit var addMedicine: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_add_medicine_reminder, container, false)

        frequencySpinner = view.findViewById(R.id.spFrequency)
        timeTextView = view.findViewById(R.id.tvTime)
        startDateEditText = view.findViewById(R.id.etStartDate)
        endDateEditText = view.findViewById(R.id.etEndDate)
        containerEveryXDay = view.findViewById(R.id.containerEveryXDay)
        containerDaysOfWeek = view.findViewById(R.id.containerDaysOfWeek)
        containerSpecificDates = view.findViewById(R.id.containerSpecificDates)
        addMedicine = view.findViewById(R.id.addMedicine)

        setupFrequencyDropdown()
        setupDatePickers()
        setupTimePicker()

        addMedicine.setOnClickListener {
            showMedicineDialog()
        }

        return view
    }

    private fun showMedicineDialog() {
        val dialogView = layoutInflater.inflate(R.layout.medicine_reminder_schedule_dialogue, null)
        val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(dialogView)
        bottomSheetDialog.setCancelable(false)
        bottomSheetDialog.setCanceledOnTouchOutside(false)

        // Medicine GIF
        val medicineIcon = dialogView.findViewById<ImageView>(R.id.medicineReminder)
        Glide.with(this)
            .asGif()
            .load(R.drawable.medicine_reminder)
            .into(medicineIcon)

        // Close button
        val closeButton = dialogView.findViewById<ImageView>(R.id.closeButton)
        closeButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        // "Add More" button
        val addMoreButton = dialogView.findViewById<Button>(R.id.btnAddMore)
        addMoreButton.setOnClickListener {
            bottomSheetDialog.dismiss() // close this success dialog

            // 👇 Open Add Medicine screen again
            // Option 1: if you already have a button that opens the add medicine dialog


            // Option 2 (alternative): directly call your existing function if it exists
            // showAddMedicineDialog()
        }

        // Finally, show the dialog
        bottomSheetDialog.show()
    }


    private fun setupFrequencyDropdown() {
        val frequencies = arrayOf("Every day", "Every x day", "Every week", "Every month", "As Needed")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, frequencies)
        frequencySpinner.adapter = adapter

        frequencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    1 -> showFrequencyContainer(containerEveryXDay)
                    2 -> showFrequencyContainer(containerDaysOfWeek)
                    3 -> showFrequencyContainer(containerSpecificDates)
                    else -> hideAllFrequencyContainers()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()
        val listener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            val date = String.format("%02d/%02d/%04d", day, month + 1, year)
            (if (startDateEditText.isFocused) startDateEditText else endDateEditText).setText(date)
        }

        startDateEditText.setOnClickListener {
            DatePickerDialog(requireContext(), listener, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]).show()
        }

        endDateEditText.setOnClickListener {
            DatePickerDialog(requireContext(), listener, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH]).show()
        }
    }

    private fun setupTimePicker() {
        timeTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    timeTextView.text = String.format("%02d:%02d", hour, minute)
                },
                calendar[Calendar.HOUR_OF_DAY],
                calendar[Calendar.MINUTE],
                true
            ).show()
        }
    }


    private fun showFrequencyContainer(container: LinearLayout) {
        hideAllFrequencyContainers()
        container.visibility = View.VISIBLE
    }

    private fun hideAllFrequencyContainers() {
        containerEveryXDay.visibility = View.GONE
        containerDaysOfWeek.visibility = View.GONE
        containerSpecificDates.visibility = View.GONE
    }
}
