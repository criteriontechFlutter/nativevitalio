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
import com.critetiontech.ctvitalio.databinding.FragmentSignupSelectGenderBinding

class SignupSelectGenderFragment : Fragment() {

    private lateinit var binding: FragmentSignupSelectGenderBinding
    private var selectedGender: String? = null

    private lateinit var viewModel: RegistrationViewModel
    private lateinit var progressViewModel: ProgressViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignupSelectGenderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]

        setupGenderSelection()

        binding.tvDescription.text = "Hi ${viewModel.firstName.value?.replaceFirstChar { it.uppercaseChar() } ?: "Guest"}, let us know if you are male or female."
        // ✅ Restore previously selected gender
        selectedGender = viewModel.gender.value
        selectedGender?.let {
            when (it) {
                "Male" -> highlightSelectedCard(binding.layoutMale)
                "Female" -> highlightSelectedCard(binding.layoutFemale)
                "Other" -> highlightSelectedCard(binding.layoutOther)
            }
            binding.btnNext.isEnabled = true
            binding.btnNext.setBackgroundResource(R.drawable.rounded_corners)
        }

        binding.btnNext.setOnClickListener {
            if (selectedGender.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Please select a gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.gender.value = selectedGender
            viewModel.genderId.value = when (selectedGender) {
                "Male" -> "1"
                "Female" -> "2"
                "Other" -> "3"
                else -> ""
            }

            progressViewModel.updateProgress(2)
            findNavController().navigate(R.id.action_genderFragment_to_dobFragment)
        }
    }

    private fun setupGenderSelection() {
        val genderMap = mapOf(
            binding.layoutMale to Pair("Male", "1"),
            binding.layoutFemale to Pair("Female", "2"),
            binding.layoutOther to Pair("Other", "3")
        )

        genderMap.keys.forEach { layout ->
            layout.setOnClickListener {
                highlightSelectedCard(layout)
                selectedGender = genderMap[layout]?.first
                viewModel.gender.value = selectedGender
                viewModel.genderId.value = genderMap[layout]?.second

                binding.btnNext.isEnabled = true
                binding.btnNext.setBackgroundResource(R.drawable.rounded_corners)
            }
        }
    }

    private fun highlightSelectedCard(selected: View) {
        val allLayouts = listOf(binding.layoutMale, binding.layoutFemale, binding.layoutOther)
        allLayouts.forEach {
            it.setBackgroundResource(
                if (it == selected)
                    R.drawable.gender_card_unselected  // selected background
                else
                    R.drawable.gender_card_selected    // unselected background
            )
        }
    }
}