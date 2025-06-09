package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentSetPreferenseFluidItakeBinding

class SetPreferenseFluidItake : Fragment() {


    private lateinit var binding: FragmentSetPreferenseFluidItakeBinding
    private lateinit var viewModel: RegistrationViewModel
    private lateinit var progressViewModel: ProgressViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSetPreferenseFluidItakeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]

        binding.btnNext.setOnClickListener {
            val fluidText = binding.fluidIntakeInput.text.toString()
//            val unit = binding.unitSpinner.selectedItem.toString()

            progressViewModel.updateProgress(15)
            progressViewModel.updateProgressPage(2)

            val amount = if (fluidText.isEmpty()) {
                0f
            } else {
                // Use a try-catch to handle any potential exceptions when converting to float
                try {
                    fluidText.toFloat()
                } catch (e: NumberFormatException) {
                    0f  // If parsing fails, default to 0f
                }
            }

            viewModel.setFluidIntake(amount, "Litre")
                    viewModel.patientParameterSettingInsert()
                    findNavController().navigate(R.id.action_setPreferenseFluidItake_to_completionScreen)

        }
    }

}