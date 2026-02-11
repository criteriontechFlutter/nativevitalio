package com.critetiontech.ctvitalio.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class GlucoseTrendGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var minValues: List<Float> = emptyList()
    private var maxValues: List<Float> = emptyList()
    private var days: List<String> = emptyList()

    // Paints
    private val bgBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E6EBF0")   // grey bar
    }

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val rangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CFEDE3")   // normal range band
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#D0D7DE")
        strokeWidth = 2f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#777777")
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    private val yTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#777777")
        textSize = 26f
    }

    fun setData(
        minValues: List<Float>,
        maxValues: List<Float>,
        days: List<String>
    ) {
        this.minValues = minValues
        this.maxValues = maxValues
        this.days = days
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (minValues.isEmpty() || maxValues.isEmpty()) return

        val maxChartValue = 200f
        val chartHeight = height * 0.6f
        val bottom = height * 0.75f
        val leftPadding = 80f
        val spacing = (width - leftPadding) / (minValues.size + 1)

        // ---------- Y Axis Labels + Grid ----------
        val ySteps = listOf(0, 50, 100, 150, 200)

        ySteps.forEach { value ->
            val y = bottom - (value / maxChartValue) * chartHeight

            canvas.drawLine(leftPadding, y, width.toFloat(), y, axisPaint)
            canvas.drawText(value.toString(), 10f, y + 8f, yTextPaint)
        }

        // ---------- Normal Range Band ----------
        val normalMin = 70f
        val normalMax = 140f

        val rangeTop = bottom - (normalMax / maxChartValue) * chartHeight
        val rangeBottom = bottom - (normalMin / maxChartValue) * chartHeight

        canvas.drawRect(
            leftPadding,
            rangeTop,
            width.toFloat(),
            rangeBottom,
            rangePaint
        )

        // ---------- Bars ----------
        minValues.forEachIndexed { index, minVal ->

            val maxVal = maxValues[index]
            val xCenter = leftPadding + spacing * (index + 1)
            val barWidth = 26f

            // Case 1: No data → small grey bar
            if (minVal == 0f && maxVal == 0f) {

                canvas.drawRoundRect(
                    xCenter - barWidth / 2,
                    bottom - 6f,
                    xCenter + barWidth / 2,
                    bottom,
                    12f,
                    12f,
                    bgBarPaint
                )

            } else {

                val yMin = bottom - (minVal / maxChartValue) * chartHeight
                val yMax = bottom - (maxVal / maxChartValue) * chartHeight

                // Grey bar only below min
                canvas.drawRoundRect(
                    xCenter - barWidth / 2,
                    yMin,
                    xCenter + barWidth / 2,
                    bottom,
                    20f,
                    20f,
                    bgBarPaint
                )

                // Gradient green bar (min → max)
                val gradient = LinearGradient(
                    xCenter,
                    yMin,
                    xCenter,
                    yMax,
                    Color.parseColor("#A8E6CF"),
                    Color.parseColor("#12B886"),
                    Shader.TileMode.CLAMP
                )

                barPaint.shader = gradient

                canvas.drawRoundRect(
                    xCenter - 8f,
                    yMax,
                    xCenter + 8f,
                    yMin,
                    20f,
                    20f,
                    barPaint
                )

                barPaint.shader = null
            }

            // X-axis label (date)
            if (index < days.size) {
                canvas.drawText(
                    days[index],
                    xCenter,
                    bottom + 40,
                    textPaint
                )
            }
        }

        // ---------- X Axis Line ----------
        canvas.drawLine(leftPadding, bottom, width.toFloat(), bottom, axisPaint)
    }
}