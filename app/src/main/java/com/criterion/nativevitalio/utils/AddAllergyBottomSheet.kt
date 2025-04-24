package com.criterion.nativevitalio.utils

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.criterion.nativevitalio.model.AllergyTypeItem
import com.criterion.nativevitalio.viewmodel.AllergiesViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.BottomsheetAddAllergyBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AddAllergyBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: BottomsheetAddAllergyBinding
    private var allergyTypes: List<AllergyTypeItem> = emptyList()
    private var selectedSeverity: String = ""

    private val viewModel: AllergiesViewModel by activityViewModels()
    override fun getTheme(): Int = R.style.BottomSheetDialogTheme
    companion object {
        private const val ARG_ALLERGY_LIST = "allergy_type_list"

        fun newInstance(list: List<AllergyTypeItem>): AddAllergyBottomSheet {
            val fragment = AddAllergyBottomSheet()
            val bundle = Bundle()
            bundle.putString(ARG_ALLERGY_LIST, Gson().toJson(list))
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(ARG_ALLERGY_LIST)?.let { json ->
            val type = object : TypeToken<List<AllergyTypeItem>>() {}.type
            allergyTypes = Gson().fromJson(json, type)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomsheetAddAllergyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Populate allergy type spinner
        val allergyTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            allergyTypes.map { it.parameterName }
        )
        allergyTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSubstanceType.adapter = allergyTypeAdapter

        // Severity buttons
        binding.btnMild.setOnClickListener { selectSeverity("Mild") }
        binding.btnModerate.setOnClickListener { selectSeverity("Moderate") }
        binding.btnSevere.setOnClickListener { selectSeverity("Severe") }

        // Save button
        binding.btnAddAllergy.setOnClickListener {
            val substance = binding.inputSubstance.text.toString()
            val reaction = binding.inputReaction.text.toString()
            val selectedTypeIndex = binding.spinnerSubstanceType.selectedItemPosition
            val selectedType = allergyTypes.getOrNull(selectedTypeIndex)

            if (substance.isBlank() || reaction.isBlank() || selectedSeverity.isBlank() || selectedType == null) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.savePatientAllergies(
                parameterStatement = "Self",
                substance = substance,
                reaction = reaction,
                severity = selectedSeverity,
                historyParameterAssignId = selectedType.historyParameterAssignId.toString(),
                onSuccess = {
                    Toast.makeText(requireContext(), "Allergy added successfully!", Toast.LENGTH_SHORT).show()
                    viewModel.getAllergies() // Refresh list
                    dismiss()
                },
                onError = {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    private fun selectSeverity(severity: String) {
        selectedSeverity = severity

        // Reset tint
        binding.btnMild.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
        binding.btnModerate.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)
        binding.btnSevere.backgroundTintList = ColorStateList.valueOf(Color.LTGRAY)

        val selectedColor = when (severity) {
            "Mild" -> Color.parseColor("#FFA500")
            "Moderate" -> Color.parseColor("#FF5722")
            "Severe" -> Color.parseColor("#FF0000")
            else -> Color.LTGRAY
        }

        when (severity) {
            "Mild" -> binding.btnMild.backgroundTintList = ColorStateList.valueOf(selectedColor)
            "Moderate" -> binding.btnModerate.backgroundTintList = ColorStateList.valueOf(selectedColor)
            "Severe" -> binding.btnSevere.backgroundTintList = ColorStateList.valueOf(selectedColor)
        }
    }
}