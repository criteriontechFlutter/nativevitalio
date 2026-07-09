package com.critetiontech.ctvitalio.UI.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentCrissCrossViewBinding
import com.critetiontech.ctvitalio.UI.ui.EndSessionBottomSheet

class CrissCrossFragment : Fragment() {

    private var _binding: FragmentCrissCrossViewBinding? = null

    private val binding get() = _binding!!

    // State properties
    private var isPlaying = true
    private var timeLeftSeconds = 120
    private var activeIndex = 0

    // Animators & Handlers
    private var timerAnimator: ValueAnimator? = null
    private var movementAnimator: ValueAnimator? = null
    private val jumpHandler = Handler(Looper.getMainLooper())
    private var startTimeMillis: Long = 0

    private var pathStep = 0
    private val jumpRunnable = object : Runnable {
        override fun run() {
            if (isPlaying) {
                transitionToNextDot()
                scheduleNextJump()
            }
        }
    }

    private val dots by lazy {
        listOf(
            binding.dotTopLeft,
            binding.dotTopRight,
            binding.dotBottomLeft,
            binding.dotBottomRight
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrissCrossViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Enable full-screen edge-to-edge layout
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }
        Glide.with(requireContext())
            .asGif()
            .load(R.drawable.exercisebg)
            .into(binding.imgGifBackground)
        setupInitialDotsState()
        setupControls()
        setupTimerAnimator()

        // Start session
        isPlaying = true
        startTimeMillis = System.currentTimeMillis()
        timerAnimator?.start()
        scheduleNextJump()
    }

    private fun setupInitialDotsState() {
        // Position 0 (top-left) starts as active, others as inactive
        activeIndex = 0
        dots.forEach { imageView ->
            imageView.setImageResource(R.drawable.bg_criss_cross_inactive_dot)
            imageView.scaleX = 1.0f
            imageView.scaleY = 1.0f
        }
        binding.crissCrossLineView.startPointIndex = activeIndex
        binding.crissCrossLineView.endPointIndex = activeIndex
        binding.crissCrossLineView.progress = 1.0f
    }

    private fun setupControls() {
        // Play/Pause button
        binding.btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        // End button
        binding.btnEnd.setOnClickListener {
            openEndSessionBottomSheet()
        }

        // Listen for custom bottom sheet results
        parentFragmentManager.setFragmentResultListener(
            EndSessionBottomSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val action = bundle.getString(EndSessionBottomSheet.EXTRA_ACTION)
            if (action == "success") {
                onSessionComplete()
                exitFragment()
            } else if (action == "end") {
                endSessionAndExit()
            } else {
                resumeSession()
            }
        }
    }

    private fun setupTimerAnimator() {
        timerAnimator = ValueAnimator.ofInt(timeLeftSeconds, 0).apply {
            duration = (timeLeftSeconds * 1000).toLong()
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                timeLeftSeconds = animator.animatedValue as Int
                updateTimerText()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (timeLeftSeconds == 0) {
                        onSessionComplete()
                    }
                }
            })
        }
    }

    private fun transitionToNextDot() {
        if (!isAdded || _binding == null) return

        val startPoint: Int
        val endPoint: Int

        when (pathStep) {
            0 -> {
                // Diagonal 1: Top Left -> Bottom Right
                startPoint = 0
                endPoint = 3
            }
            1 -> {
                // Connect: Bottom Right -> Top Right
                startPoint = 1
                endPoint = 1
            }
            2 -> {
                // Diagonal 2: Top Right -> Bottom Left
                startPoint = 1
                endPoint = 2
            }
            else -> {
                // Connect: Bottom Left -> Top Left
                startPoint = 0
                endPoint = 0
            }
        }

        pathStep = (pathStep + 1) % 4

        movementAnimator?.cancel()

        binding.crissCrossLineView.startPointIndex = startPoint
        binding.crissCrossLineView.endPointIndex = endPoint
        binding.crissCrossLineView.progress = 0f

        movementAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000L
            interpolator = LinearInterpolator()

            addUpdateListener {
                binding.crissCrossLineView.progress = it.animatedValue as Float
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    activeIndex = endPoint
                }
            })

            start()
        }
    }

    private fun scheduleNextJump() {
        jumpHandler.removeCallbacks(jumpRunnable)
        // Random interval between 1000ms and 2000ms
        val randomDelay = (1000..2000).random().toLong()
        jumpHandler.postDelayed(jumpRunnable, randomDelay)
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            pauseSession()
        } else {
            resumeSession()
        }
    }

    private fun pauseSession() {
        isPlaying = false
        binding.btnPlayPause.setImageResource(R.drawable.ic_play)
        timerAnimator?.pause()
        movementAnimator?.pause()
        jumpHandler.removeCallbacks(jumpRunnable)
    }

    private fun resumeSession() {
        isPlaying = true
        binding.btnPlayPause.setImageResource(R.drawable.ic_pause)

        // Resume timer
        if (timerAnimator?.isStarted == true) {
            timerAnimator?.resume()
        } else {
            timerAnimator?.setIntValues(timeLeftSeconds, 0)
            timerAnimator?.duration = (timeLeftSeconds * 1000).toLong()
            timerAnimator?.start()
        }

        // Resume movement
        movementAnimator?.resume()

        // Restart jumping loop
        scheduleNextJump()
    }

    private fun updateTimerText() {
        val min = timeLeftSeconds / 60
        val sec = timeLeftSeconds % 60
        binding.tvTimer.text = String.format("%02d:%02d", min, sec)
    }

    private fun openEndSessionBottomSheet() {
        pauseSession()
        val durationSeconds = ((System.currentTimeMillis() - startTimeMillis) / 1000).toInt()
        val exerciseId = arguments?.getString("exerciseId")?.toIntOrNull() ?: 0
        val mindfulnessData = mapOf(
            "exerciseName" to "Criss-Cross Focus",
            "timeLeftSeconds" to timeLeftSeconds,
            "completed" to (timeLeftSeconds == 0)
        )
        val mindfulnessJson = com.google.gson.Gson().toJson(mindfulnessData)
        val totalSteps = if (timeLeftSeconds == 0) 1 else 0

        val bottomSheet = EndSessionBottomSheet.newInstance(
            exerciseId = exerciseId,
            duration = durationSeconds,
            totalSteps = totalSteps,
            mindfulnessJson = mindfulnessJson,
            title = "Incomplete Session!",
            description = "Take a moment to finish your session mindfully. Completing the Criss-Cross Focus session will boost your progress stats."
        )
        bottomSheet.show(parentFragmentManager, EndSessionBottomSheet.TAG)
    }

    private fun endSessionAndExit() {
        Toast.makeText(context, "Session Incomplete. Progress not saved.", Toast.LENGTH_SHORT).show()
        exitFragment()
    }

    private fun onSessionComplete() {
        Toast.makeText(context, "Session Completed! Progress Saved.", Toast.LENGTH_LONG).show()
        pauseSession()
    }

    private fun exitFragment() {
        if (!parentFragmentManager.popBackStackImmediate()) {
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Prevent memory leaks
        timerAnimator?.cancel()
        movementAnimator?.cancel()
        jumpHandler.removeCallbacks(jumpRunnable)
        _binding = null
    }
}
