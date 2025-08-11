package com.critetiontech.ctvitalio.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random
import androidx.core.graphics.toColorInt

class SleepTrackerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#E8E3F3".toColorInt()
        style = Paint.Style.FILL
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#333333".toColorInt()
        textSize = 32f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7C3AED")
        style = Paint.Style.FILL
    }

    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1F1F1F")
        textSize = 72f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#666666")
        textSize = 36f
    }

    private val graphLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7C3AED")
        strokeWidth = 6f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#999999")
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val axisTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#999999")
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#999999")
        style = Paint.Style.FILL
    }

    private var sleepData = generateSleepData()
    private val cornerRadius = 60f

    data class SleepPoint(val hour: Int, val quality: Float)

    private fun generateSleepData(): List<SleepPoint> {
        val data = mutableListOf<SleepPoint>()

        for (hour in 4..9) {
            // Create realistic sleep pattern with some randomness
            val variation = when (hour) {
                4 -> Random.nextFloat() * 0.2f + 0.2f  // Lower quality at start
                5 -> Random.nextFloat() * 0.3f + 0.1f  // Very low quality
                6 -> Random.nextFloat() * 0.4f + 0.2f  // Gradual improvement
                7 -> Random.nextFloat() * 0.3f + 0.4f  // Better quality
                8 -> Random.nextFloat() * 0.4f + 0.5f  // Peak quality
                9 -> Random.nextFloat() * 0.3f + 0.7f  // Highest quality
                else -> 0.5f
            }
            data.add(SleepPoint(hour, variation))
        }
        return data
    }

    fun updateSleepData() {
        sleepData = generateSleepData()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // Draw rounded background
        val backgroundRect = RectF(0f, 0f, width, height)
        canvas.drawRoundRect(backgroundRect, cornerRadius, cornerRadius, backgroundPaint)

        drawContent(canvas, width, height)
    }

    private fun drawContent(canvas: Canvas, width: Float, height: Float) {
        val padding = 40f
        val graphWidth = width * 0.4f
        val graphHeight = height * 0.6f
        val graphStartX = width - graphWidth - padding
        val graphStartY = padding + 40f

        // Draw sleep icon (bed icon)
        drawSleepIcon(canvas, padding + 20f, padding + 20f)

        // Draw title
        canvas.drawText("Sleep Tracker", padding + 80f, padding + 50f, titlePaint)

        // Draw moon icon
        drawMoonIcon(canvas, padding + 20f, padding + 120f)

        // Draw sleep time
        canvas.drawText("06h 41m", padding + 80f, padding + 150f, timePaint)

        // Draw subtitle
        canvas.drawText("", padding + 80f, padding + 200f, subtitlePaint)

        // Draw sleep quality graph
        drawSleepGraph(canvas, graphStartX, graphStartY, graphWidth, graphHeight)

        // Draw time axis
        drawTimeAxis(canvas, graphStartX, graphStartY + graphHeight, graphWidth)
    }

    private fun drawSleepIcon(canvas: Canvas, x: Float, y: Float) {
        val iconSize = 40f
        val rect = RectF(x, y, x + iconSize, y + iconSize * 0.6f)
        canvas.drawRoundRect(rect, 8f, 8f, iconPaint)

        // Draw pillow
        val pillowRect = RectF(x + iconSize * 0.7f, y - iconSize * 0.2f, x + iconSize, y + iconSize * 0.4f)
        canvas.drawRoundRect(pillowRect, 6f, 6f, iconPaint)
    }

    private fun drawMoonIcon(canvas: Canvas, x: Float, y: Float) {
        val moonRadius = 25f
        canvas.drawCircle(x + moonRadius, y + moonRadius, moonRadius, iconPaint)

        // Draw crescent by drawing a smaller circle to create the moon shape
        val crescentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E8E3F3")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(x + moonRadius + 8f, y + moonRadius - 8f, moonRadius * 0.8f, crescentPaint)
    }

    private fun drawSleepGraph(canvas: Canvas, startX: Float, startY: Float, width: Float, height: Float) {
        if (sleepData.isEmpty()) return

        val path = Path()
        val points = mutableListOf<PointF>()

        // Calculate points
        for (i in sleepData.indices) {
            val x = startX + (i.toFloat() / (sleepData.size - 1)) * width
            val y = startY + height - (sleepData[i].quality * height)
            points.add(PointF(x, y))

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                // Create smooth curves using quadratic bezier
                val prevPoint = points[i - 1]
                val controlX = (prevPoint.x + x) / 2
                val controlY = prevPoint.y
                path.quadTo(controlX, controlY, x, y)
            }
        }

        // Draw the sleep quality line
        canvas.drawPath(path, graphLinePaint)

        // Draw dots at data points
        for (point in points) {
            canvas.drawCircle(point.x, point.y, 8f, graphLinePaint)
        }

        // Draw vertical axis lines (dotted)
        drawVerticalAxisLines(canvas, startX, startY, width, height)
    }

    private fun drawVerticalAxisLines(canvas: Canvas, startX: Float, startY: Float, width: Float, height: Float) {
        val dashEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        val dashedLinePaint = Paint(axisPaint).apply {
            pathEffect = dashEffect
            alpha = 100
        }

        // Draw horizontal dotted lines
        for (i in 1..4) {
            val y = startY + (height * i / 5)
            canvas.drawLine(startX, y, startX + width, y, dashedLinePaint)
        }
    }

    private fun drawTimeAxis(canvas: Canvas, startX: Float, startY: Float, width: Float) {
        val timeLabels = listOf("4", "5", "6", "7", "8", "9")

        for (i in timeLabels.indices) {
            val x = startX + (i.toFloat() / (timeLabels.size - 1)) * width

            // Draw time labels
            canvas.drawText(timeLabels[i], x, startY + 50f, axisTextPaint)

            // Draw small dots above labels
            canvas.drawCircle(x, startY - 10f, 4f, dotPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 800
        val desiredHeight = 300

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> minOf(desiredWidth, widthSize)
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> minOf(desiredHeight, heightSize)
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }
}