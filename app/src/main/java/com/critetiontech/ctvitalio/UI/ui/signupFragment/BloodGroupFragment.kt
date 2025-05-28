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
import com.critetiontech.ctvitalio.adapter.BloodGroupAdapter
import com.critetiontech.ctvitalio.model.BloodGroup
import com.critetiontech.ctvitalio.viewmodel.RegistrationViewModel
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.ui.signupFragment.ProgressViewModel
import com.critetiontech.ctvitalio.databinding.FragmentBloodGroupBinding

class BloodGroupFragment : Fragment() {


    private lateinit var binding: FragmentBloodGroupBinding
    private var selectedBloodGroup: BloodGroup? = null

    private val bloodGroups = listOf(
        BloodGroup(1, "A+", 1, 1, "2023-05-02T11:22:58"),
        BloodGroup(2, "A-", 1, 1, "2023-05-30T12:52:37"),
        BloodGroup(3, "B+", 1, 1, "2023-05-30T12:52:56"),
        BloodGroup(4, "B-", 1, 1, "2023-05-31T13:59:25"),
        BloodGroup(6, "O+", 1, 1, "2023-09-05T12:23:47"),
        BloodGroup(7, "O-", 1, 1, "2023-09-05T12:23:56"),
        BloodGroup(8, "AB+", 1, 1, "2023-09-05T12:24:06"),
        BloodGroup(9, "AB-", 1, 1, "2023-09-05T12:24:11")
    )

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

        // Load the selected blood group from the ViewModel
        val preSelectedGroup = bloodGroups.find { it.groupName == viewModel.bg.value }

        val adapter = BloodGroupAdapter(
            bloodGroups,
            selected = preSelectedGroup // Use pre-selected group from ViewModel
        ) { selected ->
            selectedBloodGroup = selected
            viewModel.bg.value = selected.groupName.toString()
            viewModel.bgId.value = selected.id.toString()
        }

        binding.rvBloodGroups.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvBloodGroups.adapter = adapter

        binding.btnNext.setOnClickListener {
            if (selectedBloodGroup != null) {
                progressViewModel.updateProgress(4)
                findNavController().navigate(R.id.action_bloodGroupFragment_to_adressFragment)
            } else {
                Toast.makeText(requireContext(), "Please select a blood group", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Restore the previously selected blood group when coming back to this fragment
        val preSelectedGroup = bloodGroups.find { it.groupName == viewModel.bg.value }
        selectedBloodGroup = preSelectedGroup
    }
}