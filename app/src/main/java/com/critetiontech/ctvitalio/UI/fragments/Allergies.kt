package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.adapter.AllergiesAdapter
import com.critetiontech.ctvitalio.model.AllergyTypeItem
import com.critetiontech.ctvitalio.utils.AddAllergyBottomSheet
import com.critetiontech.ctvitalio.viewmodel.AllergiesViewModel
import com.critetiontech.ctvitalio.databinding.FragmentAllergiesBinding


class Allergies : Fragment(), AllergiesAdapter.SelectionListener {

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

        adapter = AllergiesAdapter()
        adapter.selectionListener = this
        binding.allergiesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.allergiesRecycler.adapter = adapter

        viewModel.allergyList.observe(viewLifecycleOwner) { allergyItems ->
            adapter.submitList(allergyItems.toList())
            adapter.clearSelection()
        }

        viewModel.getAllergies()
        viewModel.getHistorySubCategoryMasterById()

        viewModel.allergyTypes.observe(viewLifecycleOwner) { list ->
            allergyTypeList = list
        }

        binding.addAllergiesBtn.setOnClickListener {
            if (allergyTypeList.isNotEmpty()) {
                val sheet = AddAllergyBottomSheet.newInstance(allergyTypeList)
                sheet.show(childFragmentManager, "AddAllergyBottomSheet")
            } else {
                Toast.makeText(requireContext(), "Allergy types not loaded yet.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.deleteIcon.setOnClickListener {
            val selectedItem = adapter.getSelectedItem()
            if (selectedItem != null) {
             viewModel.deletePatientAllergies(selectedItem.rowId.toString())
            } else {
                // No item selected, maybe show a message
            }
        }
    }

    override fun onSelectionChanged(selectedCount: Int) {
        binding.deleteIcon.isVisible = (selectedCount == 1)
    }
}