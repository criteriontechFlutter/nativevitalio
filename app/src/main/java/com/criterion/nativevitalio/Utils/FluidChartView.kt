package com.criterion.nativevitalio.Utils


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.criterion.nativevitalio.model.FluidPoint

class FluidChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val axisPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val labelPaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    private val pointPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val linePaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    var fluidData: List<FluidPoint> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = 60f
        val chartHeight = height - padding * 2
        val chartWidth = width - padding * 2
        val maxY = 1000f
        val maxX = 24f

        // Draw Y grid lines
        for (i in 0..10) {
            val y = padding + chartHeight * (i / 10f)
            canvas.drawLine(padding, y, width - padding, y, axisPaint)
            val value = maxY - (maxY * (i / 10f))
            canvas.drawText("${value.toInt()} ml", padding - 30, y + 10, labelPaint)
        }

        // Draw X axis labels
        for (i in 0..8) {
            val x = padding + chartWidth * (i / 8f)
            val hour = (i * 3)
            canvas.drawText(String.format("%02d", hour), x, height - padding / 2, labelPaint)
        }

        // Draw lines & points
        val scaledPoints = fluidData.map {
            val x = padding + (it.time / maxX) * chartWidth
            val y = padding + (1f - it.amount / maxY) * chartHeight
            Pair(x, y)
        }

        for (i in 0 until scaledPoints.size - 1) {
            val (x1, y1) = scaledPoints[i]
            val (x2, y2) = scaledPoints[i + 1]
            canvas.drawLine(x1, y1, x2, y2, linePaint)
        }

        fluidData.forEachIndexed { index, point ->
            val (x, y) = scaledPoints[index]
            pointPaint.color = Color.parseColor(point.colorHex)
            canvas.drawCircle(x, y, 12f, pointPaint)

            // Draw label
            canvas.drawText("${point.amount.toInt()}ml", x, y - 20, labelPaint)
        }
    }
}
