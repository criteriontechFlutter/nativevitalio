package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
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
            progressViewModel.updatepageNo(2)
//            findNavController().navigate(R.id.action_genderFragment_to_dobFragment)
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
            }
        }
    }

    private fun highlightSelectedCard(selected: View) {
        val allLayouts = listOf(binding.layoutMale, binding.layoutFemale, binding.layoutOther)
        allLayouts.forEach {
            val textView = when (it) {
                binding.layoutMale -> binding.tvMale
                binding.layoutFemale -> binding.tvFemale
                else -> binding.tvOther
            }

            // Change background and text color
            if (it == selected) {
                it.setBackgroundResource(R.drawable.bg_selected)  // selected background
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))  // selected text color
            } else {
                it.setBackgroundResource(R.drawable.bg_unselected)  // unselected background
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.bgDark))  // unselected text color
            }
        }
    }
}