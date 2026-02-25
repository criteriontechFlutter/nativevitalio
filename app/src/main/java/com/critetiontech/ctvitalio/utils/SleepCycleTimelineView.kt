package com.critetiontech.ctvitalio.utils
import SleepCycle
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration


class SleepCycleTimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var cycles: List<SleepCycle> = emptyList()
    private var totalMinutes: Long = 1

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        strokeWidth = 2f
    }

    private val formatter =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        else null

    private val displayFormatter =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            DateTimeFormatter.ofPattern("h:mm a")
        else null

    private val leftPadding = 40f
    private val rightPadding = 40f
    private val topPadding = 30f
    private val barHeight = 40f
    private val cornerRadius = 12f
    private val barGap = 6f
    private val textPadding = 20f // extra padding for first/last labels

    // -------------------------
    // Bind JSON SleepCycles Data
    // -------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun setSleepCycles(data: List<SleepCycle>) {
        cycles = data

        if (data.isNotEmpty()) {
            val start = LocalDateTime.parse(data.first().startTime, formatter!!)
            val end = LocalDateTime.parse(data.last().endTime, formatter)
            totalMinutes = Duration.between(start, end).toMinutes()
            if (totalMinutes <= 0) totalMinutes = 1
        }

        invalidate()
    }

    // -------------------------
    // Draw Timeline
    // -------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (cycles.isEmpty()) return

        val chartWidth = width - leftPadding - rightPadding
        val baseTime = LocalDateTime.parse(cycles.first().startTime, formatter!!)

        // ===== Draw Sleep Bars with Colors from JSON =====
        cycles.forEach { cycle ->
            val start = LocalDateTime.parse(cycle.startTime, formatter)
            val end = LocalDateTime.parse(cycle.endTime, formatter)

            val startMin = Duration.between(baseTime, start).toMinutes()
            val endMin = Duration.between(baseTime, end).toMinutes()

            val xStart = leftPadding + (startMin.toFloat() / totalMinutes * chartWidth)
            val xEnd = leftPadding + (endMin.toFloat() / totalMinutes * chartWidth)

            val rect = RectF(
                xStart + barGap,
                topPadding,
                xEnd - barGap,
                topPadding + barHeight
            )

            // Use color from JSON if available, else default gray
            barPaint.color = cycle.color?.let { Color.parseColor(it) } ?: Color.parseColor("#E0E0E0")
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint)
        }

        // ===== Draw X Axis Ticks & Labels =====
        val axisY = topPadding + barHeight + 25f
        val intervals = 4

        for (i in 0..intervals) {
            val ratio = i / intervals.toFloat()
            val x = leftPadding + ratio * chartWidth

            // Vertical tick
            canvas.drawLine(x, axisY - 12f, x, axisY + 12f, tickPaint)

            // Time label dynamically calculated
            val minutes = (ratio * totalMinutes).toLong()
            val labelTime = baseTime.plusMinutes(minutes)
            val label = labelTime.format(displayFormatter)

            val adjustedX = when (i) {
                0 -> x + textPadding
                intervals -> x - textPadding
                else -> x
            }

            canvas.drawText(label, adjustedX, axisY + 45f, axisPaint)
        }
    }
}