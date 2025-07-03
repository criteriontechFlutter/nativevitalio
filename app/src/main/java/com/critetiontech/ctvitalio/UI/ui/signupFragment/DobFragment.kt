package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentDobBinding
import java.util.Calendar

class DobFragment : Fragment() {
    private lateinit var binding: FragmentDobBinding
    private lateinit var viewModel: RegistrationViewModel
    private lateinit var progressViewModel: ProgressViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDobBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]

        // ✅ Restore DOB if already selected
        viewModel.dob.value?.let { dobString ->
            val parts = dobString.split("-")
            if (parts.size == 3) {
                val day = parts[0].toIntOrNull() ?: return@let
                val month = parts[1].toIntOrNull()?.minus(1) ?: return@let // 0-based index
                val year = parts[2].toIntOrNull() ?: return@let
                binding.datePicker.updateDate(year, month, day)
            }
        }

        binding.btnNext.setOnClickListener {
            val day = binding.datePicker.dayOfMonth
            val month = binding.datePicker.month + 1 // 1-based for display
            val year = binding.datePicker.year

            val selectedCalendar = Calendar.getInstance().apply {
                set(year, binding.datePicker.month, day)
            }

            val today = Calendar.getInstance()
            if (selectedCalendar.after(today)) {
                Toast.makeText(requireContext(), "Please select a valid date of birth", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dob = "$day-$month-$year" // ✅ stored in val
            viewModel.dob.value = dob

            progressViewModel.updateProgress(4)
            findNavController().navigate(R.id.action_dobFragment_to_bloodGroupFragment)
        }
    }
}