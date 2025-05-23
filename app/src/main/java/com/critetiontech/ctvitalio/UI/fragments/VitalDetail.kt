package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.VitalDetailsAdapter
import com.critetiontech.ctvitalio.databinding.FragmentVitalDetailBinding
import com.critetiontech.ctvitalio.viewmodel.VitalDetailsViewModel

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
        adapter = VitalDetailsAdapter( { vitalType ->
            val bundle = Bundle().apply {
                putString("vitalType", vitalType)
            }
            findNavController().navigate(R.id.action_vitalDetail_to_connection, bundle)
        },findNavController())
        val recyclerView = view.findViewById<RecyclerView>(R.id.vitalsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }

        binding.backButton.setOnClickListener(){
            findNavController().popBackStack()
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