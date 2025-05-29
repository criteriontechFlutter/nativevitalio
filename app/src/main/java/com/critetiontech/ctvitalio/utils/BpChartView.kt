package com.critetiontech.ctvitalio.utils


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

data class BpDataPoint(
    val timeLabel: String,
    val systolic: Int,
    val diastolic: Int,
    val isDay: Boolean
)

class BpChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var dataPoints: List<BpDataPoint> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val sysPaint = Paint().apply {
        color = Color.rgb(255, 165, 0)
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val diaPaint = Paint().apply {
        color = Color.rgb(33, 50, 89)
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val pointPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val labelPaint = Paint().apply {
        color = Color.BLACK
        textSize = 28f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val axisPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val normalRangePaint = Paint().apply {
        color = Color.parseColor("#e6f5f7") // light blue
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataPoints.isEmpty()) return

        val padding = 80f
        val widthPerPoint = (width - 2 * padding) / (dataPoints.size - 1)
        val maxBP = 160f
        val minBP = 40f
        val chartHeight = height - 2 * padding

        fun toY(value: Int): Float {
            return padding + (maxBP - value) * chartHeight / (maxBP - minBP)
        }

        // Draw normal BP range background
        val top = toY(120)
        val bottom = toY(80)
        canvas.drawRect(padding, top, width - padding, bottom, normalRangePaint)

        // Draw lines and points
        for (i in 0 until dataPoints.size - 1) {
            val x1 = padding + i * widthPerPoint
            val x2 = padding + (i + 1) * widthPerPoint
            val dp1 = dataPoints[i]
            val dp2 = dataPoints[i + 1]

            canvas.drawLine(x1, toY(dp1.systolic), x2, toY(dp2.systolic), sysPaint)
            canvas.drawLine(x1, toY(dp1.diastolic), x2, toY(dp2.diastolic), diaPaint)
        }

        dataPoints.forEachIndexed { i, dp ->
            val x = padding + i * widthPerPoint
            val ySys = toY(dp.systolic)
            val yDia = toY(dp.diastolic)

            pointPaint.color = sysPaint.color
            canvas.drawCircle(x, ySys, 8f, pointPaint)
            canvas.drawText(dp.systolic.toString(), x, ySys - 12, labelPaint)

            pointPaint.color = diaPaint.color
            canvas.drawCircle(x, yDia, 8f, pointPaint)
            canvas.drawText(dp.diastolic.toString(), x, yDia - 12, labelPaint)

            // Draw Time Label
            canvas.drawText(dp.timeLabel, x, height - padding / 3, labelPaint)

            // Draw Day/Night Symbol
            val symbol = if (dp.isDay) "☀" else "🌙"
            canvas.drawText(symbol, x, height - padding / 1.5f, labelPaint)
        }

        // Y-axis label
        labelPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("BP (mmHg)", 10f, padding - 20f, labelPaint)
    }
}
