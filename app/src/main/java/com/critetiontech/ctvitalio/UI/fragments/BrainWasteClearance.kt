package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentBrainWasteClearanceBinding
import com.critetiontech.ctvitalio.model.MovementIndexViewModel

class BrainWasteClearance : Fragment() {

    private var _binding: FragmentBrainWasteClearanceBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MovementIndexViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrainWasteClearanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Back button click listener

        viewModel = ViewModelProvider(this)[MovementIndexViewModel::class.java]
        binding.wellnessImageArrow.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.wellnessMetrics.observe(viewLifecycleOwner) { response ->

            // Map API data safely
        // Example usage
     }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // prevent memory leak
    }
}