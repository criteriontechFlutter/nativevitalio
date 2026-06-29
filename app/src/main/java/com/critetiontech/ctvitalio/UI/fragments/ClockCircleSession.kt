package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentClockCircleSessionBinding

class ClockCircleSession : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentClockCircleSessionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClockCircleSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.btnStartAgain.setOnClickListener {
            // Restart Exercise
        }

        binding.btnChooseExercise.setOnClickListener {
            // Navigate to Exercise List
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}