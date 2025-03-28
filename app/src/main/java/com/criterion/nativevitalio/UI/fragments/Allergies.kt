package com.criterion.nativevitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentAllergiesBinding
import com.criterion.nativevitalio.databinding.FragmentDashboardBinding


class Allergies : Fragment() {

    private lateinit var binding : FragmentAllergiesBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAllergiesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


}