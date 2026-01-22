package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.critetiontech.ctvitalio.adapter.ChallengesAdapter
import com.critetiontech.ctvitalio.databinding.FragmentAllergiesBinding
import com.critetiontech.ctvitalio.databinding.FragmentChallengesBinding
import com.critetiontech.ctvitalio.databinding.FragmentChatBinding
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel
import com.critetiontech.ctvitalio.viewmodel.ChatViewModel
import com.google.android.material.tabs.TabLayoutMediator


class Challenges : Fragment() {
    private lateinit var binding: FragmentChallengesBinding
    private  val challengesViewModel: ChallengesViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChallengesBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        challengesViewModel = ViewModelProvider(this)[ChallengesViewModel::class.java]
        challengesViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) showLoading() else hideLoading()
        }


        challengesViewModel.getJoinedChallenge()
        val pagerAdapter = ChallengesAdapter(requireActivity())
        binding.viewPager.adapter = pagerAdapter


        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Joined Challenges (0)"
                else -> "New Challenges (0)"
            }
        }.attach()
        observeTabCounts()

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Trigger API call based on tab index
                when (position) {
                    0 -> challengesViewModel.getJoinedChallenge()
                    1 -> challengesViewModel.getNewChallenge()
                }
            }
        })


    }
    private fun observeTabCounts() {
        challengesViewModel.joinedCount.observe(viewLifecycleOwner) { count ->
            val tab = binding.tabLayout.getTabAt(0)
            tab?.text = "Joined Challenges ($count)"
        }

        challengesViewModel.newCount.observe(viewLifecycleOwner) { count ->
            val tab = binding.tabLayout.getTabAt(1)
            tab?.text = "New Challenges ($count)"
        }
    }

}