package com.critetiontech.ctvitalio.UI.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class SleepChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Dynamic sleep stage data
    private var chartStages: List<Int> = emptyList()

    fun setSleepStages(stages: List<Int>) {
        chartStages = stages
        invalidate()
    }

    // === Paints ===
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF999999.toInt()
        textSize = 28f
    }

    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF666666.toInt()
        textSize = 32f
    }

    private val awakePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFA726.toInt()
    }

    private val remPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF64B5F6.toInt()
    }

    private val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1976D2.toInt()
    }

    private val deepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0D47A1.toInt()
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE0E0E0.toInt()
        strokeWidth = 1f
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFBDBDBD.toInt()
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        strokeWidth = 2.5f
        style = Paint.Style.STROKE
        alpha = 180
    }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        alpha = 200
    }

    private val timeLabels = listOf("2 AM", "5 AM", "8 AM", "11 AM")

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (chartStages.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()

        val leftMargin = 100f
        val rightMargin = 40f
        val topMargin = 40f
        val bottomMargin = 60f

        val chartWidth = width - leftMargin - rightMargin
        val chartHeight = height - topMargin - bottomMargin
        val labelSpacing = chartHeight / 4f

        // Grid
        for (i in 0..4) {
            val y = topMargin + i * labelSpacing
            canvas.drawLine(leftMargin, y, leftMargin + chartWidth, y, gridPaint)
        }

        // Border
        canvas.drawRect(
            RectF(leftMargin, topMargin, leftMargin + chartWidth, topMargin + chartHeight),
            borderPaint
        )

        val barWidth = chartWidth / chartStages.size
        val barHeight = labelSpacing * 0.6f
        val cornerRadius = 6f

        val path = Path()
        var lastX = 0f
        var lastY = 0f

        chartStages.forEachIndexed { index, stage ->

            val paint = when (stage) {
                0 -> deepPaint
                1 -> lightPaint
                2 -> remPaint
                else -> awakePaint
            }

            val x = leftMargin + index * barWidth

            val y = when (stage) {
                0 -> topMargin + 3 * labelSpacing + (labelSpacing - barHeight) / 2
                1 -> topMargin + 2 * labelSpacing + (labelSpacing - barHeight) / 2
                2 -> topMargin + 1 * labelSpacing + (labelSpacing - barHeight) / 2
                else -> topMargin + (labelSpacing - barHeight) / 2
            }

            val rect = RectF(x, y, x + barWidth, y + barHeight)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)

            val midX = x + barWidth / 2
            val midY = y + barHeight / 2

            ringPaint.color = paint.color
            canvas.drawCircle(midX, midY, 5f, ringPaint)

            if (index == 0) {
                path.moveTo(midX, midY)
            } else {
                val controlX = (lastX + midX) / 2
                path.quadTo(lastX, lastY, controlX, midY)
            }

            lastX = midX
            lastY = midY
        }

        canvas.drawPath(path, linePaint)

        val timeSpacing = chartWidth / (timeLabels.size - 1)
        timeLabels.forEachIndexed { i, t ->
            val tx = leftMargin + i * timeSpacing
            val tw = timePaint.measureText(t)
            canvas.drawText(t, tx - tw / 2, height - 20f, timePaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(800, widthMeasureSpec)
        val height = resolveSize(350, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}