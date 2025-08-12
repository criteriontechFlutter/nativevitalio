package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.util.Log
import android.view.Gravity
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

    private var isPlayerId2Visible = false // Tracks visibility state

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

        // Sample user data
        val users = listOf(
            User(1, "Albert Flores", 465),
            User(2, "Jacob Jones", 360),
            User(3, "Darrell Steward", 350),
            User(4, "Emma Watson", 320),
            User(5, "Chris Evans", 310),
            User(6, "Scarlett Johansson", 300),
            User(7, "Robert Downey", 290),
            User(8, "Tom Holland", 280),
            User(9, "Benedict Cumberbatch", 270)
        )

        // Initial visibility
//        binding.playerId.visibility = View.VISIBLE
//        binding.playerId2.visibility = View.GONE

        // RecyclerView setup
        binding.playerList.layoutManager = LinearLayoutManager(requireContext())
        binding.playerList.adapter = LeaderboardAdapter(users)

        // MotionLayout transition listener
        binding.motionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {

            }
            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
//                when {
//                    startId == R.id.collapsed && endId == R.id.expanded -> {
//                        Log.d("MotionState", "Sliding UP → Expanding")
//                    }
//                    startId == R.id.expanded && endId == R.id.collapsed -> {
//                        Log.d("MotionState", "Sliding DOWN → Collapsing")
//                    }
//                }
            }

            override fun onTransitionCompleted(
                motionLayout: MotionLayout?,
                currentId: Int
            ) {
                Log.d("MotionState", "Transition completed to $currentId")
                when (currentId) {
                    R.id.collapsed -> {
                        Log.d("MotionState", "Collapsed state reached")
                        binding.bodyLeft.visibility = View.GONE
                        binding.bodyCenter.visibility = View.GONE
                        binding.bodyRight.visibility = View.GONE
                        binding.playerId.gravity = Gravity.TOP
                    }
                    R.id.expanded -> {
                        Log.d("MotionState", "Expanded state reached")
                        binding.bodyLeft.visibility = View.VISIBLE
                        binding.bodyCenter.visibility = View.VISIBLE
                        binding.bodyRight.visibility = View.VISIBLE
                        binding.playerId.gravity = Gravity.CENTER
                    }
                }
            }

            override fun onTransitionTrigger(
                motionLayout: MotionLayout?,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class User(val rank: Int, val name: String, val gems: Int)