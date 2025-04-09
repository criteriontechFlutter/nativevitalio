package com.criterion.nativevitalio.UI.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.adapter.SymptomsTrackerAdapter
import com.criterion.nativevitalio.databinding.FragmentSymptomTrackerFragmentsBinding
import com.criterion.nativevitalio.model.SymptomDetail
import com.criterion.nativevitalio.viewmodel.SymptomsTrackerViewModel

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
    }

    private fun observeViewModel() {
        viewModel.symptomList.observe(viewLifecycleOwner) { list ->
            symptomList.clear()
            symptomList.addAll(list)
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
                onYesClicked = {
                    symptom.selection = 1
                    goToNext()
                },
                onNoClicked = {
                    symptom.selection = 0
                    goToNext()
                },
                onBackClicked = {
                    if (currentIndex > 0) {
                        currentIndex--
                        showCurrentSymptom()
                    }
                }
            )

            binding.recyclerViewSymptoms.adapter = adapter
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
//        Toast.makeText(requireContext(), "Selected: ${selected.joinToString { it.details }}", Toast.LENGTH_LONG).show()
        // Or: navigate to result screen or submit to server
    }
}