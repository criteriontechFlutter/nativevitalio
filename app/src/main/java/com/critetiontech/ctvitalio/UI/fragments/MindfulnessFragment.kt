package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.adapter.BreathingAdapter
import com.critetiontech.ctvitalio.adapter.EyeExerciseAdapter
import com.critetiontech.ctvitalio.databinding.FragmentMindfullnessBinding
import com.critetiontech.ctvitalio.viewmodel.WellnessViewModel


class MindfulnessFragment : Fragment() {

    private lateinit var binding: FragmentMindfullnessBinding
    private val viewModel: WellnessViewModel by viewModels()
    private lateinit var breathingAdapter: BreathingAdapter
    private lateinit var eyeAdapter: EyeExerciseAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMindfullnessBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.wellnessImageArrow.setOnClickListener {
            findNavController().popBackStack()
        }
        setupBreathingRecyclerView()
        setupEyeExerciseRecyclerView()
        observeViewModel()




    }


    private fun setupBreathingRecyclerView() {
        breathingAdapter = BreathingAdapter()
        binding.breathingRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = breathingAdapter
        }
    }

    private fun setupEyeExerciseRecyclerView() {
        eyeAdapter = EyeExerciseAdapter()
        binding.eyeExerciseRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eyeAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.breathingProtocols.observe(viewLifecycleOwner) { protocols ->
            breathingAdapter.submitList(protocols)
        }
        viewModel.eyeExercises.observe(viewLifecycleOwner) { exercises ->
            eyeAdapter.submitList(exercises)
        }
    }

}