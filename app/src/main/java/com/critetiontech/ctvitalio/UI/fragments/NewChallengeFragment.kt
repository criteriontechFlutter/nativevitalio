package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.critetiontech.ctvitalio.adapter.NewChallengedAdapter
import com.critetiontech.ctvitalio.databinding.FragmentNewChallengeBinding
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel

class NewChallengeFragment : Fragment() {
    private lateinit var binding: FragmentNewChallengeBinding
    private lateinit var challengesViewModel: ChallengesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        challengesViewModel = ViewModelProvider(this)[ChallengesViewModel::class.java]


        challengesViewModel.getNewChallenge()
        challengesViewModel.newChallengeList.observe(viewLifecycleOwner) { list ->
            binding.newChallengedRecyclerView.adapter = NewChallengedAdapter(list)
        }

    }


}