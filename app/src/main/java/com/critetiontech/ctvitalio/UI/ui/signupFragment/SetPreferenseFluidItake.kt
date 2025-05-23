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

        binding.btnNext.setOnClickListener {
            val fluidText = binding.fluidIntakeInput.text.toString()
            val unit = binding.unitSpinner.selectedItem.toString()

            if (fluidText.isNotEmpty()) {
                try {
                    val amount = fluidText.toFloat()
                    viewModel.setFluidIntake(amount, unit)

                    viewModel.patientParameterSettingInsert()
                    findNavController().navigate(R.id.action_setPreferenseFluidItake_to_completionScreen)
                } catch (e: NumberFormatException) {
                    binding.fluidIntakeInput.error = "Enter a valid number"
                }
            } else {
                binding.fluidIntakeInput.error = "This field cannot be empty"
            }
        }
    }

}