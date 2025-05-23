package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.UI.Home
import com.critetiontech.ctvitalio.databinding.FragmentCompletionDashboardReadyBinding

class CompletionDashboardReady : Fragment() {

    private lateinit var binding: FragmentCompletionDashboardReadyBinding
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCompletionDashboardReadyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]

        binding.btnGoToDashboard.setOnClickListener(){

            val intent = Intent(requireContext(), Home::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            requireContext().startActivity(intent)
        }

    }

}