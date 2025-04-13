package com.criterion.nativevitalio.UI.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.adapter.SymptomHistoryAdapter
import com.criterion.nativevitalio.adapter.SymptomsTrackerAdapter
import com.criterion.nativevitalio.databinding.FragmentDrawerBinding
import com.criterion.nativevitalio.databinding.FragmentSymptomHistoryBinding
import com.criterion.nativevitalio.databinding.FragmentSymptomTrackerFragmentsBinding
import com.criterion.nativevitalio.viewmodel.SymptomsHistoryViewModel
import com.criterion.nativevitalio.viewmodel.SymptomsTrackerViewModel
import com.criterion.nativevitalio.viewmodel.SymptomsViewModel

class SymptomHistory: Fragment() {

    private lateinit var binding: FragmentSymptomHistoryBinding
    private lateinit var viewModel: SymptomsHistoryViewModel
    private lateinit var adapter: SymptomHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSymptomHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SymptomHistoryAdapter()
        binding.symptomRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.symptomRecyclerView.adapter = adapter

        viewModel = ViewModelProvider(this)[SymptomsHistoryViewModel::class.java]
        viewModel.getSymptoms()

        viewModel.symptomList.observe(viewLifecycleOwner) {
            adapter.submitGroupedList(it)
        }

//        viewModel.loading.observe(viewLifecycleOwner) {
//            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
//        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
        binding.backButton.setOnClickListener(){

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
}