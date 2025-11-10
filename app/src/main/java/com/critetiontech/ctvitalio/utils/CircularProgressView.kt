package com.critetiontech.ctvitalio.utils

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


class ArcProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val filledPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#FFA74A") // orange
        strokeWidth = 20f
    }

    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#D9CEBD") // light beige-gray
        strokeWidth = 18f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#222222")
        textSize = 100f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#999999")
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }

    private var progress = 55f
    private var animatedProgress = 0f
    private var calorieValue = "450 kcal"
    private var stepValue = "2,150"

    // Convert dp to pixels
    private val paddingPx = (20 * context.resources.displayMetrics.density)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2.2f
        val centerY = height / 2.2f
        val radius = (min(width, height) / 2.8f)

        // Total number of vertical segments
        val totalSegments = 32
        val anglePerSegment = 180f / totalSegments

        // Calculate how many segments are filled based on progress
        val filledSegments = ((animatedProgress / 100f) * totalSegments).toInt()

        // Draw vertical line segments
        for (i in 0 until totalSegments) {
            val currentAngle = 180f + (i * anglePerSegment)
            val angleRad = Math.toRadians(currentAngle.toDouble())

            // Inner point (closer to center)
            val innerRadius = radius - 45f
            val startX = (centerX + innerRadius * cos(angleRad)).toFloat()
            val startY = (centerY + innerRadius * sin(angleRad)).toFloat()

            // Outer point (farther from center)
            val outerRadius = radius + 8f
            val endX = (centerX + outerRadius * cos(angleRad)).toFloat()
            val endY = (centerY + outerRadius * sin(angleRad)).toFloat()

            // Select paint based on whether segment should be filled
            val paint = if (i < filledSegments) filledPaint else emptyPaint

            // Draw the line segment
            canvas.drawLine(startX, startY, endX, endY, paint)
        }

        // Draw main progress number
        canvas.drawText(
            progress.toInt().toString(),
            centerX,
            centerY - 60f,
            textPaint
        )

        // Draw calorie & step text
        val calorieStepText = "\uD83D\uDD25 $calorieValue     \uD83C\uDFC3 $stepValue"
        canvas.drawText(calorieStepText, centerX, centerY + 120f, subTextPaint)
    }

    /** Animate progress smoothly */
    fun setProgressAnimated(value: Float) {
        progress = value.coerceIn(0f, 100f)
        val animator = ValueAnimator.ofFloat(animatedProgress, progress).apply {
            duration = 1500
            addUpdateListener {
                animatedProgress = it.animatedValue as Float
                invalidate()
            }
        }
        animator.start()
    }

    /** Set progress directly */
    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 100f)
        animatedProgress = progress
        invalidate()
    }

    fun setMetrics(calories: String, steps: String) {
        calorieValue = calories
        stepValue = steps
        invalidate()
    }
}