package com.criterion.nativevitalio.UI.fragments

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView

import com.criterion.nativevitalio.databinding.FragmentFluidBinding



class FluidFragment : Fragment() {


    private lateinit var binding: FragmentFluidBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFluidBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


      //  binding.glassView.setGlassSize(250)
        // set selected size

//        binding.glassView.setOnFillChangedListener { percent, ml ->
//            //binding.fillPercent.text = "$percent%\n$ml ml"
//        }

        val selectedGlassSize = 250
       // binding.glass.setGlassSize(selectedGlassSize)





    }



}

