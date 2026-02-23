package com.critetiontech.ctvitalio.UI.fragments

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.graphics.toColorInt
import kotlin.math.min



class WaterRingWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // Paints
    private val innerWavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val lightWavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val outerWavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringBasePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Paths
    private val wavePath = Path()
    private val wavePath2 = Path()
    private val ringClipPath = Path()
    private val innerCirclePath = Path()

    // State
    private var waveOffset = 0f
    private var waveOffset2 = 0f
    private var fillFraction = 0f   // 0f–1f internally
    private var percentValue = 0f   // 0–100 externally
    private var lastTouchY = 0f

    // Sizes
    private var ringWidth = dp(60f)
    private var waveHeight = dp(18f)

    // Animators
    private var waveAnimator: ValueAnimator? = null
    private var levelAnimator: ValueAnimator? = null

    init {

        innerWavePaint.style = Paint.Style.FILL
        innerWavePaint.color = "#990A76E9".toColorInt()

        lightWavePaint.style = Paint.Style.FILL
        lightWavePaint.color = "#803197D6".toColorInt()

        outerWavePaint.style = Paint.Style.FILL
        outerWavePaint.color = "#40A5C8FF".toColorInt()

        textPaint.color = "#1A1A1A".toColorInt()
        textPaint.textSize = sp(26f)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD

        ringBasePaint.style = Paint.Style.STROKE
        ringBasePaint.strokeWidth = ringWidth
        ringBasePaint.color = "#4D000000".toColorInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        startWaveAnimation(w.toFloat())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val radius = min(w, h) / 2f
        val cx = w / 2f
        val cy = h / 2f

        val outerRadius = radius
        val innerRadius = radius - ringWidth

        // Base ring
        canvas.drawCircle(cx, cy, outerRadius - ringWidth / 2, ringBasePaint)

        // Correct fill calculation (0–100%)
        val waveTop = cy + innerRadius - (innerRadius * 2 * fillFraction)

        val waveWidth = w * 1.5f

        // Wave 1
        wavePath.reset()
        wavePath.moveTo(-waveWidth + waveOffset, waveTop)
        wavePath.quadTo(
            -waveWidth * 0.5f + waveOffset,
            waveTop - waveHeight,
            waveOffset,
            waveTop
        )
        wavePath.quadTo(
            waveWidth * 0.5f + waveOffset,
            waveTop + waveHeight,
            waveWidth + waveOffset,
            waveTop
        )
        wavePath.lineTo(waveWidth, h)
        wavePath.lineTo(0f, h)
        wavePath.close()

        // Wave 2
        wavePath2.reset()
        wavePath2.moveTo(-waveWidth + waveOffset2, waveTop)
        wavePath2.quadTo(
            -waveWidth * 0.5f + waveOffset2,
            waveTop - waveHeight * 0.4f,
            waveOffset2,
            waveTop
        )
        wavePath2.quadTo(
            waveWidth * 0.5f + waveOffset2,
            waveTop + waveHeight * 0.4f,
            waveWidth + waveOffset2,
            waveTop
        )
        wavePath2.lineTo(waveWidth, h)
        wavePath2.lineTo(0f, h)
        wavePath2.close()

        // Inner circle clip
        innerCirclePath.reset()
        innerCirclePath.addCircle(cx, cy, innerRadius, Path.Direction.CW)

        canvas.save()
        canvas.clipPath(innerCirclePath)
        canvas.drawPath(wavePath, outerWavePaint)
        canvas.drawPath(wavePath2, outerWavePaint)
        canvas.restore()

        // Ring clip
        ringClipPath.reset()
        ringClipPath.addCircle(cx, cy, outerRadius, Path.Direction.CW)
        ringClipPath.addCircle(cx, cy, innerRadius, Path.Direction.CCW)

        canvas.save()
        canvas.clipPath(ringClipPath)
        canvas.drawPath(wavePath, innerWavePaint)
        canvas.drawPath(wavePath2, lightWavePaint)
        canvas.restore()

        // Draw percent text
        val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText("${percentValue.toInt()}%", cx, textY, textPaint)
    }

    // Smooth wave motion
    private fun startWaveAnimation(maxWidth: Float) {
        waveAnimator?.cancel()
        waveAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()

            addUpdateListener {
                val fraction = it.animatedFraction
                waveOffset = fraction * maxWidth
                waveOffset2 = fraction * maxWidth * 1.3f
                postInvalidateOnAnimation()
            }
            start()
        }
    }

    // Direct level set (0–100)
    fun setLevel(percent: Float) {
        percentValue = percent.coerceIn(0f, 100f)
        fillFraction = percentValue / 100f
        invalidate()
    }

    // Smooth animation level set
    fun setLevelSmooth(targetPercent: Float, duration: Long = 800) {
        levelAnimator?.cancel()

        val safePercent = targetPercent.coerceIn(0f, 100f)

        levelAnimator = ValueAnimator.ofFloat(percentValue, safePercent).apply {
            this.duration = duration
            interpolator = LinearInterpolator()

            addUpdateListener {
                percentValue = it.animatedValue as Float
                fillFraction = percentValue / 100f
                invalidate()
            }
            start()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        waveAnimator?.cancel()
        levelAnimator?.cancel()
    }

    private fun dp(v: Float) = v * resources.displayMetrics.density
    private fun sp(v: Float) = v * resources.displayMetrics.scaledDensity
}