package com.critetiontech.ctvitalio.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin


class Figure8View @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(6f)
        color = Color.parseColor("#4DFFFFFF") // Semi-transparent white
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FF5A79") // Pink/Red target
    }

    private val glowOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#33FF5A79")
    }

    private val glowInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#80FF5A79")
    }

    private val trailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // Current loop progress: 0.0f to 1.0f
    var progress: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    var showDot: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private val infinityPath = Path()
    private var centerValX = 0f
    private var centerValY = 0f
    private var pathScaleA = 0f

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerValX = w / 2f
        centerValY = h / 2f

        // Deduct padding to avoid clipping
        val margin = dpToPx(24f)
        pathScaleA = (w - margin * 2f) / 2f

        // Precompute path using Lemniscate of Bernoulli
        infinityPath.reset()
        val steps = 180
        for (i in 0..steps) {
            val t = (i * 2.0 * Math.PI) / steps
            val cosT = cos(t)
            val sinT = sin(t)
            val denom = 1.0 + sinT * sinT

            val x = (centerValX + (pathScaleA * cosT) / denom).toFloat()
            val y = (centerValY + (pathScaleA * sinT * cosT) / denom).toFloat()

            if (i == 0) {
                infinityPath.moveTo(x, y)
            } else {
                infinityPath.lineTo(x, y)
            }
        }
        infinityPath.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (pathScaleA <= 0f) return

        // Draw infinity track outline
        canvas.drawPath(infinityPath, pathPaint)

        if (showDot) {
            // Draw trail dots first
            drawTrail(canvas, progress)

            // Draw current active glowing dot
            val angle = progress * 2.0 * Math.PI
            val cosT = cos(angle)
            val sinT = sin(angle)
            val denom = 1.0 + sinT * sinT

            val dotX = (centerValX + (pathScaleA * cosT) / denom).toFloat()
            val dotY = (centerValY + (pathScaleA * sinT * cosT) / denom).toFloat()

            // Draw glowing outer/inner circles
            canvas.drawCircle(dotX, dotY, dpToPx(16f), glowOuterPaint)
            canvas.drawCircle(dotX, dotY, dpToPx(12f), glowInnerPaint)
            // Draw main dot
            canvas.drawCircle(dotX, dotY, dpToPx(8f), dotPaint)
        }
    }

    private fun drawTrail(canvas: Canvas, currentProgress: Float) {
        // Draw 3 fading trailing circles behind current progress
        val trailSteps = 3
        val stepSizeFraction = 0.015f

        for (i in 1..trailSteps) {
            var trailProgress = currentProgress - (i * stepSizeFraction)
            if (trailProgress < 0f) {
                trailProgress += 1.0f // Wrap around
            }

            val t = trailProgress * 2.0 * Math.PI
            val cosT = cos(t)
            val sinT = sin(t)
            val denom = 1.0 + sinT * sinT

            val trailX = (centerValX + (pathScaleA * cosT) / denom).toFloat()
            val trailY = (centerValY + (pathScaleA * sinT * cosT) / denom).toFloat()

            // Fade size and opacity
            val radius = dpToPx(8f - i * 1.5f)
            val alpha = (200 - i * 50).coerceAtLeast(0)
            trailPaint.color = Color.argb(alpha, 255, 90, 121)

            canvas.drawCircle(trailX, trailY, radius, trailPaint)
        }
    }
}
