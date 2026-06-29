package com.critetiontech.ctvitalio.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class ClockCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Properties for rendering
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(6f)
        color = Color.parseColor("#4DFFFFFF") // Semi-transparent white
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FF4D4D") // Red color
    }

    private val glowOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#33FF4D4D") // Transparent red for glow
    }

    private val glowInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#80FF4D4D") // Semi-transparent red for glow
    }

    // The angle in degrees. 0 degrees is 3 o'clock. We start at 270f (-90f) for 12 o'clock.
    var currentAngle: Float = 270f
        set(value) {
            field = value
            invalidate()
        }

    var showDot: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f

        // Ensure there is enough padding for the glow of the red dot to not get clipped
        val dotMaxRadius = dpToPx(16f)
        val trackStrokeHalf = trackPaint.strokeWidth / 2f
        val margin = dotMaxRadius + trackStrokeHalf + dpToPx(4f)
        val radius = (width.coerceAtMost(height) / 2f) - margin

        if (radius <= 0f) return

        // Draw track
        canvas.drawCircle(cx, cy, radius, trackPaint)

        // Draw animated red dot if enabled
        if (showDot) {
            val angleRad = Math.toRadians(currentAngle.toDouble())
            val dotX = (cx + radius * cos(angleRad)).toFloat()
            val dotY = (cy + radius * sin(angleRad)).toFloat()

            // Draw glowing outer halo
            canvas.drawCircle(dotX, dotY, dpToPx(16f), glowOuterPaint)
            // Draw glowing inner halo
            canvas.drawCircle(dotX, dotY, dpToPx(12f), glowInnerPaint)
            // Draw main solid red dot
            canvas.drawCircle(dotX, dotY, dpToPx(8f), dotPaint)
        }
    }
}