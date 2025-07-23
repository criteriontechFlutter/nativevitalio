package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.ctvitalio.adapter.JoinedChallengesAdapter
import com.critetiontech.ctvitalio.databinding.FragmentJoinedFragmentsBinding
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel


class JoinedFragments : Fragment() {
    private lateinit var binding: FragmentJoinedFragmentsBinding
    private lateinit var challengesViewModel: ChallengesViewModel
    private lateinit var joinedChallengesAdapter: JoinedChallengesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJoinedFragmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        challengesViewModel = ViewModelProvider(this)[ChallengesViewModel::class.java]
        challengesViewModel.getJoinedChallenge()
        challengesViewModel.joinedChallengeList.observe(viewLifecycleOwner) { list ->
            joinedChallengesAdapter = JoinedChallengesAdapter(list)
            binding.joinedRecyclerView.adapter = joinedChallengesAdapter
        }
    }

}