package com.critetiontech.ctvitalio.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView


class FocusShiftAnimationManager(
    private val tvCountdown: TextView,
    private val ivLeftHand: ImageView,
    private val ivRightHand: ImageView,
    private val layoutHands: View,
    private val ivSingleFinger: ImageView,
    private val ambientGlow: View,
    private val onStateChanged: (State, String, String) -> Unit
) {

    enum class State {
        COUNTDOWN,
        STAGE_1,      // Stage 1: Left and right hands entry (1000ms)
        STAGE_2,      // Stage 2: Gradually apply blur (1000ms)
        STAGE_3,      // Stage 3: Front hand entry (1000ms)
        STAGE_4_CLEAR, // Stage 4: Loop clear state (1500ms)
        STAGE_4_BLUR  // Stage 4: Loop blur state (1500ms)
    }

    private var currentState = State.COUNTDOWN
    private var activeAnimatorSet: AnimatorSet? = null
    private var isPaused = false

    init {
        resetViews()
    }

    /**
     * Resets all visual properties to initial state.
     */
    fun resetViews() {
        tvCountdown.visibility = View.INVISIBLE
        ivLeftHand.visibility = View.INVISIBLE
        ivRightHand.visibility = View.INVISIBLE
        ivSingleFinger.visibility = View.INVISIBLE
        layoutHands.alpha = 1.0f
        layoutHands.translationX = 0f
        layoutHands.translationY = 0f
        layoutHands.scaleX = 1.0f
        layoutHands.scaleY = 1.0f
        ivLeftHand.translationX = 0f
        ivRightHand.translationX = 0f
        ivSingleFinger.alpha = 0f
        ivSingleFinger.scaleX = 1.0f
        ivSingleFinger.scaleY = 1.0f
        clearBlur(layoutHands)
    }

    /**
     * Starts the exercise sequence.
     * @param skipCountdown If true, bypasses the initial countdown and goes directly to Stage 1.
     */
    fun start(skipCountdown: Boolean = false) {
        isPaused = false
        currentState = if (skipCountdown) State.STAGE_1 else State.COUNTDOWN
        runNextPhase()
    }

    /**
     * Pauses all ongoing animators.
     */
    fun pause() {
        isPaused = true
        activeAnimatorSet?.pause()
    }

    /**
     * Resumes animators from paused position.
     */
    fun resume() {
        if (!isPaused) return
        isPaused = false
        activeAnimatorSet?.resume()
    }

    /**
     * Fully stops and resets animators.
     */
    fun stop() {
        isPaused = false
        cancelAll()
        resetViews()
    }

    private fun cancelAll() {
        activeAnimatorSet?.cancel()
        activeAnimatorSet = null
        clearBlur(layoutHands)
    }

    private fun runNextPhase() {
        if (isPaused) return
        cancelAll()

        when (currentState) {
            State.COUNTDOWN -> startCountdownPhase()
            State.STAGE_1 -> startStage1()
            State.STAGE_2 -> startStage2()
            State.STAGE_3 -> startStage3()
            State.STAGE_4_CLEAR -> startStage4Clear()
            State.STAGE_4_BLUR -> startStage4Blur()
        }
    }

    // ==========================================
    // COUNTDOWN (3, 2, 1)
    // ==========================================
    private fun startCountdownPhase() {
        onStateChanged(State.COUNTDOWN, "Take Position", "Get ready for the exercise...")
        tvCountdown.visibility = View.VISIBLE
        tvCountdown.text = "3"
        tvCountdown.alpha = 0f
        tvCountdown.scaleX = 0.5f
        tvCountdown.scaleY = 0.5f

        val numberAnimator = ValueAnimator.ofInt(3, 0).apply {
            duration = 3000
            interpolator = LinearInterpolator()
            var currentNum = 3

            addUpdateListener { animator ->
                val remaining = 3 - (animator.animatedFraction * 3).toInt()
                if (remaining in 1..3 && remaining != currentNum) {
                    currentNum = remaining
                    tvCountdown.text = currentNum.toString()
                }
            }
        }

        val scaleX = ObjectAnimator.ofFloat(tvCountdown, "scaleX", 0.5f, 1.2f, 0.8f).apply {
            duration = 3000
        }
        val scaleY = ObjectAnimator.ofFloat(tvCountdown, "scaleY", 0.5f, 1.2f, 0.8f).apply {
            duration = 3000
        }
        val alpha = ObjectAnimator.ofFloat(tvCountdown, "alpha", 0f, 1f, 0.2f).apply {
            duration = 3000
        }

        activeAnimatorSet = AnimatorSet().apply {
            playTogether(numberAnimator, scaleX, scaleY, alpha)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    tvCountdown.visibility = View.INVISIBLE
                    currentState = State.STAGE_1
                    runNextPhase()
                }
            })
            start()
        }
    }

    // ==========================================
    // STAGE 1: Left and right hands entry (1000ms)
    // ==========================================
    private fun startStage1() {
        onStateChanged(State.STAGE_1, "Take Position", "Left and right hands move to center...")

        ivLeftHand.visibility = View.VISIBLE
        ivRightHand.visibility = View.VISIBLE

        val screenWidth = layoutHands.width.toFloat().let { if (it <= 0) 1000f else it }
        ivLeftHand.translationX = -screenWidth / 2f
        ivRightHand.translationX = screenWidth / 2f
        ivLeftHand.alpha = 0f
        ivRightHand.alpha = 0f
        ivLeftHand.scaleX = 0.8f
        ivLeftHand.scaleY = 0.8f
        ivRightHand.scaleX = 0.8f
        ivRightHand.scaleY = 0.8f

        val slideLeft = ObjectAnimator.ofFloat(ivLeftHand, "translationX", 0f)
        val slideRight = ObjectAnimator.ofFloat(ivRightHand, "translationX", 0f)
        val alphaLeft = ObjectAnimator.ofFloat(ivLeftHand, "alpha", 1f)
        val alphaRight = ObjectAnimator.ofFloat(ivRightHand, "alpha", 1f)
        val scaleLeftX = ObjectAnimator.ofFloat(ivLeftHand, "scaleX", 1f)
        val scaleLeftY = ObjectAnimator.ofFloat(ivLeftHand, "scaleY", 1f)
        val scaleRightX = ObjectAnimator.ofFloat(ivRightHand, "scaleX", 1f)
        val scaleRightY = ObjectAnimator.ofFloat(ivRightHand, "scaleY", 1f)

        activeAnimatorSet = AnimatorSet().apply {
            duration = 1000
            interpolator = DecelerateInterpolator()
            playTogether(slideLeft, slideRight, alphaLeft, alphaRight, scaleLeftX, scaleLeftY, scaleRightX, scaleRightY)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentState = State.STAGE_2
                    runNextPhase()
                }
            })
            start()
        }
    }

    // ==========================================
    // STAGE 2: Gradually apply blur (1000ms)
    // ==========================================
    private fun startStage2() {
        onStateChanged(State.STAGE_2, "Take Position", "Gradually blurring background hands...")

        val blurAnimator = ValueAnimator.ofFloat(0.1f, 25f).apply {
            addUpdateListener { animator ->
                val radius = animator.animatedValue as Float
                applyBlurRadius(radius)
            }
        }

        activeAnimatorSet = AnimatorSet().apply {
            duration = 1000
            interpolator = AccelerateDecelerateInterpolator()
            play(blurAnimator)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentState = State.STAGE_3
                    runNextPhase()
                }
            })
            start()
        }
    }

    // ==========================================
    // STAGE 3: Front hand entry (1000ms)
    // ==========================================
    private fun startStage3() {
        onStateChanged(State.STAGE_3, "Focus Near", "Gently focus on the front hand...")

        ivSingleFinger.visibility = View.VISIBLE
        ivSingleFinger.alpha = 0f
        ivSingleFinger.scaleX = 0.8f
        ivSingleFinger.scaleY = 0.8f

        val scaleX = ObjectAnimator.ofFloat(ivSingleFinger, "scaleX", 1.0f)
        val scaleY = ObjectAnimator.ofFloat(ivSingleFinger, "scaleY", 1.0f)
        val alpha = ObjectAnimator.ofFloat(ivSingleFinger, "alpha", 1f)

        activeAnimatorSet = AnimatorSet().apply {
            duration = 1000
            interpolator = OvershootInterpolator(1.1f)
            playTogether(scaleX, scaleY, alpha)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentState = State.STAGE_4_CLEAR
                    runNextPhase()
                }
            })
            start()
        }
    }

    // ==========================================
    // STAGE 4 (Loop Phase A): Front hand fades out, background hands clear & move (1500ms)
    // ==========================================
    private fun startStage4Clear() {
        onStateChanged(State.STAGE_4_CLEAR, "Focus Far", "Shift focus to background hands...")

        val fadeOutFront = ObjectAnimator.ofFloat(ivSingleFinger, "alpha", 0f)

        val clearBlurAnimator = ValueAnimator.ofFloat(25f, 0.1f).apply {
            addUpdateListener { animator ->
                val radius = animator.animatedValue as Float
                applyBlurRadius(radius)
            }
        }

        // Slight natural shifting of background hands
        val shiftY = ObjectAnimator.ofFloat(layoutHands, "translationY", 0f, -12f)
        val scaleUpHandsX = ObjectAnimator.ofFloat(layoutHands, "scaleX", 1f, 1.03f)
        val scaleUpHandsY = ObjectAnimator.ofFloat(layoutHands, "scaleY", 1f, 1.03f)

        activeAnimatorSet = AnimatorSet().apply {
            duration = 1500
            interpolator = AccelerateDecelerateInterpolator()
            playTogether(fadeOutFront, clearBlurAnimator, shiftY, scaleUpHandsX, scaleUpHandsY)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentState = State.STAGE_4_BLUR
                    runNextPhase()
                }
            })
            start()
        }
    }

    // ==========================================
    // STAGE 4 (Loop Phase B): Background hands blur again, front hand fades in (1500ms)
    // ==========================================
    private fun startStage4Blur() {
        onStateChanged(State.STAGE_4_BLUR, "Focus Near", "Shift focus back to front hand...")

        val fadeInFront = ObjectAnimator.ofFloat(ivSingleFinger, "alpha", 1f)

        val blurAnimator = ValueAnimator.ofFloat(0.1f, 25f).apply {
            addUpdateListener { animator ->
                val radius = animator.animatedValue as Float
                applyBlurRadius(radius)
            }
        }

        // Return background hands to rest position
        val shiftY = ObjectAnimator.ofFloat(layoutHands, "translationY", -12f, 0f)
        val scaleDownHandsX = ObjectAnimator.ofFloat(layoutHands, "scaleX", 1.03f, 1f)
        val scaleDownHandsY = ObjectAnimator.ofFloat(layoutHands, "scaleY", 1.03f, 1f)

        activeAnimatorSet = AnimatorSet().apply {
            duration = 1500
            interpolator = AccelerateDecelerateInterpolator()
            playTogether(fadeInFront, blurAnimator, shiftY, scaleDownHandsX, scaleDownHandsY)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    currentState = State.STAGE_4_CLEAR
                    runNextPhase()
                }
            })
            start()
        }
    }

    private fun applyBlurRadius(radius: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            layoutHands.setRenderEffect(RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP))
        } else {
            // High fidelity fallback for older Android versions
            layoutHands.alpha = 1f - (radius / 25f) * 0.45f
        }
    }

    private fun clearBlur(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            view.setRenderEffect(null)
        }
    }
}
