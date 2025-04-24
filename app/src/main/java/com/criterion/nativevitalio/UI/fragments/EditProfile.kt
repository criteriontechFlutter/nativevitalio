package com.criterion.nativevitalio.UI.fragments

import PrefsManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.critetiontech.ctvitalio.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditProfile : Fragment() {

    private lateinit var firstNameField: EditText
    private lateinit var lastNameField: EditText
    private lateinit var dobField: EditText
    private lateinit var radioMale: RadioButton
    private lateinit var radioFemale: RadioButton
    private lateinit var genderGroup: RadioGroup
    private lateinit var updateButton: Button

    private val prefsManager by lazy { PrefsManager() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        firstNameField = view.findViewById(R.id.firstNameField)
        lastNameField = view.findViewById(R.id.lastNameField)
        dobField = view.findViewById(R.id.dobField)
        radioMale = view.findViewById(R.id.radioMale)
        radioFemale = view.findViewById(R.id.radioFemale)
        genderGroup = view.findViewById(R.id.genderGroup)
        updateButton = Button(requireContext()).apply { text = "Update" }

        // Add button dynamically (optional)
        (view as ViewGroup).addView(updateButton)

        bindPatientData()

        updateButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
//                updateUserData(
//                    requireContext(),
//                    prefsManager,
//                    filePath = null // Or image path if file needs to be uploaded
//                )
            }
        }

        return view
    }

    private fun bindPatientData() {
        prefsManager.getPatient()?.let { patient ->
            val nameParts = patient.patientName.split(" ")
            firstNameField.setText(nameParts.getOrNull(0) ?: "")
            lastNameField.setText(nameParts.getOrNull(1) ?: "")
            dobField.setText(patient.dob)

            if (patient.gender.lowercase() == "male") {
                radioMale.isChecked = true
            } else if (patient.gender.lowercase() == "female") {
                radioFemale.isChecked = true
            }
        }
    }
}