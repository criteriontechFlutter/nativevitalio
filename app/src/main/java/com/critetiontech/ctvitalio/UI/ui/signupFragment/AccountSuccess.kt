package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R


import com.critetiontech.ctvitalio.databinding.FragmentAccountSuccessBinding
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel


class AccountSuccess : Fragment() {

    private lateinit var binding: FragmentAccountSuccessBinding

    private lateinit var progressViewModel: ProgressViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]

        // Handle the "Set Preferences" button click
        binding.btnSetPreferences.setOnClickListener {
            progressViewModel.updateProgress(11)
            findNavController().navigate(R.id.action_accountSuccess_to_setPreferences)
        }
    }
}