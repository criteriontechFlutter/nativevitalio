package com.critetiontech.ctvitalio.UI.fragments

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.critetiontech.ctvitalio.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import androidx.core.graphics.toColorInt
class WaterRingWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // Paints
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val blurPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringBasePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Paths
    private val wavePath = Path()
    private val innerClipPath = Path()
    private val ringClipPath = Path()

    // State
    private var waveOffset = 0f
    var fillFraction = 0f
    var perData = "0"
    private var lastTouchY = 0f

    // Sizes
    private var ringWidth = dp(50f)
    private var waveHeight = dp(18f)

    // Animators
    private var waveAnimator: ValueAnimator? = null
    private var levelAnimator: ValueAnimator? = null

    init {
        // Wave color
        wavePaint.style = Paint.Style.FILL
        wavePaint.color = "#1A85FF".toColorInt()

        // Inner frosted circle
        blurPaint.color = "#80FFFFFF".toColorInt()

        // Center text
        textPaint.color = "#1A1A1A".toColorInt()
        textPaint.textSize = sp(26f)
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD

        // Outer ring
        ringBasePaint.style = Paint.Style.STROKE
        ringBasePaint.strokeWidth = ringWidth
        ringBasePaint.color = "#33000000".toColorInt()
    }

    // ---------- SIZE ----------
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        startWaveAnimation(w.toFloat())
    }

    // ---------- DRAW ----------
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val radius = min(w, h) / 2f
        val cx = w / 2f
        val cy = h / 2f

        val outerRadius = radius
        val innerRadius = radius - ringWidth

        // Outer ring
        ringBasePaint.strokeWidth = ringWidth
        canvas.drawCircle(cx, cy, outerRadius - ringWidth / 2, ringBasePaint)

        // Wave calculation
        val waveTop = cy + innerRadius * (1 - fillFraction)
        val waveWidth = w * 1.5f

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

        // Outer ring wave
        ringClipPath.reset()
        ringClipPath.addCircle(cx, cy, outerRadius, Path.Direction.CW)
        ringClipPath.addCircle(cx, cy, innerRadius, Path.Direction.CCW)

        canvas.save()
        canvas.clipPath(ringClipPath)
        wavePaint.alpha = 160
        canvas.drawPath(wavePath, wavePaint)
        canvas.restore()

        // Inner circle wave
        innerClipPath.reset()
        innerClipPath.addCircle(cx, cy, innerRadius, Path.Direction.CW)

        canvas.save()
        canvas.clipPath(innerClipPath)
        canvas.drawCircle(cx, cy, innerRadius, blurPaint)
        wavePaint.alpha = 255
        canvas.drawPath(wavePath, wavePaint)
        canvas.restore()

        // Center percentage text
        val textY = cy - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText("  ${perData}%", cx, textY, textPaint)
    }

    // ---------- SLIDE TO FILL ----------
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                lastTouchY = event.y
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dy = lastTouchY - event.y
                lastTouchY = event.y

                val delta = dy / height
                fillFraction += delta
                fillFraction = fillFraction.coerceIn(0f, 1f)

                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    // ---------- WAVE ANIMATION ----------
    private fun startWaveAnimation(maxWidth: Float) {
        waveAnimator?.cancel()
        waveAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                waveOffset = it.animatedFraction * maxWidth
                postInvalidateOnAnimation()
            }
            start()
        }
    }

    // ---------- LEVEL CONTROL ----------
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

    // ---------- SIZE CONTROL ----------
    fun setRingWidthDp(dpValue: Float) {
        ringWidth = dp(dpValue)
        invalidate()
    }

    fun setWaveHeightDp(dpValue: Float) {
        waveHeight = dp(dpValue)
        invalidate()
    }

    // ---------- CLEANUP ----------
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        waveAnimator?.cancel()
        levelAnimator?.cancel()
    }

    // ---------- UTILS ----------
    private fun dp(v: Float) = v * resources.displayMetrics.density
    private fun sp(v: Float) = v * resources.displayMetrics.scaledDensity
}