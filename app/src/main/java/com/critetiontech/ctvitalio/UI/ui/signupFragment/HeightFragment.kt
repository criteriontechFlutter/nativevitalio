package com.critetiontech.ctvitalio.UI.ui.signupFragment

import DateUtils.showHeightPicker
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.ui.signupFragment.ProgressViewModel
import com.critetiontech.ctvitalio.databinding.FragmentHeightBinding

class HeightFragment : Fragment() {

    private lateinit var binding: FragmentHeightBinding
    private lateinit var progressViewModel: ProgressViewModel
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHeightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]
        // Restore previously saved height
        binding.etHeight.setText(viewModel.ht.value ?: "")

        binding.etHeight.apply {
            isFocusable = false
            isClickable = true
        }

        binding.etHeight.setOnClickListener {
            showHeightPicker(requireContext()) { selectedHeight ->
                var heightInCm = 0.0
                var displayText = selectedHeight // default

                when {
                    selectedHeight.contains("cm") -> {
                        heightInCm = selectedHeight.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
                        displayText = "$heightInCm cm"
                    }
                    selectedHeight.contains("in") && !selectedHeight.contains("'") -> {
                        val inches = selectedHeight.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
                        heightInCm = inches * 2.54
                        displayText = "${inches.toInt()} in"
                    }
                    selectedHeight.contains("'") -> {
                        val regex = Regex("""(\d+)' (\d+)\"""")
                        val match = regex.find(selectedHeight)
                        if (match != null) {
                            val (feet, inches) = match.destructured
                            heightInCm = feet.toInt() * 30.48 + inches.toInt() * 2.54
                            displayText = "$feet' $inches\" ft"
                        }
                    }
                }

                // ✅ Show original value in EditText
                binding.etHeight.setText(displayText)

                // ✅ Store converted value (cm) for backend/API
                viewModel.htInCm.value = String.format("%.1f", heightInCm)

                // ✅ Store exact selected display (e.g., "5' 8\" ft" or "170 cm")
                viewModel.ht.value = displayText
            }
        }

        binding.btnNext.setOnClickListener {

            progressViewModel.updateProgress(7)
            progressViewModel.updatepageNo(7)
            findNavController().navigate(R.id.action_heightFragment_to_chronicConditionFragment)
        }
    }
}