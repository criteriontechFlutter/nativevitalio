package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentCorporateDashBoardBinding
import com.critetiontech.ctvitalio.databinding.FragmentWaterIntakeBinding


class WaterLayout : Fragment() {

    private lateinit var binding: FragmentWaterIntakeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentWaterIntakeBinding.inflate(inflater, container, false)
        return binding.root
    }


}