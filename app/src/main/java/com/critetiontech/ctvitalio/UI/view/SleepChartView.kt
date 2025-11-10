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
        color = 0xFFFFA726.toInt() // Orange
    }

    private val remPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF64B5F6.toInt() // Light Blue
    }

    private val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1976D2.toInt() // Blue
    }

    private val deepPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF0D47A1.toInt() // Dark Blue
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFE0E0E0.toInt() // light gray grid lines
        strokeWidth = 1f
    }

    // Border around entire graph
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFBDBDBD.toInt()   // slightly darker gray for border
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

    // === Sleep Data ===
    private val sleepData = listOf(
        3, 3, 1, 0, 0, 1, 1, 1, 0, 0,
        1, 2, 2, 0, 1, 1, 1, 1, 1, 1,
        1, 1, 2, 1, 1, 1, 1, 1, 0, 0,
        3, 3, 1, 1, 0, 0
    )

    private val labels = listOf("Awake", "REM", "Light", "Deep")
    private val timeLabels = listOf("2 AM", "5 AM", "8 AM", "11 AM")

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // Chart margins
        val leftMargin = 100f
        val rightMargin = 40f
        val topMargin = 40f
        val bottomMargin = 60f

        val chartWidth = width - leftMargin - rightMargin
        val chartHeight = height - topMargin - bottomMargin
        val labelSpacing = chartHeight / 4f

        // === Draw Y-axis labels ===
        labels.forEachIndexed { index, label ->
            val y = topMargin + index * labelSpacing + labelSpacing / 2 + 10f
            canvas.drawText(label, 10f, y, labelPaint)
        }

        // === Draw horizontal grid lines ===
        for (i in 0..4) {
            val y = topMargin + i * labelSpacing
            canvas.drawLine(leftMargin, y, leftMargin + chartWidth, y, gridPaint)
        }

        // === Draw border around graph area ===
        val borderRect = RectF(leftMargin, topMargin, leftMargin + chartWidth, topMargin + chartHeight)
        canvas.drawRect(borderRect, borderPaint)

        // === Bar setup ===
        val barWidth = chartWidth / sleepData.size
        val barSpacing = 0f
        val actualBarWidth = barWidth - barSpacing
        val barHeight = labelSpacing * 0.6f
        val cornerRadius = 6f

        val path = Path()
        var lastX = 0f
        var lastY = 0f

        // === Draw sleep bars and connecting line ===
        sleepData.forEachIndexed { index, stage ->
            val fillPaint: Paint
            val ringColor: Int

            when (stage) {
                0 -> { fillPaint = deepPaint; ringColor = deepPaint.color }
                1 -> { fillPaint = lightPaint; ringColor = lightPaint.color }
                2 -> { fillPaint = remPaint; ringColor = remPaint.color }
                else -> { fillPaint = awakePaint; ringColor = awakePaint.color }
            }

            val x = leftMargin + index * barWidth
            val y = when (stage) {
                0 -> topMargin + 3 * labelSpacing + (labelSpacing - barHeight) / 2
                1 -> topMargin + 2 * labelSpacing + (labelSpacing - barHeight) / 2
                2 -> topMargin + 1 * labelSpacing + (labelSpacing - barHeight) / 2
                else -> topMargin + (labelSpacing - barHeight) / 2
            }

            val rect = RectF(x, y, x + actualBarWidth, y + barHeight)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, fillPaint)

            val midX = x + actualBarWidth / 2
            val midY = y + barHeight / 2

            // 🔹 Draw outline ring around midpoint (not filled)
            ringPaint.color = ringColor
            canvas.drawCircle(midX, midY, 5f, ringPaint)

            // Build smooth connecting path
            if (index == 0) {
                path.moveTo(midX, midY)
            } else {
                val controlX = (lastX + midX) / 2
                path.quadTo(lastX, lastY, controlX, midY)
            }

            lastX = midX
            lastY = midY
        }

        // === Draw smooth connecting line ===
        canvas.drawPath(path, linePaint)

        // === Draw X-axis time labels ===
        val timeSpacing = chartWidth / (timeLabels.size - 1)
        timeLabels.forEachIndexed { index, time ->
            val x = leftMargin + index * timeSpacing
            val textWidth = timePaint.measureText(time)
            canvas.drawText(time, x - textWidth / 2, height - 20f, timePaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 800
        val desiredHeight = 350
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}