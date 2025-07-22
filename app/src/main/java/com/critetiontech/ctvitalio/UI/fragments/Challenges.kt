package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.ChallengesAdapter
import com.critetiontech.ctvitalio.databinding.FragmentAllergiesBinding
import com.critetiontech.ctvitalio.databinding.FragmentChallengesBinding
import com.critetiontech.ctvitalio.databinding.FragmentChatBinding
import com.google.android.material.tabs.TabLayoutMediator


class Challenges : Fragment() {
    private lateinit var binding: FragmentChallengesBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChallengesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerAdapter = ChallengesAdapter(requireActivity())
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Joined Challenges (5)"
                else -> "New Challenges (3)"
            }
        }.attach()

        // Optional: to add spacing between tabs
        val tabStrip = binding.tabLayout.getChildAt(0) as ViewGroup
        for (i in 0 until tabStrip.childCount) {
            val tab = tabStrip.getChildAt(i)
            val lp = tab.layoutParams as ViewGroup.MarginLayoutParams
            lp.setMargins(8, 8, 8, 8)
            tab.layoutParams = lp
            binding.tabLayout.requestLayout()
        }


    }
}