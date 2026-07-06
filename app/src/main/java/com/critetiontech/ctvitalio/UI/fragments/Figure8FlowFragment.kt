package com.critetiontech.ctvitalio.UI.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentFigure8FlowBinding
import com.critetiontech.ctvitalio.UI.ui.EndSessionBottomSheet

class Figure8FlowFragment : Fragment() {

    private var _binding: FragmentFigure8FlowBinding? = null
    private val binding get() = _binding!!

    // Speed states
    enum class Speed(val durationMs: Long) {
        SLOW(8000L),
        MEDIUM(5000L),
        FAST(3000L)
    }

    private var currentSpeed = Speed.MEDIUM
    private var isPlaying = true
    private var isCountdownFinished = false
    private var timeLeftSeconds = 120

    // Animators
    private var countdownAnimator: ValueAnimator? = null
    private var exerciseAnimator: ValueAnimator? = null
    private var timerAnimator: ValueAnimator? = null
    private var startTimeMillis: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFigure8FlowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Enable full-screen edge-to-edge layout
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        setupControls()
        setupSpeedControl()
        setupCountdownAnimator()
        setupExerciseAnimator()
        setupTimerAnimator()

        startTimeMillis = System.currentTimeMillis()
        // Start countdown initially
        startCountdown()
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

        // Listen for bottom sheet results
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

    private fun setupSpeedControl() {
        binding.btnSpeedSlow.setOnClickListener {
            updateSpeed(Speed.SLOW)
        }
        binding.btnSpeedMedium.setOnClickListener {
            updateSpeed(Speed.MEDIUM)
        }
        binding.btnSpeedFast.setOnClickListener {
            updateSpeed(Speed.FAST)
        }

        // Initial visual state
        updateSpeedVisuals()
    }

    private fun updateSpeed(speed: Speed) {
        currentSpeed = speed
        updateSpeedVisuals()

        // Dynamically adjust ongoing animator duration
        exerciseAnimator?.let { animator ->
            val wasRunning = animator.isRunning
            if (wasRunning) {
                animator.duration = currentSpeed.durationMs
            }
        }
    }

    private fun updateSpeedVisuals() {
        val selectedBg = R.drawable.bg_speed_selected
        val unselectedBg = 0 // transparent

        binding.btnSpeedSlow.setBackgroundResource(if (currentSpeed == Speed.SLOW) selectedBg else unselectedBg)
        binding.btnSpeedMedium.setBackgroundResource(if (currentSpeed == Speed.MEDIUM) selectedBg else unselectedBg)
        binding.btnSpeedFast.setBackgroundResource(if (currentSpeed == Speed.FAST) selectedBg else unselectedBg)
    }

    private fun setupCountdownAnimator() {
        countdownAnimator = ValueAnimator.ofInt(3, 0).apply {
            duration = 3000
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                val progress = animator.animatedFraction
                val currentNumber = 3 - (progress * 3).toInt()
                if (currentNumber in 1..3) {
                    binding.tvCountdown.text = currentNumber.toString()
                }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (!isCountdownFinished && isPlaying) {
                        isCountdownFinished = true
                        startExercise()
                    }
                }
            })
        }
    }

    private fun setupExerciseAnimator() {
        // Linear path progress from 0f to 1f
        exerciseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = currentSpeed.durationMs
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator() // Ease-in-out movement around curves
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                binding.figure8View.progress = progress
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

    private fun startCountdown() {
        binding.tvCountdown.visibility = View.VISIBLE
        binding.figure8View.visibility = View.INVISIBLE
        binding.figure8View.showDot = false

        if (isPlaying) {
            countdownAnimator?.start()
        }
    }

    private fun startExercise() {
        binding.tvCountdown.visibility = View.GONE
        binding.figure8View.visibility = View.VISIBLE
        binding.figure8View.showDot = true

        if (isPlaying) {
            exerciseAnimator?.start()
            // Reset and start/resume timer animator
            timerAnimator?.setIntValues(timeLeftSeconds, 0)
            timerAnimator?.duration = (timeLeftSeconds * 1000).toLong()
            timerAnimator?.start()
        }
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

        if (!isCountdownFinished) {
            countdownAnimator?.pause()
        } else {
            exerciseAnimator?.pause()
            timerAnimator?.pause()
        }
    }

    private fun resumeSession() {
        isPlaying = true
        binding.btnPlayPause.setImageResource(R.drawable.ic_pause)

        if (!isCountdownFinished) {
            if (countdownAnimator?.isStarted == true) {
                countdownAnimator?.resume()
            } else {
                countdownAnimator?.start()
            }
        } else {
            if (exerciseAnimator?.isStarted == true) {
                exerciseAnimator?.resume()
            } else {
                // Ensure duration is synchronized
                exerciseAnimator?.duration = currentSpeed.durationMs
                exerciseAnimator?.start()
            }

            // Resume timer
            if (timerAnimator?.isStarted == true) {
                timerAnimator?.resume()
            } else {
                timerAnimator?.setIntValues(timeLeftSeconds, 0)
                timerAnimator?.duration = (timeLeftSeconds * 1000).toLong()
                timerAnimator?.start()
            }
        }
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
            "exerciseName" to "Figure 8 Flow",
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
            description = "Take a moment to finish your session mindfully. Completing the Figure 8 Flow session will boost your progress stats."
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
        // Cancel all animators to prevent memory leaks
        countdownAnimator?.cancel()
        exerciseAnimator?.cancel()
        timerAnimator?.cancel()
        _binding = null
    }
}
