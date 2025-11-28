package com.critetiontech.ctvitalio.UI.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.critetiontech.ctvitalio.databinding.FragmentWaterIntakeBinding

class WaterIntakeFragment : Fragment() {

    private var _binding: FragmentWaterIntakeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaterIntakeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Example: change ring dynamically

        binding.waterRing.setLevel(25f) // animates to 50%
//
//// tweak visuals to match the video:
//        binding.waterRing.setColors(Color.parseColor("#D1F4FF"), Color.parseColor("#2DA6FF"))
//        binding.waterRing.setWaveAmplitude(0.02f, 0.05f)   // back, front amplitude fractions
//        binding.waterRing.setWavelengthFactor(1.8f)
//        binding.waterRing.setPhaseDuration(1500L)
//        binding.waterRing.setRingThicknessDp(10f)
//        binding.waterRing.setPercentTextSp(26f)
//
//// stop wave if you want
//        binding.waterRing.setWaveRunning(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
