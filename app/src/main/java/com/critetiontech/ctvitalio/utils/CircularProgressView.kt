package com.critetiontech.ctvitalio.utils

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin


class ArcProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** PAINTS ------------------------------------------------------------ */

    private val filledPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#FFA74A") // orange
        strokeWidth = 22f
    }

    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#D9CEBD")
        strokeWidth = 22f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#222222")
        textSize = 110f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        textSize = 42f
        textAlign = Paint.Align.CENTER
    }

    /** VALUES ------------------------------------------------------------ */

    private var progress = 65f
    private var animatedProgress = 0f

    private var labelText = "Moderate"

    private var customInterpolator: TimeInterpolator? = null


    /** DRAW LOGIC -------------------------------------------------------- */

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 1.9f
        val radius = min(width, height) / 2.4f

        val totalSegments = 32
        val anglePerSegment = 180f / totalSegments
        val filledSegments = ((animatedProgress / 100f) * totalSegments).toInt()

        for (i in 0 until totalSegments) {
            val angle = 180f + (i * anglePerSegment)
            val rad = Math.toRadians(angle.toDouble())

            val innerR = radius - 30f
            val outerR = radius + 8f

            val startX = (centerX + innerR * cos(rad)).toFloat()
            val startY = (centerY + innerR * sin(rad)).toFloat()
            val endX = (centerX + outerR * cos(rad)).toFloat()
            val endY = (centerY + outerR * sin(rad)).toFloat()

            val paint = if (i < filledSegments) filledPaint else emptyPaint
            canvas.drawLine(startX, startY, endX, endY, paint)
        }

        // Value
        canvas.drawText(
            progress.toInt().toString(),
            centerX,
            centerY - 40f,
            textPaint
        )

        // Label
        canvas.drawText(
            labelText,
            centerX,
            centerY + 70f,
            labelPaint
        )
    }


    /** PUBLIC API -------------------------------------------------------- */

    fun setProgressAnimated(value: Float, label: String = "") {
        progress = value.coerceIn(0f, 100f)
        if (label.isNotEmpty()) labelText = label

        val animator = ValueAnimator.ofFloat(animatedProgress, progress).apply {
            duration = 1500
            interpolator = customInterpolator ?: AccelerateDecelerateInterpolator()
            addUpdateListener {
                animatedProgress = it.animatedValue as Float
                invalidate()
            }
        }
        animator.start()
    }

    fun setProgress(value: Float, label: String = "") {
        progress = value.coerceIn(0f, 100f)
        if (label.isNotEmpty()) labelText = label
        animatedProgress = progress
        invalidate()
    }

    /** CUSTOMIZATION FROM FRAGMENT -------------------------------------- */

    fun setStrokeWidth(width: Float) {
        filledPaint.strokeWidth = width
        emptyPaint.strokeWidth = width
        invalidate()
    }

    fun setColors(filled: Int, empty: Int) {
        filledPaint.color = filled
        emptyPaint.color = empty
        invalidate()
    }

    fun setLabel(label: String) {
        labelText = label
        invalidate()
    }

    fun setInterpolator(interpolator: TimeInterpolator) {
        customInterpolator = interpolator
    }
}