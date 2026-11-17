package com.critetiontech.vitalioCorporate.UI.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.critetiontech.vitalioCorporate.R
import com.critetiontech.vitalioCorporate.databinding.FragmentCorporateDashBoardBinding
import com.critetiontech.vitalioCorporate.databinding.FragmentWaterIntakeBinding


class WaterLayout : Fragment() {

    private lateinit var binding: FragmentWaterIntakeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentWaterIntakeBinding.inflate(inflater, container, false)
        return binding.root
    }


}























