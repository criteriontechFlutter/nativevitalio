package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentSignUp2Binding

class SignUpFragment2 : Fragment() {
    private lateinit var binding: FragmentSignUp2Binding
    private lateinit var progressViewModel: ProgressViewModel
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUp2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]

        // ✅ Restore existing values if already entered
        binding.etFirstName.setText(viewModel.firstName.value ?: "")
        binding.etLastName.setText(viewModel.lastName.value ?: "")

        binding.btnNext.setOnClickListener {
            val first = binding.etFirstName.text.toString().trim()
            val last = binding.etLastName.text.toString().trim()

            if (first.isEmpty()) {
                binding.etFirstName.error = "First name is required"
                return@setOnClickListener
            }

            // ✅ Save values in ViewModel
            viewModel.firstName.value = first
            viewModel.lastName.value = last

            // ✅ Update progress
            progressViewModel.updateProgress(1)

            // ✅ Navigate to next fragment
            findNavController().navigate(R.id.action_nameFragment_to_genderFragment)
        }
    }
}