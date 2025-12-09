package com.critetiontech.ctvitalio.UI.fragments.dashboard_pages

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.UltraHumanActivity
import com.critetiontech.ctvitalio.adapter.NewChallengedAdapter
import com.critetiontech.ctvitalio.databinding.FragmentDashboardChallengesBinding
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel

class dashboard_challenges : Fragment() {

    private var _binding: FragmentDashboardChallengesBinding? = null
    private val binding get() = _binding!!

    private val challengesViewModel: ChallengesViewModel by viewModels()

    private lateinit var adapter: NewChallengedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardChallengesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch challenge data
        challengesViewModel.getNewChallenge()

        // Initialize adapter ONCE
        observeChallenges()

        // Observe list once

    }
    private fun observeChallenges() {
        challengesViewModel.newChallenges.observe(viewLifecycleOwner) { list ->
            binding.newChallengedRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                adapter = NewChallengedAdapter(list.toMutableList(), { challenge ->
                    challengesViewModel.joinChallenge(challenge.id.toString())
                }, { challenge ->
                    val bundle = Bundle().apply { putSerializable("challenges", challenge) }
                    findNavController().navigate(R.id.action_home2_to_challengeDetailsFragment, bundle)
                })
                PagerSnapHelper().attachToRecyclerView(this)
            }
            binding.activeChalleTextId.text = "Active Challenges (${list.size})"
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
