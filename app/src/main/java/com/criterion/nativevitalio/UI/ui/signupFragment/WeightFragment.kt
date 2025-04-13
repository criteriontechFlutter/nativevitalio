package com.criterion.nativevitalio.UI.ui.signupFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentWeightBinding

class WeightFragment : Fragment() {
    private lateinit var binding : FragmentWeightBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWeightBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val items = listOf("Kg", "Gm")

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            items
        )
        adapter.setDropDownViewResource(R.layout.spinner_item)
        binding.spnUnit.adapter = adapter

        binding.btnNext.setOnClickListener {
            findNavController().navigate(R.id.action_weightFragment_to_heightFragment)
        }
    }
}