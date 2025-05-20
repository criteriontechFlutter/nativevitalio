package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.adapter.AllergiesAdapter
import com.critetiontech.ctvitalio.model.AllergyTypeItem
import com.critetiontech.ctvitalio.utils.AddAllergyBottomSheet
import com.critetiontech.ctvitalio.viewmodel.AllergiesViewModel
import com.critetiontech.ctvitalio.databinding.FragmentAllergiesBinding


class Allergies : Fragment() {


    private lateinit var binding: FragmentAllergiesBinding
    private lateinit var viewModel: AllergiesViewModel
    private lateinit var adapter: AllergiesAdapter
    private var allergyTypeList: List<AllergyTypeItem> = listOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllergiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[AllergiesViewModel::class.java]

        // RecyclerView setup
        adapter = AllergiesAdapter()
        binding.allergiesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.allergiesRecycler.adapter = adapter

        // Observe ViewModel
        observeViewModel()

        // Fetch data
        viewModel.getAllergies()
        viewModel.getHistorySubCategoryMasterById()

        // Listen to allergy types and cache for bottom sheet
        viewModel.allergyTypes.observe(viewLifecycleOwner) { list ->
            allergyTypeList = list
        }

        // Open bottom sheet with allergy types
        binding.addAllergiesBtn.setOnClickListener {
            if (allergyTypeList.isNotEmpty()) {
                val sheet = AddAllergyBottomSheet.newInstance(allergyTypeList)
                 sheet.show(childFragmentManager, "AddAllergyBottomSheet")
            } else {
                Toast.makeText(requireContext(), "Allergy types not loaded yet.", Toast.LENGTH_SHORT).show()
            }
        }

        // Optional: Back button
        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun observeViewModel() {
        viewModel. loading.observe(viewLifecycleOwner) {
            // Optionally show/hide progress
        }

        viewModel.allergyList.observe(viewLifecycleOwner) { allergyItems ->
            Log.d("ALLERGIES_OBSERVER", "List updated: ${allergyItems.size}")
            adapter.submitList(allergyItems)
        }

        viewModel._errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }
}