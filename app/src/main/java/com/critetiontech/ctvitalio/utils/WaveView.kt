package com.critetiontech.ctvitalio.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener
import androidx.core.graphics.toColorInt


class WaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,

) : View(context, attrs, defStyleAttr) {

    // Wave offsets
    @Volatile private var offsetBack = -120f
    @Volatile private var offsetFront = -160f

    // Paths - reused to avoid allocations
    private val pathBack = Path()
    private val pathFront = Path()
    private val clipPath = Path()

    // Paints - configured once
    private val paintBack = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val paintFront = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Corner radius
    private val cornerRadius by lazy { 24f.dp() }

    // Cached corner radii array to avoid allocations
    private val cornerRadii = FloatArray(8)

    // Animation references for proper cleanup
    private var dropAnimatorSet: AnimatorSet? = null
    private var floatBackAnimator: ValueAnimator? = null
    private var floatFrontAnimator: ValueAnimator? = null

    // Cached dimensions
    private var cachedWidth = 0f
    private var cachedHeight = 0f

    // Pending colors flag
    private var hasPendingColorUpdate = false
    private var defaultBackgroundColor = "#FDEEEE".toColorInt()
    private var defaultBackWaveColor = "#F6D9D9".toColorInt()
    private var defaultFrontWaveColor = "#F3CACA".toColorInt()


    init {
        // Default colors
        paintBackground.color = defaultBackgroundColor
        paintBack.color = defaultBackWaveColor
        paintFront.color = defaultFrontWaveColor

        // Set corner radii once
        cornerRadii[0] = cornerRadius // top-left x
        cornerRadii[1] = cornerRadius // top-left y
        cornerRadii[2] = cornerRadius // top-right x
        cornerRadii[3] = cornerRadius // top-right y
        // bottom corners remain 0f
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimationSequence()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelAllAnimations()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility != VISIBLE) {
            pauseAnimations()
        } else if (isAttachedToWindow) {
            resumeAnimations()
        }
    }
    /**
     * Update default colors dynamically.
     * If any color is null, existing default will be retained.
     */
    fun setDefaultWaveColors(
        backgroundColor: Int? = null,
        backWaveColor: Int? = null,
        frontWaveColor: Int? = null
    ) {
        backgroundColor?.let {
            defaultBackgroundColor = it
            paintBackground.color = it
        }

        backWaveColor?.let {
            defaultBackWaveColor = it
            paintBack.color = it
        }

        frontWaveColor?.let {
            defaultFrontWaveColor = it
            paintFront.color = it
        }

        invalidate()
        postInvalidateOnAnimation()
    }


    /**
     * Set wave colors dynamically
     */
    fun setWaveColors(
        backgroundColor: Int,
        backWaveColor: Int,
        frontWaveColor: Int
    ) {
        paintBackground.color = backgroundColor
        paintBack.color = backWaveColor
        paintFront.color = frontWaveColor
        hasPendingColorUpdate = true

        // Force immediate redraw with multiple strategies
        invalidate()
        postInvalidate()

        if (isAttachedToWindow) {
            postInvalidateOnAnimation()
        }
    }

    private fun Float.dp(): Float = this * resources.displayMetrics.density

    private fun startAnimationSequence() {
        // Clean up any existing animations
        cancelAllAnimations()

        // Drop animator phase
        val dropBack = ObjectAnimator.ofFloat(this, "offsetBack", -120f, 0f).apply {
            duration = 500
            interpolator = DecelerateInterpolator()
        }

        val dropFront = ObjectAnimator.ofFloat(this, "offsetFront", -160f, 0f).apply {
            duration = 500
            interpolator = DecelerateInterpolator()
        }

        // Float animator phase
        floatBackAnimator = ValueAnimator.ofFloat(-20f, 20f).apply {
            duration = 4500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                offsetBack = animator.animatedValue as Float
                postInvalidateOnAnimation()
            }
        }

        floatFrontAnimator = ValueAnimator.ofFloat(-15f, 15f).apply {
            duration = 3800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                offsetFront = animator.animatedValue as Float
                postInvalidateOnAnimation()
            }
        }

        dropAnimatorSet = AnimatorSet().apply {
            playTogether(dropBack, dropFront)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    floatBackAnimator?.start()
                    floatFrontAnimator?.start()
                }

                override fun onAnimationCancel(animation: Animator) {
                    floatBackAnimator?.cancel()
                    floatFrontAnimator?.cancel()
                }
            })
            start()
        }
    }

    private fun pauseAnimations() {
        floatBackAnimator?.pause()
        floatFrontAnimator?.pause()
        dropAnimatorSet?.pause()
    }

    private fun resumeAnimations() {
        floatBackAnimator?.resume()
        floatFrontAnimator?.resume()
        dropAnimatorSet?.resume()
    }

    private fun cancelAllAnimations() {
        floatBackAnimator?.cancel()
        floatFrontAnimator?.cancel()
        dropAnimatorSet?.cancel()

        floatBackAnimator = null
        floatFrontAnimator = null
        dropAnimatorSet = null
    }

    // Property setters for ObjectAnimator
    @Suppress("unused")
    private fun setOffsetBack(value: Float) {
        offsetBack = value
        postInvalidateOnAnimation()
    }

    @Suppress("unused")
    private fun setOffsetFront(value: Float) {
        offsetFront = value
        postInvalidateOnAnimation()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cachedWidth = w.toFloat()
        cachedHeight = h.toFloat()

        // Recreate clip path with new dimensions
        updateClipPath()
    }

    private fun updateClipPath() {
        if (cachedWidth > 0 && cachedHeight > 0) {
            clipPath.reset()
            cornerRadii[0] = cornerRadius
            cornerRadii[1] = cornerRadius
            cornerRadii[2] = cornerRadius
            cornerRadii[3] = cornerRadius

            clipPath.addRoundRect(
                0f, 0f, cachedWidth, cachedHeight,
                cornerRadii,
                Path.Direction.CW
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (cachedWidth <= 0 || cachedHeight <= 0) return

        // Clear pending color flag
        hasPendingColorUpdate = false

        val saveCount = canvas.save()

        try {
            // Apply rounded corner clipping
            canvas.clipPath(clipPath)

            // Draw background
            canvas.drawRect(0f, 0f, cachedWidth, cachedHeight, paintBackground)

            // Draw back wave
            drawBackWave(canvas)

            // Draw front wave
            drawFrontWave(canvas)

        } finally {
            canvas.restoreToCount(saveCount)
        }
    }

    private fun drawBackWave(canvas: Canvas) {
        pathBack.reset()
        pathBack.moveTo(0f, cachedHeight * 0.32f + offsetBack)
        pathBack.quadTo(
            cachedWidth * 0.45f,
            cachedHeight * (0.50f + offsetBack / 50),
            cachedWidth,
            cachedHeight * 0.35f + offsetBack
        )
        pathBack.lineTo(cachedWidth, cachedHeight)
        pathBack.lineTo(0f, cachedHeight)
        pathBack.close()
        canvas.drawPath(pathBack, paintBack)
    }

    private fun drawFrontWave(canvas: Canvas) {
        pathFront.reset()
        pathFront.moveTo(0f, cachedHeight * 0.52f + offsetFront)
        pathFront.quadTo(
            cachedWidth * 0.55f,
            cachedHeight * (0.70f + offsetFront / 45),
            cachedWidth,
            cachedHeight * 0.50f + offsetFront
        )
        pathFront.lineTo(cachedWidth, cachedHeight)
        pathFront.lineTo(0f, cachedHeight)
        pathFront.close()
        canvas.drawPath(pathFront, paintFront)
    }
}