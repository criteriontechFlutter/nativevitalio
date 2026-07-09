package com.critetiontech.ctvitalio.UI.fragments

import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentDeepBreatheSessionViewBinding
import com.critetiontech.ctvitalio.UI.ui.EndSessionBottomSheet
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class DeepBreatheSessionView : Fragment() {

    private var _binding: FragmentDeepBreatheSessionViewBinding? = null
    private val binding get() = _binding!!

    private var player: ExoPlayer? = null
    private var countDownTimer: CountDownTimer? = null

    private var totalSeconds = 60
    private var remainingSeconds = totalSeconds

    private var isPaused = false
    private var startTimeMillis: Long = 0

    // ---------------- ON CREATE VIEW ----------------
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDeepBreatheSessionViewBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    // ---------------- ON VIEW CREATED ----------------
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.txtTimer.text = "01:00"

        setupVideo()
        startTimer()

        binding.btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        startTimeMillis = System.currentTimeMillis()

        // Listen for bottom sheet results
        parentFragmentManager.setFragmentResultListener(
            EndSessionBottomSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val action = bundle.getString(EndSessionBottomSheet.EXTRA_ACTION)
            if (action == "success") {
                onSessionComplete()
            } else if (action == "end") {
                endSessionAndExit()
            } else {
                resumeSession()
            }
        }

        // End button
        binding.btnEnd.setOnClickListener {
            pauseSession()
            val durationSeconds = ((System.currentTimeMillis() - startTimeMillis) / 1000).toInt()
            val exerciseId = arguments?.getString("exerciseId")?.toIntOrNull() ?: 0
            val mindfulnessData = mapOf(
                "exerciseName" to "Deep Breathing",
                "remainingSeconds" to remainingSeconds,
                "completed" to (remainingSeconds == 0)
            )
            val mindfulnessJson = com.google.gson.Gson().toJson(mindfulnessData)
            val totalSteps = if (remainingSeconds == 0) 1 else 0

            val bottomSheet = EndSessionBottomSheet.newInstance(
                exerciseId = exerciseId,
                duration = durationSeconds,
                totalSteps = totalSteps,
                mindfulnessJson = mindfulnessJson,
                title = "Incomplete Session!",
                description = "Take a moment to finish your session mindfully. Completing the Deep Breathing session will boost your progress stats."
            )
            bottomSheet.show(parentFragmentManager, EndSessionBottomSheet.TAG)
        }
    }

    // ---------------- VIDEO SETUP ----------------
    private fun setupVideo() {

        player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player

        val videoUri = Uri.parse(
//            "android.resource://${requireContext().packageName}/${R.raw.meditation_video}"

            "android.resource://${requireContext().packageName} "
        )

        val mediaItem = MediaItem.fromUri(videoUri)

        player?.apply {
            setMediaItem(mediaItem)
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            prepare()
            play()
        }
    }

    // ---------------- TIMER ----------------
    private fun startTimer() {

        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(remainingSeconds * 1000L, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                remainingSeconds = (millisUntilFinished / 1000).toInt()

                val min = remainingSeconds / 60
                val sec = remainingSeconds % 60

                binding.txtTimer.text = String.format("%02d:%02d", min, sec)
            }

            override fun onFinish() {
                binding.txtTimer.text = "00:00"
            }
        }

        countDownTimer?.start()
    }

    // ---------------- PLAY / PAUSE ----------------
    private fun togglePlayPause() {

        isPaused = !isPaused

        if (isPaused) {

            player?.pause()
            countDownTimer?.cancel()

            binding.btnPlayPause.setImageResource(R.drawable.ic_play)

        } else {

            player?.play()
            startTimer()

            binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
        }
    }

    private fun pauseSession() {
        if (!isPaused) {
            togglePlayPause()
        }
    }

    private fun resumeSession() {
        if (isPaused) {
            togglePlayPause()
        }
    }

    private fun endSessionAndExit() {
        android.widget.Toast.makeText(context, "Session Incomplete. Progress not saved.", android.widget.Toast.LENGTH_SHORT).show()
        exitFragment()
    }

    private fun onSessionComplete() {
        android.widget.Toast.makeText(context, "Deep Breathing Session Completed! Progress Saved.", android.widget.Toast.LENGTH_LONG).show()
        exitFragment()
    }

    private fun exitFragment() {
        if (!parentFragmentManager.popBackStackImmediate()) {
            activity?.finish()
        }
    }

    // ---------------- CLEANUP ----------------
    override fun onDestroyView() {
        super.onDestroyView()

        countDownTimer?.cancel()
        player?.release()
        player = null
        _binding = null
    }
}