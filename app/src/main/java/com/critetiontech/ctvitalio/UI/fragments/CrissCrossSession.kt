package com.critetiontech.vitalioCorporate.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.critetiontech.vitalioCorporate.R
import com.critetiontech.vitalioCorporate.databinding.FragmentCrissCrossSessionBinding

class CrissCrossSession : Fragment() {



    private lateinit var binding: FragmentCrissCrossSessionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentCrissCrossSessionBinding.inflate(inflater, container, false)

        binding.btnStartAgain.setOnClickListener {
            // Restart Exercise
        }

        binding.btnDifferentExercise.setOnClickListener {
            // Open Exercise List
        }

        return binding.root
    }
}