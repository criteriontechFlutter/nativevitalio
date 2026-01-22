package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentDynamicRecoveryBinding

class DynamicRecovery : Fragment() {

    // 1. Declare the binding variable
    private var _binding: FragmentDynamicRecoveryBinding? = null
    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 2. Inflate the layout using binding
        _binding = FragmentDynamicRecoveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 3. Now you can safely use your views via binding
         binding.wellnessImageArrow.setOnClickListener {

             findNavController().popBackStack()
            // Handle button click
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 4. Avoid memory leaks by nullifying the binding
        _binding = null
    }
}