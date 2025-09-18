package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.NewChallengedAdapter
import com.critetiontech.ctvitalio.databinding.FragmentNewChallengeBinding
import com.critetiontech.ctvitalio.model.NewChallengeModel
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel

class NewChallengeFragment : Fragment() {
    private lateinit var binding: FragmentNewChallengeBinding
    private lateinit var challengesViewModel: ChallengesViewModel
//    val context = MyApplication.appContext




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
            binding.newChallengedRecyclerView.adapter = NewChallengedAdapter(
                list,
                onItemClick =  { challenge ->
                    challengesViewModel.insertChallengeparticipants( challenge.id.toString())
                },
                onItemClick1 =  { challenge: NewChallengeModel ->
                    val bundle = Bundle().apply {
                        putSerializable("challenges", challenge )
                    }
                    findNavController().navigate(R.id.action_challenges_to_challengeDetailsFragment, bundle)

                }
            )

        }

    }


}