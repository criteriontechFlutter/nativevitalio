package com.critetiontech.ctvitalio.UI.fragments

import LeaderboardItem
import PrefsManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.LeaderboardAdapter
import com.critetiontech.ctvitalio.databinding.FragmentLeaderBoardBinding
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderBoardBinding? = null
    private val binding get() = _binding!!

    // 🔹 Store Top 3 here (NOT bound to UI)
    private var top1User: LeaderboardItem? = null
    private var top2User: LeaderboardItem? = null
    private var top3User: LeaderboardItem? = null
    private var currentEmployee: LeaderboardItem? = null

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

        val patient = PrefsManager().getPatient()

        // 🔹 Parse leaderboard JSON
        val leaderboardList: List<LeaderboardItem> =
            if (!patient?.leaderboardData.isNullOrEmpty()) {
                Gson().fromJson(
                    patient.leaderboardData,
                    object : TypeToken<List<LeaderboardItem>>() {}.type
                )
            } else {
                emptyList()
            }

        Log.d("Leaderboard", "Total items = ${leaderboardList.size}")

        // 🔹 Sort by rank (important)
        val sortedList = leaderboardList.sortedBy { it.rank }

        // 🔹 Store Top 3 ONLY
        top1User = sortedList.getOrNull(0)
        top2User = sortedList.getOrNull(1)
        top3User = sortedList.getOrNull(2)
        currentEmployee = sortedList.find { it.empId.toString() == PrefsManager().getPatient()?.empId.toString() }

        binding.rankText.text = currentEmployee?.rank?.toString() ?: "-"
        binding.nameText.text = currentEmployee?.empName ?: "Unknown"
        binding.gemText.text  = currentEmployee?.totalPoints?.toString() ?: "0"

        Glide.with(binding  .profileImage.context)
            .load(RetrofitInstance.StaggingbaseUrl.toString()+":5082/"+ currentEmployee?.imageURL?.replace("\\", "/"))

            .circleCrop().placeholder(R.drawable.achievement_background)
            .error(R.drawable.achievement_background)
            .circleCrop()
            .into(binding .profileImage)
        Log.d("Leaderboard", "Top1 = $top1User")
        Log.d("Leaderboard", "Top2 = $top2User")
        Log.d("Leaderboard", "Top3 = $top3User")
        bindTopThree()
        // 🔹 Remaining users (rank 4+)
        val remainingUsers = if (sortedList.size > 3) {
            sortedList.drop(3)
        } else {
            emptyList()
        }

        // 🔹 RecyclerView (ONLY remaining users)
        binding.playerListdata.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = LeaderboardAdapter(remainingUsers)
        }

        setupMotionListener()
    }
    private fun bindTopThree() {

        // 🥇 Rank 1 → CENTER
        top1User?.let { user ->
            binding.nameCenter.text = user.empName
            binding.scoreCenter.text = user.totalPoints.toString()
            // image loading optional
            // loadImage(binding.firstUser, user.imageURL)
            Glide.with(binding.firstUser.context)
                .load(RetrofitInstance.StaggingbaseUrl.toString()+":5082/"+ user.imageURL.replace("\\", "/"))

                .circleCrop().placeholder(R.drawable.achievement_background)
                .error(R.drawable.achievement_background)
                .into(binding.firstUser)

        }

        // 🥈 Rank 2 → RIGHT
        top2User?.let { user ->
            binding.nameRight.text = user.empName
            binding.scoreRight.text = user.totalPoints.toString()
            // loadImage(binding.secondUser, user.imageURL)
            Glide.with(binding.secondUser.context)
                .load(RetrofitInstance.StaggingbaseUrl.toString()+":5082/"+ user.imageURL.replace("\\", "/"))

                .circleCrop() .placeholder(R.drawable.achievement_background)
                .error(R.drawable.achievement_background)
                .into(binding.secondUser)
        }

        // 🥉 Rank 3 → LEFT
        top3User?.let { user ->
            binding.nameLeft.text = user.empName
            binding.scoreLeft.text = user.totalPoints.toString()
            // loadImage(binding.thirdUser, user.imageURL)
            Glide.with(binding.thirdUser.context)
                .load(RetrofitInstance.StaggingbaseUrl.toString()+":5082/"+ user.imageURL.replace("\\", "/"))

                .circleCrop() .placeholder(R.drawable.achievement_background)
                .error(R.drawable.achievement_background)
                .into(binding.thirdUser)
        }
    }
    // 🔹 MotionLayout listener (unchanged)
    private fun setupMotionListener() {
        binding.motionLayout.setTransitionListener(object :
            MotionLayout.TransitionListener {

            override fun onTransitionStarted(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int
            ) {}

            override fun onTransitionChange(
                motionLayout: MotionLayout?,
                startId: Int,
                endId: Int,
                progress: Float
            ) {}

            override fun onTransitionCompleted(
                motionLayout: MotionLayout?,
                currentId: Int
            ) {
                when (currentId) {
                    R.id.collapsed -> {
                        binding.bodyLeft.visibility = View.GONE
                        binding.bodyCenter.visibility = View.GONE
                        binding.bodyRight.visibility = View.GONE
                        binding.playerId.gravity = Gravity.TOP
                    }
                    R.id.expanded -> {
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