package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentCompletionScreenBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CompletionScreen : Fragment() {


        private lateinit var binding: FragmentCompletionScreenBinding
        private lateinit var viewModel: RegistrationViewModel

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            binding = FragmentCompletionScreenBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]

            lifecycleScope.launch {
                delay(2000) // 2000 ms = 2 seconds
                findNavController().navigate(R.id.action_completionScreen_to_completionDashboardReady)
            }
        }

    }