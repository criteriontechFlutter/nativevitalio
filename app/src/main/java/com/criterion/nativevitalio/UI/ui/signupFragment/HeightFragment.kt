package com.criterion.nativevitalio.UI.ui.signupFragment

import DateUtils.showHeightPicker
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

import com.criterion.nativevitalio.viewmodel.RegistrationViewModel
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

        // ✅ Restore previously saved height
        binding.etHeight.setText(viewModel.ht.value ?: "")

        binding.btnNext.setOnClickListener {
            viewModel.ht.value = binding.etHeight.text.toString()
            progressViewModel.updateProgress(4)
            findNavController().navigate(R.id.action_heightFragment_to_chronicConditionFragment)
        }

        binding.etHeight.apply {
            isFocusable = false
            isClickable = true
        }

        binding.etHeight.setOnClickListener {
            showHeightPicker(requireContext()) { selectedHeight ->
                binding.etHeight.setText(selectedHeight)
            }
        }
    }
}