package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentBrainWasteClearanceBinding

class BrainWasteClearance : Fragment() {

    private var _binding: FragmentBrainWasteClearanceBinding? = null
    private val binding get() = _binding!!

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
        binding.wellnessImageArrow.setOnClickListener {
            // Option 1: If using Navigation Component
            // findNavController().navigateUp()

            // Option 2: If not using Navigation Component
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        // Example usage
     }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // prevent memory leak
    }
}