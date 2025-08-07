package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.ui.signupFragment.ProgressViewModel
import com.critetiontech.ctvitalio.databinding.FragmentAddYourPhotoBinding
import com.critetiontech.ctvitalio.databinding.FragmentWeightBinding

class AddYourPhoto : Fragment() {
    private lateinit var binding: FragmentAddYourPhotoBinding
    private lateinit var progressViewModel: ProgressViewModel
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddYourPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the Spinner
//        val items = listOf("Kg" )  // Units list
//        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, items)
//        adapter.setDropDownViewResource(R.layout.spinner_item)
//        binding.spinnerUnit.adapter = adapter  // Bind the adapter to the spinner

        // Get the ViewModel instances

        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]
        // Restore previously entered weight if any

        binding.btnNext.setOnClickListener(){

            progressViewModel.updateProgress(10)
            findNavController().navigate(R.id.action_addYourPhoto_to_setYourOwnGoal)
        }
    }
}