package com.critetiontech.ctvitalio.UI.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.databinding.FragmentFocusShiftViewBinding
import com.critetiontech.ctvitalio.utils.FocusShiftAnimationManager
import com.critetiontech.ctvitalio.UI.ui.EndSessionBottomSheet
import com.critetiontech.ctvitalio.viewmodel.FocusShiftViewModel
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FocusShiftView.newInstance] factory method to
 * create an instance of this fragment.
 */
class FocusShiftView : Fragment() {
    private var _binding: FragmentFocusShiftViewBinding?= null
    private val binding get() = _binding!!

    private val viewModel: FocusShiftViewModel by viewModels()
    private var animationManager: FocusShiftAnimationManager? = null

    // Session Timer Animator
    private var timerAnimator: ValueAnimator? = null

    // Flag to track if the initial countdown animation has been run
    private var isCountdownCompleted = false
    private var startTimeMillis: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFocusShiftViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Enable full-screen edge-to-edge layout
        activity?.window?.let { window ->
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        startTimeMillis = System.currentTimeMillis()

        // Initialize hand drawables
        binding.ivLeftHand.setImageResource(R.drawable.iv_left_hand)
        binding.ivRightHand.setImageResource(R.drawable.iv_right_hand)
        binding.ivSingleFinger.setImageResource(R.drawable.iv_single_finger)

        // Initialize Animation Manager
        animationManager = FocusShiftAnimationManager(
            tvCountdown = binding.tvCountdown,
            ivLeftHand = binding.ivLeftHand,
            ivRightHand = binding.ivRightHand,
            layoutHands = binding.layoutHands,
            ivSingleFinger = binding.ivSingleFinger,
            ambientGlow = binding.ambientGlow,
            onStateChanged = { state, guide, description ->
                viewModel.updateInstructions(guide, description)
                // Start the 12-second exercise timer only after the countdown finishes and Stage 1 begins
                if (state == FocusShiftAnimationManager.State.STAGE_1) {
                    if (viewModel.isPlaying.value == true && timerAnimator?.isStarted == false) {
                        timerAnimator?.start()
                    }
                }
            }
        )

        // Setup reverse timer animator
        setupTimerAnimator()

        // Observe ViewModel States
        setupObservers()

        // Play/Pause button action
        binding.btnPlayPause.setOnClickListener {
            val isPlaying = viewModel.isPlaying.value ?: true
            viewModel.setPlaying(!isPlaying)
        }

        // End button action
        binding.btnEnd.setOnClickListener {
            openEndSessionBottomSheet()
        }

        // Listen for bottom sheet confirmation results
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

        // Auto-start animations on page load
        if (viewModel.isPlaying.value == true) {
            resumeAll()
        }
    }

    private fun setupObservers() {
        // Observe playing state
        viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            if (isPlaying) {
                resumeAll()
                binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
            } else {
                pauseAll()
                binding.btnPlayPause.setImageResource(R.drawable.ic_play)
            }
        }

        // Observe timer changes
        viewModel.timeLeftSeconds.observe(viewLifecycleOwner) { seconds ->
            val min = seconds / 60
            val sec = seconds % 60
            binding.tvTimer.text = String.format("%02d:%02d", min, sec)
        }

        // Observe instructional guide titles
        viewModel.focusGuideText.observe(viewLifecycleOwner) { guide ->
            binding.tvFocusGuide.text = guide
        }

        // Observe instructional descriptions
        viewModel.descriptionText.observe(viewLifecycleOwner) { description ->
            binding.tvDescription.text = description
        }
    }

    private fun setupTimerAnimator() {
        val maxDuration = viewModel.timeLeftSeconds.value ?: 12
        timerAnimator = ValueAnimator.ofInt(maxDuration, 0).apply {
            duration = (maxDuration * 1000).toLong()
            interpolator = LinearInterpolator()

            addUpdateListener { animator ->
                val remainingSeconds = animator.animatedValue as Int
                viewModel.setTimeLeft(remainingSeconds)
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (viewModel.timeLeftSeconds.value == 0) {
                        onSessionComplete()
                    }
                }
            })
        }
    }

    private fun resumeAll() {
        val remaining = viewModel.timeLeftSeconds.value ?: 12

        if (remaining == 0) {
            // Replay state: reset timer, skip countdown and start directly from hand sequence
            viewModel.setTimeLeft(12)
            timerAnimator?.cancel()
            setupTimerAnimator()

            isCountdownCompleted = true
            animationManager?.stop()
            animationManager?.start(skipCountdown = true)
        } else if (remaining == 12 && !isCountdownCompleted) {
            // First time entry: show countdown
            isCountdownCompleted = true
            animationManager?.start(skipCountdown = false)
        } else {
            // Resuming from pause: keep hands visible, continue from current animation frame/timer
            animationManager?.resume()
            timerAnimator?.let {
                if (it.isStarted) {
                    it.resume()
                } else {
                    it.start()
                }
            }
        }
    }

    private fun pauseAll() {
        animationManager?.pause()
        timerAnimator?.let {
            if (it.isStarted) {
                it.pause()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isPlaying.value == true) {
            resumeAll()
        }
    }

    override fun onPause() {
        super.onPause()
        pauseAll()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        animationManager?.stop()
        animationManager = null
        timerAnimator?.cancel()
        timerAnimator = null
        _binding = null
    }

    private fun openEndSessionBottomSheet() {
        viewModel.setPlaying(false)
        val durationSeconds = ((System.currentTimeMillis() - startTimeMillis) / 1000).toInt()
        val exerciseId = arguments?.getString("exerciseId")?.toIntOrNull() ?: 0
        val timeLeftSeconds = viewModel.timeLeftSeconds.value ?: 0
        val mindfulnessData = mapOf(
            "exerciseName" to "Focus Shift",
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
            description = "Take a moment to finish your session mindfully. Completing the Focus Shift session will boost your progress stats."
        )
        bottomSheet.show(parentFragmentManager, EndSessionBottomSheet.TAG)
    }

    private fun resumeSession() {
        viewModel.setPlaying(true)
    }

    private fun endSessionAndExit() {
        Toast.makeText(context, "Session Incomplete. Progress not saved.", Toast.LENGTH_SHORT).show()
        exitFragment()
    }

    private fun onSessionComplete() {
        Toast.makeText(context, "Focus Shift Session Completed! Progress Saved.", Toast.LENGTH_LONG).show()
        viewModel.setPlaying(false)
    }

    private fun exitFragment() {
        if (!parentFragmentManager.popBackStackImmediate()) {
            activity?.finish()
        }
    }
}