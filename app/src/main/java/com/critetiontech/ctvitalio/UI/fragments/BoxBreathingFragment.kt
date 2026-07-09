package com.critetiontech.ctvitalio.UI.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.UI.BoxBreathingView
import com.critetiontech.ctvitalio.UI.BoxBreathingViewModel
import com.critetiontech.ctvitalio.databinding.FragmentBoxBreathingBinding
import java.util.Locale

class BoxBreathingFragment : Fragment() {

    private var _binding: FragmentBoxBreathingBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BoxBreathingViewModel
    private var breathingAnimator: ValueAnimator? = null
    
    private val timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    
    private val countdownHandler = Handler(Looper.getMainLooper())
    private var countdownRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoxBreathingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[BoxBreathingViewModel::class.java]

        initClickListeners()
        initObservers()
        startCountdown()
    }

    private fun initClickListeners() {
        binding.btnPlayPause.setOnClickListener {
            val playing = viewModel.isPlaying.value
            if (playing != null) {
                viewModel.setPlaying(!playing)
            }
        }

        binding.btnEnd.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initObservers() {
        // Observe Countdown State
        viewModel.countdownSeconds.observe(viewLifecycleOwner) { seconds ->
            if (seconds != null) {
                binding.tvCountdown.text = seconds.toString()
            }
        }

        viewModel.isCountdownActive.observe(viewLifecycleOwner) { active ->
            if (active != null && !active) {
                binding.countdownContainer.visibility = View.GONE
                binding.boxBreathingView.visibility = View.VISIBLE
                binding.tvPhaseText.visibility = View.VISIBLE
                
                // Start breathing phase
                startBreathingAnimation()
                startTimer()
            }
        }

        // Observe Breathing Progress
        viewModel.breathingProgress.observe(viewLifecycleOwner) { progress ->
            if (progress != null) {
                binding.boxBreathingView.setProgress(progress)
            }
        }

        // Observe Current Phase Text (with transition animation)
        viewModel.currentPhaseText.observe(viewLifecycleOwner) { phaseText ->
            if (phaseText != null) {
                updatePhaseTextWithFade(phaseText)
            }
        }

        // Observe Timer
        viewModel.elapsedTimeSeconds.observe(viewLifecycleOwner) { elapsedSeconds ->
            if (elapsedSeconds != null) {
                val minutes = elapsedSeconds / 60
                val seconds = elapsedSeconds % 60
                binding.tvTimer.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            }
        }

        // Observe Play/Pause State
        viewModel.isPlaying.observe(viewLifecycleOwner) { playing ->
            if (playing != null) {
                if (playing) {
                    binding.ivPlayPauseIcon.setImageResource(R.drawable.ic_pause)
                    resumeBreathingAnimation()
                } else {
                    binding.ivPlayPauseIcon.setImageResource(R.drawable.ic_play)
                    pauseBreathingAnimation()
                }
            }
        }
    }

    private fun startCountdown() {
        countdownRunnable = object : Runnable {
            override fun run() {
                val current = viewModel.countdownSeconds.value
                if (current != null) {
                    if (current > 1) {
                        viewModel.setCountdownSeconds(current - 1)
                        countdownHandler.postDelayed(this, 1000)
                    } else {
                        viewModel.setCountdownActive(false)
                    }
                }
            }
        }
        countdownRunnable?.let { countdownHandler.postDelayed(it, 1000) }
    }

    private fun startBreathingAnimation() {
        breathingAnimator = ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            duration = viewModel.totalCycleDurationMs.toLong()
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                viewModel.setBreathingProgress(progress)
            }
        }
        
        val isPlaying = viewModel.isPlaying.value
        if (isPlaying != null && isPlaying) {
            breathingAnimator?.start()
        }
    }

    private fun pauseBreathingAnimation() {
        if (breathingAnimator?.isRunning == true) {
            breathingAnimator?.pause()
        }
        stopTimer()
    }

    private fun resumeBreathingAnimation() {
        breathingAnimator?.let {
            if (it.isPaused) {
                it.resume()
            } else if (!it.isRunning) {
                it.start()
            }
        }
        startTimer()
    }

    private fun startTimer() {
        // Stop any existing timer first
        stopTimer()

        timerRunnable = object : Runnable {
            override fun run() {
                viewModel.incrementElapsedTime()
                timerHandler.postDelayed(this, 1000)
            }
        }
        
        // Only run timer if countdown is finished and currently playing
        val countdownActive = viewModel.isCountdownActive.value
        val playing = viewModel.isPlaying.value
        if (countdownActive != null && !countdownActive && playing != null && playing) {
            timerRunnable?.let { timerHandler.postDelayed(it, 1000) }
        }
    }

    private fun stopTimer() {
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
    }

    private fun updatePhaseTextWithFade(newText: String) {
        if (_binding == null || binding.tvPhaseText.visibility != View.VISIBLE) {
            _binding?.tvPhaseText?.text = newText
            return
        }

        // Fade out
        binding.tvPhaseText.animate()
            .alpha(0.0f)
            .setDuration(200)
            .withEndAction {
                _binding?.let {
                    it.tvPhaseText.text = newText
                    // Fade in
                    it.tvPhaseText.animate()
                        .alpha(1.0f)
                        .setDuration(200)
                        .start()
                }
            }
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countdownRunnable?.let { countdownHandler.removeCallbacks(it) }
        timerRunnable?.let { timerHandler.removeCallbacks(it) }
        breathingAnimator?.cancel()
        _binding = null
    }
}
