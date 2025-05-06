package com.criterion.nativevitalio.UI.ui.signupFragment

import DateUtils.showListBottomSheet
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentAdressBinding


class AdressFragment : Fragment() {
    // TODO: Rename and change types of parameters

    private lateinit var binding : FragmentAdressBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentAdressBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_adressFragment_to_weightFragment)
        }

        binding.etCountry.setOnClickListener {
            val countries = listOf("India", "USA", "UK", "Australia", "Germany")
            showListBottomSheet(requireContext(), "Select Country", countries) {
                binding.etCountry.setText(it)
                // Optionally clear state and city
                binding.etState.setText("")
                binding.etCity.setText("")
            }
        }

        binding.etState.setOnClickListener {
            val states = listOf("Delhi", "Maharashtra", "Karnataka", "Gujarat")
            showListBottomSheet(requireContext(), "Select State", states) {
                binding.etState.setText(it)
                binding.etCity.setText("")
            }
        }

        binding.etCity.setOnClickListener {
            val cities = listOf("Mumbai", "Delhi", "Bangalore", "Ahmedabad")
            showListBottomSheet(requireContext(), "Select City", cities) {
                binding.etCity.setText(it)
            }
        }

    }
}