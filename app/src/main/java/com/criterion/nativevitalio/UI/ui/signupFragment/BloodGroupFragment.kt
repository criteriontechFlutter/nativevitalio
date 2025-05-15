package com.critetiontech.ctvitalio.UI.ui.signupFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.BloodGroupAdapter
import com.critetiontech.ctvitalio.databinding.FragmentBloodGroupBinding
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel

class BloodGroupFragment : Fragment() {

    private lateinit var binding: FragmentBloodGroupBinding
    private var selectedBloodGroup: String? = null
    private val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    private lateinit var progressViewModel: ProgressViewModel
    private lateinit var viewModel: RegistrationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBloodGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[RegistrationViewModel::class.java]
        progressViewModel = ViewModelProvider(requireActivity())[ProgressViewModel::class.java]
        selectedBloodGroup = viewModel.bg.value
        val adapter = BloodGroupAdapter(
            bloodGroups,
            selected = viewModel.bg.value         // restore selection from ViewModel
        ) { selected ->
            selectedBloodGroup = selected
            viewModel.bg.value = selected
        }


        binding.rvBloodGroups.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvBloodGroups.adapter = adapter

        binding.btnNext.setOnClickListener {
            if (!selectedBloodGroup.isNullOrEmpty()) {
                viewModel.bg.value = selectedBloodGroup
                progressViewModel.updateProgress(4)
                findNavController().navigate(R.id.action_bloodGroupFragment_to_adressFragment)
            } else {
                Toast.makeText(requireContext(), "Please select a blood group", Toast.LENGTH_SHORT).show()
            }
        }
    }
}