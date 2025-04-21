package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.ctvitalio.viewmodel.UploadReportViewModel
import com.critetiontech.ctvitalio.adapter.VitalDetailsAdapter
import com.critetiontech.ctvitalio.databinding.FragmentUploadReportBinding

class UploadReport : Fragment() {
    private lateinit var binding: FragmentUploadReportBinding
    private lateinit var viewModel: UploadReportViewModel
    private lateinit var adapter: VitalDetailsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentUploadReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[UploadReportViewModel::class.java]


    }

}