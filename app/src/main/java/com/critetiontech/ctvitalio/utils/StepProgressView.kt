package com.critetiontech.ctvitalio.utils


import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.critetiontech.ctvitalio.R

class StepProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {

    private var totalSteps = 5
    private var currentStep = 1

    private val circleRadius = 16f * resources.displayMetrics.density // 16dp
    private val lineHeight = 4f * resources.displayMetrics.density // 4dp
    private val lineLength = 40f * resources.displayMetrics.density // 40dp

    private val redPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.red)
        style = Paint.Style.FILL
    }
    private val greenPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.green)
        style = Paint.Style.FILL
    }
    private val greyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.grey)
        style = Paint.Style.FILL
    }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.line_color)
        style = Paint.Style.FILL
    }

    fun setSteps(total: Int) {
        totalSteps = total
        invalidate()
        requestLayout()
    }

    fun setCurrentStep(step: Int) {
        currentStep = step.coerceIn(1, totalSteps)
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = ((circleRadius * 2) * totalSteps + lineLength * (totalSteps - 1)).toInt() + paddingLeft + paddingRight
        val height = (circleRadius * 2).toInt() + paddingTop + paddingBottom
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        var startX = paddingLeft + circleRadius
        val centerY = paddingTop + circleRadius

        for (i in 1..totalSteps) {
            // Draw circle with color based on step
            val paint = when {
                i == 1 -> redPaint
                i == 2 -> greenPaint
                i > 2 -> greyPaint
                else -> greyPaint
            }

            // Actually, for dynamic coloring based on currentStep, let's adjust:
            // Step 1: red only if currentStep == 1
            // Step 2: green only if currentStep == 2
            // Steps > 2: grey
            // But I think you want something like:
            // Step == currentStep → red
            // Step == currentStep + 1 → green
            // Others → grey

            // Let's implement this logic now:

            val circlePaint = when {
                i == currentStep -> redPaint
                i == currentStep + 1 -> greenPaint
                else -> greyPaint
            }

            canvas.drawCircle(startX, centerY, circleRadius, circlePaint)

            // Draw connecting line if not last circle
            if (i < totalSteps) {
                val lineStartX = startX + circleRadius
                val lineEndX = lineStartX + lineLength
                canvas.drawRect(lineStartX, centerY - lineHeight / 2, lineEndX, centerY + lineHeight / 2, linePaint)
                startX = lineEndX + circleRadius
            }
        }
    }
}