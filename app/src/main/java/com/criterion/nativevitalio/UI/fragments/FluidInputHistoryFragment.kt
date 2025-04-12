package com.criterion.nativevitalio.UI.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.adapter.FluidIntakeLogAdapter
import com.criterion.nativevitalio.databinding.FragmentFluidInputHistoryBinding
import com.criterion.nativevitalio.model.FluidPoint
import com.criterion.nativevitalio.viewmodel.FluidIntakeOuputViewModel

class FluidInputHistoryFragment : Fragment() {

    private lateinit var binding: FragmentFluidInputHistoryBinding
    private lateinit var fluidIntakeAdapter: FluidIntakeLogAdapter
    private val viewModel: FluidIntakeOuputViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFluidInputHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.backIcon.setOnClickListener {
            findNavController().popBackStack()
        }


        viewModel.fetchManualFluidIntake("UHID01235")

        viewModel.intakeList.observe(viewLifecycleOwner) { list ->
            fluidIntakeAdapter = FluidIntakeLogAdapter(list)
            binding.recyclerViewFluidLogs.adapter = fluidIntakeAdapter
        }


        binding.fluidChartContent.fluidData = listOf(
            FluidPoint(2f, 200f, "#3DA5F5"),
            FluidPoint(6f, 300f, "#3DA5F5"),
            FluidPoint(8f, 350f, "#FFD700"),
            FluidPoint(11f, 400f, "#3DA5F5"),
            FluidPoint(14f, 200f, "#FF7F50"),
            FluidPoint(17f, 200f, "#3DA5F5"),
            FluidPoint(18f, 50f, "#CD853F")
        )

    }
}