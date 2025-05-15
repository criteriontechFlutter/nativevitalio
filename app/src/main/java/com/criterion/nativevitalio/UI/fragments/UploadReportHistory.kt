package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.UploadHistoryAdapter
import com.critetiontech.ctvitalio.databinding.FragmentUploadReportHistoryBinding
import com.critetiontech.ctvitalio.viewmodel.UploadReportHistoryViewModel

class UploadReportHistory : Fragment() {


    private lateinit var binding: FragmentUploadReportHistoryBinding
    private lateinit var viewModel: UploadReportHistoryViewModel
    private lateinit var adapter: UploadHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUploadReportHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[UploadReportHistoryViewModel::class.java]

        binding.recyclerViewReports.layoutManager = LinearLayoutManager(requireContext())

        viewModel.reportList.observe(viewLifecycleOwner, Observer { reports ->
            adapter = UploadHistoryAdapter(reports)
            binding.recyclerViewReports.adapter = adapter
            binding.subtitleRadiology.text=reports.size.toString()
            binding.subtitleImaging.text=reports.size.toString()
            binding.subtitleLab.text=reports.size.toString()

        })

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
        binding.addReport.setOnClickListener {
            findNavController().navigate(R.id.action_uploadReportHistory_to_uploadReport3)
        }
        binding.backIcon.setOnClickListener(){

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        // Setup tab click listeners
        binding.tabRadiology.setOnClickListener { selectTab("Radiology") }
        binding.tabImaging.setOnClickListener { selectTab("Imaging") }
        binding.tabLab.setOnClickListener { selectTab("Lab") }


        // Default selection
        selectTab("Radiology")
    }

    private fun selectTab(category: String) {
        val selectedTab = when (category) {
            "Radiology" -> binding.tabRadiology
            "Imaging" -> binding.tabImaging
            "Lab" -> binding.tabLab
            else -> null
        }

        val tabs = listOf(binding.tabRadiology, binding.tabImaging, binding.tabLab)

        tabs.forEach { tab ->
            tab.setBackgroundResource(R.drawable.tab_disabled)
//            tab.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
//            tab.setTypeface(null, Typeface.NORMAL)
        }

        selectedTab?.apply {
            setBackgroundResource(R.drawable.tab_selected)
//            setTextColor(ContextCompat.getColor(requireContext(), R.color.primaryBlue))
//            setTypeface(null, Typeface.BOLD)
        }

        viewModel.getReportsByCategory(category)
    }
}
