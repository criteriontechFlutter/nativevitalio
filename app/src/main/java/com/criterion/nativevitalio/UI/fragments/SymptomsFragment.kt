package com.criterion.nativevitalio.UI.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.criterion.nativevitalio.utils.LoaderUtils.hideLoading
import com.criterion.nativevitalio.utils.LoaderUtils.showLoading
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.adapter.SymptomAdapter
import com.criterion.nativevitalio.databinding.FragmentSymtomsBinding
import com.criterion.nativevitalio.interfaces.AdapterInterface
import com.criterion.nativevitalio.model.ProblemWithIcon
import com.criterion.nativevitalio.viewmodel.SymptomsViewModel

class SymptomsFragment : Fragment() {

    private lateinit var binding: FragmentSymtomsBinding
    private lateinit var viewModel: SymptomsViewModel
    private lateinit var symptomAdapter: SymptomAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSymtomsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[SymptomsViewModel::class.java]

        setupRecyclerViews()
        setupSearchInputListener()
        observeLiveData()

        viewModel.getAllPatientMedication()
        viewModel.getAllSuggestedProblem()
        viewModel.getSymptoms()

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }
        binding.saveSymptomsBtn.setOnClickListener(){
            viewModel.insertSymptoms(findNavController(),requireContext());
        }

        binding.backButton.setOnClickListener(){

            findNavController().popBackStack()
        }

        binding.historyButton.setOnClickListener {
            findNavController().navigate(R.id.action_symptomsFragment_to_symptomHistory)

        }

        binding.saveSymptomsBtn.setOnClickListener {
            val selected = viewModel.selectedSymptoms.value.orEmpty()
            val searched = viewModel.searchSelectedSymptomList.value.orEmpty()

            if (selected.isEmpty() && searched.isEmpty()) {
                findNavController().navigate(R.id.action_symptomsFragment_to_symptomTrackerFragments)
            } else {
                viewModel.insertSymptoms(findNavController(), requireContext())

                // OR: If you want to navigate immediately (not recommended if insert fails)
                // findNavController().navigate(R.id.action_symptomsFragment_to_symptomTrackerFragments)
            }
//            findNavController().navigate(R.id.action_symptomsFragment_to_symptomTrackerFragments)

        }
    }

    private fun setupRecyclerViews() {
        binding.symptomRecycler.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.searchSuggestionList.layoutManager = GridLayoutManager(requireContext(), 1)

        // Initial empty adapter
        symptomAdapter = SymptomAdapter(
            items = emptyList(),
            selectedItems = viewModel.searchSelectedSymptomList.value ?: emptyList(),
            listener = object : AdapterInterface<ProblemWithIcon> {
                override fun onClick(position: Int, data: ProblemWithIcon) {
                    binding.searchSymptom.setText(data.problemName)
                    binding.searchSuggestionList.visibility = View.GONE
                    viewModel.toggleSymptomSelection(data, isFromSearch = true)
                }
            },
            isSearchMode = true
        )
        binding.searchSuggestionList.adapter = symptomAdapter
    }

    private fun setupSearchInputListener() {
        binding.searchSymptom.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim().orEmpty()
                if (query.length >= 2) {
                    viewModel.getAllProblems(query)
                } else {
                    binding.searchSuggestionList.visibility = View.GONE
                    viewModel.clearSearchResults()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun observeLiveData() {
        // Grid symptoms
        viewModel.symptomList.observe(viewLifecycleOwner) { symptoms ->
            viewModel.selectedSymptoms.observe(viewLifecycleOwner) { selected ->
                binding.symptomRecycler.adapter = SymptomAdapter(symptoms, selected, object : AdapterInterface<ProblemWithIcon> {
                    override fun onClick(position: Int, data: ProblemWithIcon) {
                        viewModel.toggleSymptomSelection(data)
                    }
                })
            }
        }

        // Chips for suggested problems
//        viewModel.moreSymptomList.observe(viewLifecycleOwner) { moreSymptoms ->
//            binding.moreSymptoms.removeAllViews()
//            moreSymptoms.forEach { symptom ->
//                val chipBinding = SymptomChipBinding.inflate(layoutInflater)
//                chipBinding.symptomName = symptom
//                val chip = chipBinding.chipText
//
//                val isSelected = viewModel.selectedSymptoms.value?.contains(symptom) == true
//
//                chip.setBackgroundResource(
//                    if (isSelected) R.drawable.chip_background_selected
//                    else R.drawable.chip_background_unselected
//                )
//                chip.setTextColor(
//                    ContextCompat.getColor(
//                        requireContext(),
//                        if (isSelected) android.R.color.white else R.color.terms_color
//                    )
//                )
//
//                chip.setOnClickListener {
//                    viewModel.toggleSymptomSelection(symptom)
//                    val reselected = viewModel.selectedSymptoms.value?.contains(symptom) == true
//                    chip.setBackgroundResource(
//                        if (reselected) R.drawable.chip_background_selected
//                        else R.drawable.chip_background_unselected
//                    )
//                    chip.setTextColor(
//                        ContextCompat.getColor(
//                            requireContext(),
//                            if (reselected) android.R.color.white else R.color.terms_color
//                        )
//                    )
//
//
//                }
//
//                binding.moreSymptoms.addView(chip)
//            }
//        }

        // Search result list
        viewModel.searchSymptomList.observe(viewLifecycleOwner) { results ->
            if (results.isNullOrEmpty()) {
                binding.searchSuggestionList.visibility = View.GONE
            } else {
                binding.searchSuggestionList.visibility = View.VISIBLE

                symptomAdapter = SymptomAdapter(
                    items = results,
                    selectedItems = viewModel.searchSelectedSymptomList.value ?: listOf(),
                    listener = object : AdapterInterface<ProblemWithIcon> {
                        override fun onClick(position: Int, data: ProblemWithIcon) {
                            // ✅ Select item
                            viewModel.toggleSymptomSelection(data, isFromSearch = true)

                            // ✅ Clear search input
                            binding.searchSymptom.setText("")

                            // ✅ Hide suggestion list
                            binding.searchSuggestionList.visibility = View.GONE

                            // ✅ Clear list from ViewModel
                            viewModel.clearSearchResults()
                        }
                    },
                    isSearchMode = true
                )
                binding.searchSuggestionList.adapter = symptomAdapter
            }
        }

        // Chips for selected search items (Flexbox layout)
        viewModel.searchSelectedSymptomList.observe(viewLifecycleOwner) { selectedSearchList ->
            binding.selectedChipsLayout.removeAllViews()
            binding.selectedChipsLayout.visibility =
                if (selectedSearchList.isNotEmpty()) View.VISIBLE else View.GONE

            selectedSearchList.forEach { symptom ->
                val chipView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.symptom_chip, binding.selectedChipsLayout, false)

                val chipText = chipView.findViewById<TextView>(R.id.chipText)
                chipText.text = symptom.problemName

                chipText.setOnClickListener {
                    viewModel.toggleSymptomSelection(symptom, isFromSearch = true)
                }

                binding.selectedChipsLayout.addView(chipView)
            }
        }

        // Update selected count

    }
}