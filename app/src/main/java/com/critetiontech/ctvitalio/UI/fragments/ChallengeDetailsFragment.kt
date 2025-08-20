package com.critetiontech.ctvitalio.UI.fragments

import PrefsManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.ChallengeDetailsAdapter
import com.critetiontech.ctvitalio.adapter.ChallengesAdapter
import com.critetiontech.ctvitalio.adapter.NewChallengedAdapter
import com.critetiontech.ctvitalio.databinding.FragmentAllergiesBinding
import com.critetiontech.ctvitalio.databinding.FragmentChallengeDetailsBinding
import com.critetiontech.ctvitalio.databinding.FragmentChallengesBinding
import com.critetiontech.ctvitalio.databinding.FragmentIntakePillsBinding
import com.critetiontech.ctvitalio.model.NewChallengeModel
import com.critetiontech.ctvitalio.utils.LoaderUtils.hideLoading
import com.critetiontech.ctvitalio.utils.LoaderUtils.showLoading
import com.critetiontech.ctvitalio.viewmodel.AllergiesViewModel
import com.critetiontech.ctvitalio.viewmodel.ChallengesViewModel
import com.critetiontech.ctvitalio.viewmodel.IntakePillsViewModel
import com.google.android.material.tabs.TabLayoutMediator
import okio.utf8Size
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


class ChallengeDetailsFragment : Fragment() {
    private lateinit var binding: FragmentChallengeDetailsBinding
    private lateinit var viewModel: ChallengesViewModel

    private var challenges: NewChallengeModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentChallengeDetailsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ChallengesViewModel::class.java]


        challenges = arguments?.getSerializable("challenges") as? NewChallengeModel
        binding.title.text= challenges?.title.toString()
        binding.detailId.text= challenges?.description.toString()
        binding.rewardPoints.text= challenges?.rewardPoints.toString()


        if (challenges?.startsIn?.equals("Started", ignoreCase = true) == true) {
            binding.startTitle.text = "Ended In"
        } else {
            binding.startTitle.text = "Starting In"
        }

        if (challenges?.getPeopleJoinedList()?.size!! >2){
            binding.topId.visibility=View.VISIBLE
        }
        else{
            binding.topId.visibility=View.GONE
        }


        binding.leaveChallengesId.visibility=View.GONE

        binding.ivCheck.visibility=View.GONE
        binding.tvChallengeJoined.text = "Join Now! "
        if(challenges?.getPeopleJoinedList()?.any { it.empId.toString() == PrefsManager().getPatient()?.empId.toString() } == true){

            binding.tvChallengeJoined.text = "Challenge Joined! "
            binding.ivCheck.visibility=View.VISIBLE
            binding.leaveChallengesId.visibility=View.VISIBLE

        }
        binding.joinpId.text= challenges?.getPeopleJoinedList()?.size.toString()+" people to join you in this challenge"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startCountdown(challenges?.startDate.toString(),challenges?.endDate.toString())
        }

        binding.participantsRecyclerView.adapter = challenges?.getPeopleJoinedList()?.let {
            ChallengeDetailsAdapter(
                it,  )
        }

        binding.joidId.setOnClickListener(){
            val currentUserId = PrefsManager().getPatient()?.empId.toString()

            val hasJoined = challenges
                ?.getPeopleJoinedList()
                ?.any { it.empId.toString() == currentUserId } == true

            if (!hasJoined) {
                viewModel.insertChallengeparticipants(
                    challengesId = challenges?.id.toString()
                )
            }
        }

        binding.leaveChallengesId.setOnClickListener(){
            viewModel.leaveChallenge(
                challenges?.id.toString()
            )
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startCountdown(startDateString: String, endDateString: String) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        val startMillis = LocalDateTime.parse(startDateString, formatter)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val endMillis = LocalDateTime.parse(endDateString, formatter)
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                val now = System.currentTimeMillis()

                when {
                    now < startMillis -> {
                        // Before start time → countdown to start
                        val diffMillis = startMillis - now
                        binding.countdownText.text = formatCountdown(diffMillis)
                    }
                    now in startMillis..endMillis -> {
                        // Between start and end → countdown to end
                        val diffMillis = endMillis - now
                        binding.countdownText.text = formatCountdown(diffMillis)
                    }
                    else -> {
                        // After end time
                        binding.countdownText.text = " "
                        handler.removeCallbacks(this) // stop updates
                        return
                    }
                }

                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun formatCountdown(diffMillis: Long): String {
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diffMillis) % 60

        return String.format(
            "%dd : %02dh : %02dm : %02ds",
            days, hours, minutes, seconds
        )
    }
}