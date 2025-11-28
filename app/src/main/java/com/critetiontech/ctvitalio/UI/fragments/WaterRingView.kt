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
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.critetiontech.ctvitalio.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class WaterRingWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val blurPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val wavePath = Path()
    private val clipPath = Path()

    private var waveOffset = 0f
    private var fillFraction = 0f

    private var ringWidth = dp(50f)
    private var waveHeight = dp(18f)

    init {
        // Blue gradient ring
        ringPaint.style = Paint.Style.STROKE
        ringPaint.strokeWidth = ringWidth
        ringPaint.shader = LinearGradient(
            0f, 0f, 0f, 600f,
            Color.parseColor("#A4D6FF"),
            Color.parseColor("#1A85FF"),
            Shader.TileMode.CLAMP
        )

        // Water wave color
        wavePaint.color = Color.parseColor("#1A85FF")
        wavePaint.style = Paint.Style.FILL

        // Blur inner area
        blurPaint.color = Color.parseColor("#80FFFFFF")

        // Center percentage text
        textPaint.color = Color.parseColor("#1A1A1A")
        textPaint.textSize = sp(26f)
        textPaint.textAlign = Paint.Align.CENTER

        startWaveAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val radius = min(w, h) / 2f
        val cx = w / 2f
        val cy = h / 2f

        // ✓ Draw ring
        canvas.drawCircle(cx, cy, radius - ringWidth / 2, ringPaint)

        // ✓ Clip to inner circle (where wave will be drawn)
        val innerRadius = radius - ringWidth
        clipPath.reset()
        clipPath.addCircle(cx, cy, innerRadius, Path.Direction.CW)

        canvas.save()
        canvas.clipPath(clipPath)

        // ✓ Fill inner with frosted white
        canvas.drawCircle(cx, cy, innerRadius, blurPaint)

        // ---- WAVE DRAWING ----
        wavePath.reset()

        val waveTop = cy + innerRadius * (1 - fillFraction)

        val waveWidth = w * 1.5f

        wavePath.moveTo(-waveWidth + waveOffset, waveTop)

        val step = waveWidth / 2

        wavePath.quadTo(
            -waveWidth * 0.5f + waveOffset, waveTop - waveHeight,
            waveOffset, waveTop
        )

        wavePath.quadTo(
            waveWidth * 0.5f + waveOffset, waveTop + waveHeight,
            waveWidth + waveOffset, waveTop
        )

        wavePath.lineTo(waveWidth, h)
        wavePath.lineTo(0f, h)
        wavePath.close()

        canvas.drawPath(wavePath, wavePaint)

        canvas.restore()

        // ---- DRAW % TEXT ----
        val centerY = cy - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText("${(fillFraction * 100).toInt()}%", cx, centerY, textPaint)
    }

    private fun startWaveAnimation() {
        ValueAnimator.ofFloat(0f, width.toFloat()).apply {
            duration = 1800
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addUpdateListener {
                waveOffset = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun setLevel(percent: Float) {
        fillFraction = percent.coerceIn(0f, 1f)
        invalidate()
    }

    private fun dp(v: Float) = v * resources.displayMetrics.density
    private fun sp(v: Float) = v * resources.displayMetrics.scaledDensity
}