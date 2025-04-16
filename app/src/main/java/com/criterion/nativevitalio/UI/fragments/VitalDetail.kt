package com.criterion.nativevitalio.UI.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.adapter.VitalDetailsAdapter
import com.criterion.nativevitalio.databinding.FragmentSymptomHistoryBinding
import com.criterion.nativevitalio.databinding.FragmentSymtomsBinding
import com.criterion.nativevitalio.databinding.FragmentVitalDetailBinding
import com.criterion.nativevitalio.viewmodel.PillsReminderViewModal
import com.criterion.nativevitalio.viewmodel.VitalDetailsViewModel

class VitalDetail  : Fragment() {

    private lateinit var binding: FragmentVitalDetailBinding
    private lateinit var viewModel: VitalDetailsViewModel
    private lateinit var adapter: VitalDetailsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentVitalDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[VitalDetailsViewModel::class.java]
        adapter = VitalDetailsAdapter { vitalType ->
            val bundle = Bundle().apply {
                putString("vitalType", vitalType)
            }
            findNavController().navigate(R.id.action_vitalDetail_to_connection, bundle)
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.vitalsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        binding.backButton.setOnClickListener(){

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }



        viewModel.getVitals()

        viewModel.vitalList.observe(viewLifecycleOwner) { vitals ->
            adapter.submitVitals(vitals)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            view.findViewById<ProgressBar>(R.id.progressBar).visibility =
                if (it) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) {
            it?.let {
                msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show() }
        }
    }
}