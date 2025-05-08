package com.critetiontech.ctvitalio.UI.fragments

import PrefsManager
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.databinding.FragmentEditProfileBinding
import com.critetiontech.ctvitalio.viewmodel.EditProfileViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditProfile : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private val prefsManager by lazy { PrefsManager() }
    private lateinit var viewModel: EditProfileViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind local patient data
        bindPatientData()

        viewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]
        binding.dobField.setOnClickListener {
            showDatePicker()
        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        // Handle update button click
        binding.updateProfileButton.setOnClickListener {
            val name = "${binding.firstNameField.text} ${binding.lastNameField.text}"
            val phone = prefsManager.getPatient()?.mobileNo.toString()
            val email = prefsManager.getPatient()?.emailID.toString()
            val address = prefsManager.getPatient()?.address.toString()
            val genderId = if (binding.radioMale.isChecked) "1" else "2"
            val height = prefsManager.getPatient()?.height.toString()
            val weight = prefsManager.getPatient()?.weight.toString()

            val rawDob = binding.dobField.text.toString()  // Expected format: yyyy-MM-dd


            val inputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) // ✅ This is the correct pattern
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val convertedDob = try {
                val parsedDate = inputFormat.parse(rawDob)
                outputFormat.format(parsedDate!!)
            } catch (e: Exception) {
                e.printStackTrace()
                rawDob // fallback
            }

            viewModel.updateUserData(
                requireContext(),
                filePath = null,
                name = name,
                phone = phone,
                email = email,
                dob = convertedDob,
                address = address,
                genderId = genderId,
                height = height,
                weight = weight
            )
        }
    }

    private fun bindPatientData() {
        prefsManager.getPatient()?.let { patient ->
            val nameParts = patient.patientName.split(" ")
            binding.firstNameField.setText(nameParts.getOrNull(0) ?: "")
            binding.lastNameField.setText(nameParts.getOrNull(1) ?: "")

            // Format date before showing
            val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) // Example: 20 May 2025

            val formattedDob = try {
                val parsedDate = inputFormat.parse(patient.dob)
                displayFormat.format(parsedDate!!)
            } catch (e: Exception) {
                e.printStackTrace()
                patient.dob // fallback
            }

            binding.dobField.setText(formattedDob)

            val gender = patient.genderId
            when (gender) {
                "1" -> binding.radioMale.isChecked = true
                "2" -> binding.radioFemale.isChecked = true
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        // Try to parse the current DOB and pre-select in picker
        val currentDob = binding.dobField.text.toString()
        val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        try {
            val date = displayFormat.parse(currentDob)
            if (date != null) {
                calendar.time = date
            }
        } catch (_: Exception) {}

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
            val formattedDate = displayFormat.format(selectedCalendar.time)
            binding.dobField.setText(formattedDate)
        }, year, month, day)

        datePicker.datePicker.maxDate = System.currentTimeMillis() // Optional: prevent future DOB
        datePicker.show()
    }
}