package com.criterion.nativevitalio.UI.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentDashboardBinding


class Dashboard : Fragment() {
    private lateinit var binding : FragmentDashboardBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.profileSection.setOnClickListener {
          findNavController().navigate(R.id.action_dashboard_to_drawer4)

        }



        binding.pillsReminder.setOnClickListener {
            findNavController().navigate(R.id.pillsReminder)

        }

        binding.symptomsTracker.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_symptomsFragment)

        }

    }


}