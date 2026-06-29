package com.critetiontech.ctvitalio.UI.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.ui.EndSessionBottomSheet
import com.critetiontech.ctvitalio.databinding.FragmentCircleClockBinding

class ClockCircleFragment : Fragment() {

    private var _binding: FragmentCircleClockBinding? = null
    private val binding get() = _binding!!

    // State properties
    private var isPlaying = true
    private var isCountdownFinished = false
    private var timeLeftSeconds = 120

    // Animators
    private var countdownAnimator: ValueAnimator? = null
    private var exerciseAnimator: ValueAnimator? = null
    private var timerAnimator: ValueAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCircleClockBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Enable full-screen edge-to-edge layout
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        setupControls()
        setupCountdownAnimator()
        setupExerciseAnimator()
        setupTimerAnimator()

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
            if (action == "end") {
                endSessionAndExit()
            } else {
                resumeSession()

//                val navController = findNavController()
//
//                navController.navigate(
//                    R.id.action_focusShiftView_to_focusSessionView
//                )
            }
        }
    }

    private fun setupCountdownAnimator() {
        // 3 seconds total duration
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
        // Animate progress from 0f to 4f (4 rounds: 2 rounds anti-clockwise, 2 rounds clockwise)
        // 4 seconds per round = 16 seconds total cycle
        exerciseAnimator = ValueAnimator.ofFloat(0f, 4f).apply {
            duration = 16000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                val progress = animator.animatedValue as Float
                val angle: Float
                val focusText: String

                if (progress < 2f) {
                    // Anti-clockwise rotation: angle decreases from starting 270f (12 o'clock)
                    // 2 rounds: 270f -> -90f -> -450f
                    focusText = "Focus Near"
                    angle = 270f - (progress * 360f)
                } else {
                    // Clockwise rotation: angle increases from -450f
                    // 2 rounds: -450f -> -90f -> 270f
                    focusText = "Focus Far"
                    angle = -450f + ((progress - 2f) * 360f)
                }

                binding.tvFocusGuide.text = focusText
                binding.clockCircleView.currentAngle = angle
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
        binding.clockCircleView.visibility = View.INVISIBLE
        binding.clockCircleView.showDot = false
        binding.tvFocusGuide.text = ""

        if (isPlaying) {
            countdownAnimator?.start()
        }
    }

    private fun startExercise() {
        binding.tvCountdown.visibility = View.GONE
        binding.clockCircleView.visibility = View.VISIBLE
        binding.clockCircleView.showDot = true

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
        val bottomSheet = EndSessionBottomSheet()
        bottomSheet.show(parentFragmentManager, EndSessionBottomSheet.TAG)
    }

    private fun endSessionAndExit() {
        Toast.makeText(context, "Session Incomplete. Progress not saved.", Toast.LENGTH_SHORT).show()
        exitFragment()
    }

    private fun onSessionComplete() {
        Toast.makeText(context, "Session Completed! Progress Saved.", Toast.LENGTH_LONG).show()
        exitFragment()
    }

    private fun exitFragment() {
        if (!parentFragmentManager.popBackStackImmediate()) {
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancel all animators to prevent memory leaks or animation runs after view destruction
        countdownAnimator?.cancel()
        exerciseAnimator?.cancel()
        timerAnimator?.cancel()
        _binding = null
    }
}
