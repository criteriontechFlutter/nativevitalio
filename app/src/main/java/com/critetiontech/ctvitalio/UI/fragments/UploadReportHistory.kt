package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.util.Log
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
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.viewmodel.UploadReportHistoryViewModel
import com.google.gson.Gson

class UploadReportHistory : Fragment() {


    private lateinit var binding: FragmentUploadReportHistoryBinding
    private lateinit var viewModel: UploadReportHistoryViewModel
    private lateinit var adapter: UploadHistoryAdapter
    private var selectedRadiology: Int = 0
    private var selectedImaging: Int = 0
    private var selectedLab: Int = 0
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
            val adapter = UploadHistoryAdapter(reports) { imageUri ->
                // Handle the image click
                // For example, open the image in a full-screen activity
                val bundle = Bundle().apply {

                    putString("fileUri", imageUri.toString()) // DateTime string
                }

                // Navigate to the next fragment with the uploaded data
                findNavController().navigate(R.id.action_uploadReportHistory_to_imagePdfViewFragment, bundle)

            }
            binding.recyclerViewReports.adapter = adapter


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

        // Listen for refresh signal from back stack
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Boolean>("refreshNeeded")
            ?.observe(viewLifecycleOwner) { shouldRefresh ->
                if (shouldRefresh) {
                    selectTab("Radiology")
                    findNavController().currentBackStackEntry?.savedStateHandle?.remove<Boolean>("refreshNeeded")
                }
                else{


                }
            }
        // Default selection
        selectTab("Radiology")

        viewModel.getReportsByCategory("Radiology")
        viewModel.getReportsByCategory("Imaging")
        viewModel.getReportsByCategory("Lab")
        viewModel.selectedRadiology.observe(viewLifecycleOwner, Observer { count ->
            // Only update if count is greater than 0
            if (count > 0) {
                binding.subtitleRadiology.text = " $count Record"
            } else {
                binding.subtitleLab.text = "No Record"
            }
        })

        viewModel.selectedImaging.observe(viewLifecycleOwner, Observer { count ->
            // Only update if count is greater than 0
            if (count > 0) {
                binding.subtitleImaging.text = " $count Record"
            } else {
                binding.subtitleImaging.text = "No Record"
            }
        })

        viewModel.selectedLab.observe(viewLifecycleOwner, Observer { count ->
            // Only update if count is greater than 0
            if (count > 0) {
                binding.subtitleLab.text = " $count Record"
            } else {
                binding.subtitleLab.text = "No Record"
            }
        })
    }
    override fun onResume() {
        super.onResume()
        viewModel.getReportsByCategory("Radiology") // or whatever the last selected tab was
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
