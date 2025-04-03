package com.criterion.nativevitalio.UI.fragments

import PrefsManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentDashboardBinding
import com.criterion.nativevitalio.databinding.FragmentDrawerBinding


class drawer : Fragment() {


    private lateinit var binding: FragmentDrawerBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDrawerBinding.inflate(inflater, container, false)
        return binding.root
        // Inflate the layout for this fragment

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.allergies.setOnClickListener {
            findNavController().navigate(R.id.action_drawer4_to_allergies3)

        }
        binding.logoout.setOnClickListener {
            PrefsManager().clearPatient()
            findNavController().navigate(R.id.action_drawer4_to_allergies3)

        }
    }

}