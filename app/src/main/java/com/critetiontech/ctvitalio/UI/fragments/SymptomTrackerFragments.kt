package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.SymptomsTrackerAdapter
import com.critetiontech.ctvitalio.databinding.FragmentSymptomTrackerFragmentsBinding
import com.critetiontech.ctvitalio.model.SymptomDetail
import com.critetiontech.ctvitalio.viewmodel.SymptomsTrackerViewModel

class SymptomTrackerFragments : Fragment() {


    private lateinit var binding: FragmentSymptomTrackerFragmentsBinding
    private val viewModel: SymptomsTrackerViewModel by viewModels()

    private val symptomList = mutableListOf<SymptomDetail>()
    private var currentIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSymptomTrackerFragmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerViewSymptoms.layoutManager = LinearLayoutManager(requireContext())

        observeViewModel()
        viewModel.getSymptoms()

        binding.historyText.setOnClickListener(){
            findNavController().navigate(R.id.action_symptomTrackerFragments_to_symptomHistory)
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.symptomList.observe(viewLifecycleOwner) {
            symptomList.clear()
            symptomList.addAll(it)
            currentIndex = 0
            showCurrentSymptom()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCurrentSymptom() {
        if (currentIndex in symptomList.indices) {
            val symptom = symptomList[currentIndex]

            val adapter = SymptomsTrackerAdapter(
                symptom = symptom,
                currentIndex = currentIndex,
                totalCount = symptomList.size,
                isLastItem = currentIndex == symptomList.size - 1,
                onYesClicked = {
                    symptom.selection = 1
                    if (currentIndex == symptomList.size - 1) {

                        val selected = symptomList.filter { it.selection == 1 }
                        viewModel.insertSymptoms(findNavController(),requireContext(),selected)                    }

                    else   if (currentIndex < symptomList.size - 1) {
                        goToNext()                    }


                },
                onNoClicked = {
                    symptom.selection = 0
                    if (currentIndex == symptomList.size - 1) {

                        val selected = symptomList.filter { it.selection == 1 }
                        viewModel.insertSymptoms(findNavController(),requireContext(),selected)                    }
                    else   if (currentIndex < symptomList.size - 1) {
                        goToNext()                    }
                },
                onBackClicked = {
                    if (currentIndex > 0) {
                        currentIndex--
                        showCurrentSymptom()
                    }
                }
            )

            binding.recyclerViewSymptoms.adapter = adapter

            // Show Update button only on the last item


            // Always show back button if not on first item
            binding.backButton.visibility = if (currentIndex > 0) View.VISIBLE else View.GONE

        } else {
            showSummary()
        }
    }

    private fun goToNext() {
        currentIndex++
        showCurrentSymptom()
    }

    private fun showSummary() {
        val selected = symptomList.filter { it.selection == 1 }
      //  Toast.makeText(requireContext(), "Selected: ${selected.size}", Toast.LENGTH_SHORT).show()
    }
}