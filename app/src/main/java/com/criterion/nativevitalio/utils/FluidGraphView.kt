package com.critetiontech.ctvitalio.utils




import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.critetiontech.ctvitalio.model.FluidPointGraph

class FluidGraphView(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var dataPoints: List<FluidPointGraph> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val axisPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 2f
    }

    private val labelPaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 26f
    }

    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#3DA5F5")
    }

    private val bubbleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 22f
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val padding = 80f
        val graphWidth = width - padding * 2
        val graphHeight = height - padding * 2
        val maxY = 1000f
        val maxX = 24f

        // Draw Y-axis grid
        for (i in 0..10) {
            val y = padding + graphHeight * (i / 10f)
            val value = maxY - (i * 100)
            canvas.drawLine(padding, y, width - padding, y, axisPaint)
            canvas.drawText("${value.toInt()} ml  ", 10f, y + 8, labelPaint)
        }

        // Draw X-axis labels
        for (i in 0..8) {
            val x = padding + (graphWidth / 6) * i
            val hour = (i * 3)
            canvas.drawText(String.format("%02d", hour), x, height - 20f, labelPaint)
        }

        val points = dataPoints.map {
            val x = padding + (it.timeHour / maxX) * graphWidth
            val y = padding + (1f - it.quantity / maxY) * graphHeight
            Pair(x, y)
        }

        // Draw lines
        for (i in 0 until points.size - 1) {
            canvas.drawLine(points[i].first, points[i].second, points[i + 1].first, points[i + 1].second, axisPaint)
        }

        // Draw points with bubbles
        dataPoints.forEachIndexed { i, point ->
            val (x, y) = points[i]
            pointPaint.color = Color.parseColor(point.colorHex)
            canvas.drawCircle(x, y, 12f, pointPaint)

            // Bubble
            bubblePaint.color = Color.parseColor(point.colorHex)
            val bubbleRect = RectF(x - 40f, y - 60f, x + 40f, y - 30f)
            canvas.drawRoundRect(bubbleRect, 12f, 12f, bubblePaint)
            canvas.drawText("${point.quantity.toInt()}ml", x, y - 38f, bubbleTextPaint)
        }
    }
}
