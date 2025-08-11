package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentAllergiesBinding
import com.critetiontech.ctvitalio.databinding.FragmentChallengeDetailsBinding


class ChallengeDetailsFragment : Fragment() {
    private lateinit var binding: FragmentChallengeDetailsBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChallengeDetailsBinding.inflate(inflater, container, false)
        return binding.root    }

}