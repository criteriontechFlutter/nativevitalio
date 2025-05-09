package com.criterion.nativevitalio.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.criterion.nativevitalio.model.FluidOutput

// FluidOutPutGraphView.kt
// A custom view to render a scrollable line chart with ml values and time labels without any third-party dependencies
// FluidOutPutGraphView.kt
// A custom view to render a scrollable line chart with ml values and time labels without any third-party dependencies
// FluidOutPutGraphView.kt
// A custom view to render a scrollable, clean line chart with ml values and time labels

class FluidOutPutGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var points = listOf<Pair<Float, Float>>()
    private var volumes = listOf<Float>()
    private var labels = listOf<String>()
    private var colors = listOf<String>()

    private val paddingLeft = 100f
    private val paddingBottom = 100f
    private val paddingTop = 160f
    private val itemSpacing = 160f

    private val paintGrid = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0") // Lighter grid
        strokeWidth = 1f
    }

    private val paintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f
        color = Color.LTGRAY
        style = Paint.Style.STROKE
    }

    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 28f
    }

    private val paintBox = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val yStep = 100
    private val maxY = 1000f

    fun setData(data: List<FluidOutput>) {
        volumes = data.map { it.quantity.toFloat() }
        labels = data.map { it.outputTimeFormat }
        colors = data.map { it.colour.ifBlank { "#FFEB3B" } }
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val targetWidth = (itemSpacing * (volumes.size - 1) + paddingLeft + 200f).toInt()
        val desiredHeight = 600
        setMeasuredDimension(targetWidth, desiredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (volumes.isEmpty()) return

        val usableHeight = height - paddingTop - paddingBottom

        for (i in 0..(maxY / yStep).toInt()) {
            val yVal = i * yStep
            val y = paddingTop + usableHeight - (yVal / maxY) * usableHeight
            canvas.drawLine(paddingLeft, y, width.toFloat(), y, paintGrid)
            canvas.drawText("${yVal} ml", 10f, y + 10f, paintText)
        }

        points = volumes.mapIndexed { i, volume ->
            val x = paddingLeft + i * itemSpacing
            val y = paddingTop + usableHeight - ((volume / maxY) * usableHeight * 0.85f)
            x to y
        }

        for (i in 0 until points.size - 1) {
            val (x1, y1) = points[i]
            val (x2, y2) = points[i + 1]
            canvas.drawLine(x1, y1, x2, y2, paintLine)
        }

        points.forEachIndexed { i, (x, y) ->
            val color = Color.parseColor(colors[i])
            paintCircle.color = color
            paintBox.color = color

            canvas.drawCircle(x, y, 6f, paintCircle)

            val label = "${volumes[i].toInt()}ml"
            val textWidth = paintText.measureText(label)
            val boxPadding = 10f
            val boxRect = RectF(
                x - textWidth / 2 - boxPadding,
                y - 70f,
                x + textWidth / 2 + boxPadding,
                y - 40f
            )
            canvas.drawRoundRect(boxRect, 10f, 10f, paintBox)
            paintText.color = Color.BLACK
            canvas.drawText(label, x - textWidth / 2, y - 48f, paintText)

            paintText.color = Color.DKGRAY
            val timeText = labels[i]
            val timeWidth = paintText.measureText(timeText)
            canvas.drawText(timeText, x - timeWidth / 2, height.toFloat() - 20f, paintText)
        }
    }
}
