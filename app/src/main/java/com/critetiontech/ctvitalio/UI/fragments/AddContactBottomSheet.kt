package com.critetiontech.ctvitalio.UI.fragments

import android.R
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.critetiontech.ctvitalio.databinding.BottomSheetAddEmergencyContactBinding
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.critetiontech.ctvitalio.viewmodel.EmergencyContactViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddContactBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddEmergencyContactBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EmergencyContactViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddEmergencyContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val relationships = listOf("Father", "Mother", "Brother", "Sister", "Self")
        binding.spinnerRelationship.adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_dropdown_item,
            relationships
        )
        val nameFilter = InputFilter { source, _, _, _, _, _ ->
            val regex = Regex("[a-zA-Z ]") // Allow letters and space only
            if (source.isEmpty()) return@InputFilter null // Allow backspace

            val filtered = source.filter { it.toString().matches(regex) }
            if (filtered == source) null else filtered
        }
        binding.etName.filters = arrayOf(nameFilter)
        binding.btnSaveContact.setOnClickListener {
            val name = binding.etName.text.toString()
            val phone = binding.etContactNumber.text.toString()
            val relation = binding.spinnerRelationship.selectedItem.toString()

            if (name.isBlank() || phone.isBlank()) {
                ToastUtils.showInfo(MyApplication.appContext, "All fields required")
                return@setOnClickListener
            }
            viewModel.saveEmergencyContact(name,phone,relation)
            ToastUtils.showSuccess(MyApplication.appContext, "Contact saved: $name ($relation)")
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
