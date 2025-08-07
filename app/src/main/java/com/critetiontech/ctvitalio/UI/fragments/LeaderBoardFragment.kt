package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.LeaderboardAdapter
import com.critetiontech.ctvitalio.databinding.FragmentLeaderBoardBinding

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderBoardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderBoardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sample data
        val users = listOf(
            User(1, "Albert Flores", 465),
            User(2, "Jacob Jones", 360),
            User(3, "Darrell Steward", 350),
            User(1, "Albert Flores", 465),
            User(2, "Jacob Jones", 360),
            User(3, "Darrell Steward", 350),
            User(1, "Albert Flores", 465),
            User(2, "Jacob Jones", 360),
            User(3, "Darrell Steward", 350),
            User(1, "Albert Flores", 465),
            User(2, "Jacob Jones", 360),
            User(3, "Darrell Steward", 350),
            User(1, "Albert Flores", 465),
            User(2, "Jacob Jones", 360),
            User(3, "Darrell Steward", 350),
            User(1, "Albert Flores", 465),
            User(2, "Jacob Jones", 360),
            User(3, "Darrell Steward", 350),
            // repeat or load real data...
        )

        // RecyclerView setup
        binding.playerList.layoutManager = LinearLayoutManager(requireContext())
        binding.playerList.adapter = LeaderboardAdapter(users)

        // Optionally start expanded:
        // binding.motionLayout.transitionToEnd()

        // MotionLayout transition listener
        binding.motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) { }

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
                // Optional: fade out the top area gradually as the sheet expands
                // progress goes from 0 (collapsed) -> 1 (expanded)
                // Replace 'playerid' with the id of the view you want to fade/hide

                // If you want to shrink or translate top cards while sliding,
                // you can also animate scale/translation here:
                // binding.card_center.scaleX = 1f + 0.1f * progress
                // binding.card_center.scaleY = 1f + 0.1f * progress
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                // When transition completes, hide or show the top layout based on the state.
                // Replace R.id.expanded with the actual id in your MotionScene if different.
                if (currentId == R.id.expanded) {
                    // Fully expanded — hide the top area (removes it from layout flow)
                    binding.playerid.visibility = View.VISIBLE
                    binding.bodyLeft.visibility = View.VISIBLE
                    binding.bodyRight.visibility = View.VISIBLE
                    binding.avatarCenter.visibility = View.VISIBLE
                } else {
                    // Collapsed or other — ensure it's visible
                    binding.playerid.visibility = View.GONE
//                    binding.playerid.alpha = 1f
                }
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float
            ) { }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class User(val rank: Int, val name: String, val gems: Int)