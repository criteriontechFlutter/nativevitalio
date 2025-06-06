package com.critetiontech.ctvitalio.UI.fragments

import android.R
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.critetiontech.ctvitalio.databinding.BottomSheetAddEmergencyContactBinding
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.critetiontech.ctvitalio.viewmodel.EmergencyContactViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.graphics.Color
import android.widget.AdapterView

import android.widget.Toast;
class AddContactBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetAddEmergencyContactBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EmergencyContactViewModel by viewModels()

    private var selectedPosition = 0 // default to 0 (hint)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddEmergencyContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Add hint as first item
        val relationships = listOf("Select Relationship", "Father", "Mother", "Brother", "Sister", "Self")

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            relationships
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                // Show hint in gray color for position 0
                if (position == 0) {
                    view.setTextColor(Color.GRAY)
                } else {
                    view.setTextColor(Color.BLACK)
                }
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                // Optionally, you can disable the hint item in dropdown:
                if (position == 0) {
                    view.setTextColor(Color.GRAY)
                } else {
                    view.setTextColor(Color.BLACK)
                }
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRelationship.adapter = adapter

        // Set default selected item to 0 (hint)
        binding.spinnerRelationship.setSelection(0)

        binding.spinnerRelationship.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedPosition = 0
            }
        }

        binding.btnSaveContact.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etContactNumber.text.toString().trim()

            // Validate inputs and ensure relationship other than hint is selected
            if (name.isBlank() || phone.isBlank() || selectedPosition == 0) {


                Toast.makeText(requireContext(), "All fields are required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val relation = relationships[selectedPosition]
            viewModel.saveEmergencyContact(name, phone, relation)
            Toast.makeText(requireContext(), "Contact saved: $name ($relation)", Toast.LENGTH_SHORT).show()

             dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}