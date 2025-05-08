package com.criterion.nativevitalio.UI.ui.signupFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentSignupSelectGenderBinding
import com.criterion.nativevitalio.viewmodel.RegistrationViewModel

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

        // ✅ Restore selected gender if returning to this screen
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
            progressViewModel.updateProgress(2)

            findNavController().navigate(R.id.action_genderFragment_to_dobFragment)
        }
    }

    private fun setupGenderSelection() {
        val options = mapOf(
            binding.layoutMale to "Male",
            binding.layoutFemale to "Female",
            binding.layoutOther to "Other"
        )

        options.keys.forEach { view ->
            view.setOnClickListener {
                highlightSelectedCard(view)
                selectedGender = options[view]
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
                    R.drawable.gender_card_unselected // selected background
                else
                    R.drawable.gender_card_selected   // unselected background
            )
        }
    }
}