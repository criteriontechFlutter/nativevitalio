package com.criterion.nativevitalio.UI.ui.signupFragment

import DateUtils.showHeightPicker
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentHeightBinding

class HeightFragment : Fragment() {
    private lateinit var binding : FragmentHeightBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHeightBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_heightFragment_to_chronicConditionFragment)
        }

        binding.etHeight.apply {
            isFocusable = false
            isClickable = true
        }

        binding.etHeight.setOnClickListener {
            // Just open height picker
            showHeightPicker(requireContext()) { selectedHeight ->
                binding.etHeight.setText(selectedHeight)
            }
        }
    }
}