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
    var fillFraction = 0f
    var perData = "0"
    private var lastTouchY = 0f

    // Sizes
    private var ringWidth = dp(60f)   // Increased ring thickness
    private var waveHeight = dp(18f)

    // Animators
    private var waveAnimator: ValueAnimator? = null
    private var levelAnimator: ValueAnimator? = null

    init {

        // INNER WAVE (Dark)
        innerWavePaint.style = Paint.Style.FILL
        innerWavePaint.color = "#990A76E9".toColorInt()

        // LIGHT WAVE
        lightWavePaint.style = Paint.Style.FILL
        lightWavePaint.color = "#803197D6".toColorInt()

        // OUTER RING WAVE
        outerWavePaint.style = Paint.Style.FILL
        outerWavePaint.color = "#40A5C8FF".toColorInt()

        textPaint.color = "#1A1A1A".toColorInt()
        textPaint.textSize = sp(26f)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD

        // Grey Base Ring
        ringBasePaint.style = Paint.Style.STROKE
        ringBasePaint.strokeWidth = ringWidth
        ringBasePaint.color = "#4D000000".toColorInt() // transparent grey
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

        val waveTop = cy + innerRadius * (1 - fillFraction)
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

        // INNER CIRCLE WAVE
        innerCirclePath.reset()
        innerCirclePath.addCircle(cx, cy, innerRadius, Path.Direction.CW)

        canvas.save()
        canvas.clipPath(innerCirclePath)
        canvas.drawPath(wavePath, outerWavePaint)
        canvas.drawPath(wavePath2, outerWavePaint)
        canvas.restore()

        // OUTER RING WAVE
        ringClipPath.reset()
        ringClipPath.addCircle(cx, cy, outerRadius, Path.Direction.CW)
        ringClipPath.addCircle(cx, cy, innerRadius, Path.Direction.CCW)

        canvas.save()
        canvas.clipPath(ringClipPath)
        canvas.drawPath(wavePath, innerWavePaint)
        canvas.drawPath(wavePath2, lightWavePaint)
        canvas.restore()

        // Text
        val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText("${perData}%", cx, textY, textPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchY = event.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dy = lastTouchY - event.y
                lastTouchY = event.y
                fillFraction += dy / height
                fillFraction = fillFraction.coerceIn(0f, 1f)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

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

    fun setLevel(percent: Float) {
        fillFraction = percent.coerceIn(0f, 1f)
        invalidate()
    }

    fun setLevelSmooth(targetPercent: Float, duration: Long = 800) {
        levelAnimator?.cancel()
        levelAnimator = ValueAnimator.ofFloat(fillFraction, targetPercent.coerceIn(0f, 1f)).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            addUpdateListener {
                fillFraction = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    // Change ring thickness dynamically
    fun setRingWidth(dpValue: Float) {
        ringWidth = dp(dpValue)
        ringBasePaint.strokeWidth = ringWidth
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        waveAnimator?.cancel()
        levelAnimator?.cancel()
    }

    private fun dp(v: Float) = v * resources.displayMetrics.density
    private fun sp(v: Float) = v * resources.displayMetrics.scaledDensity
}
