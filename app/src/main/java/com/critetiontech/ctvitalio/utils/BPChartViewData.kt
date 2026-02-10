package com.critetiontech.ctvitalio.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BPChartViewData(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var systolic = listOf<Int>()
    private var diastolic = listOf<Int>()
    private var days = listOf<String>()

    private val systolicPaint = Paint().apply {
        color = Color.parseColor("#18B87A")
        strokeWidth = 8f
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val diastolicPaint = Paint().apply {
        color = Color.parseColor("#2F80ED")
        strokeWidth = 8f
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // Axis line paint
    private val axisPaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.GRAY
        textSize = 28f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val yAxisTextPaint = Paint().apply {
        color = Color.GRAY
        textSize = 26f
        isAntiAlias = true
        textAlign = Paint.Align.RIGHT
    }

    fun setDataa(
        systolic: List<Int>,
        diastolic: List<Int>,
        days: List<String>
    ) {
        this.systolic = systolic
        this.diastolic = diastolic
        this.days = days
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (systolic.isEmpty() || diastolic.isEmpty()) return

        val widthF = width.toFloat()
        val heightF = height.toFloat()

        val chartLeft = 80f
        val chartRight = widthF - 40f
        val chartTop = heightF * 0.1f
        val chartBottom = heightF * 0.8f

        val maxValue = 200f
        val minValue = 0f

        // Draw X-axis
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint)

        // Draw Y-axis
        canvas.drawLine(chartLeft, chartTop, chartLeft, chartBottom, axisPaint)

        // Draw Y-axis labels (optional)
        val steps = 4
        val valueStep = (maxValue - minValue) / steps

        for (i in 0..steps) {
            val value = minValue + i * valueStep
            val y = chartBottom - (i / steps.toFloat()) * (chartBottom - chartTop)
            canvas.drawText(value.toInt().toString(), chartLeft - 10f, y + 8f, yAxisTextPaint)
        }

        val stepX = (chartRight - chartLeft) / (systolic.size + 1)

        for (i in systolic.indices) {

            val x = chartLeft + stepX * (i + 1)

            val sysY = chartBottom - ((systolic[i] - minValue) / (maxValue - minValue)) * (chartBottom - chartTop)
            val diaY = chartBottom - ((diastolic[i] - minValue) / (maxValue - minValue)) * (chartBottom - chartTop)

            // draw systolic circle
            canvas.drawCircle(x, sysY, 10f, systolicPaint)

            // draw diastolic circle
            canvas.drawCircle(x, diaY, 10f, diastolicPaint)

            // draw day text
            if (i < days.size) {
                canvas.drawText(days[i], x, chartBottom + 40f, textPaint)
            }
        }
    }
}