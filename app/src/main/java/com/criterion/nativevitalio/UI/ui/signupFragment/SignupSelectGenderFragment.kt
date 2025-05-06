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

class SignupSelectGenderFragment : Fragment() {
    private lateinit var binding: FragmentSignupSelectGenderBinding
    private var selectedGender: String? = null

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

        setupGenderSelection()

        progressViewModel = ViewModelProvider(this)[ProgressViewModel::class.java]
        binding.btnNext.setOnClickListener {
            progressViewModel.updateProgress(2)
            Toast.makeText(requireContext(), "Selected: $selectedGender", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_genderFragment_to_dobFragment)
        }
    }

    private fun setupGenderSelection() {
        val options = mapOf(
            binding.layoutMale to "Male",
            binding.layoutFemale to "Female",
            binding.layoutOther to "Other"
        )

        fun updateSelection(selected: View) {
            options.keys.forEach {
                it.setBackgroundResource(
                    if (it == selected)
                        R.drawable.gender_card_unselected  // Highlight selected card
                    else
                        R.drawable.gender_card_selected  // Dim others
                )
            }
            selectedGender = options[selected]
            binding.btnNext.isEnabled = true
            binding.btnNext.setBackgroundResource(R.drawable.rounded_corners)
        }

        options.keys.forEach { view ->
            view.setOnClickListener { updateSelection(view) }
        }
    }
}
