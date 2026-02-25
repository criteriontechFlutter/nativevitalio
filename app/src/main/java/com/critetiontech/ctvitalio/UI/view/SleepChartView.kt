package com.critetiontech.ctvit

import SleepGraphData
import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class SleepSegmentData(
    val id: String = UUID.randomUUID().toString(),
    val kind: SleepKind,
    val start: LocalDateTime,
    val end: LocalDateTime
)

enum class SleepKind(val display: String, val color: Int) {
    AWAKE("Awake", 0xFFFFA726.toInt()),
    REM("REM", 0xFF64B5F6.toInt()),
    LIGHT("Light", 0xFF429FFA.toInt()),
    DEEP("Deep", 0xFF2B5FC4.toInt());

    companion object {
        val ordered = listOf(AWAKE, REM, LIGHT, DEEP)
    }

    val index: Int
        get() = ordered.indexOf(this)
}

// -------------------------
// Custom View
// -------------------------
class SleepTimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var segments: List<SleepSegmentData> = emptyList()
    private var totalDurationMinutes: Long = 1
    private var startTime: LocalDateTime? = null
    private var endTime: LocalDateTime? = null

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF9E9E9E.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 2f
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x22000000
        strokeWidth = 1f
    }
    private val timeLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    private val yLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 30f
    }

    private val leftMargin = 140f
    private val rightMargin = 40f
    private val topMargin = 18f
    private val bottomMargin = 40f
    private val baselineHeight = 18f
    private val cornerRadius = 6f

    private val formatter =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
        else null

    // -------------------------
    // Bind JSON SleepGraph Data
    // -------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun setSegmentsFromJson(graphList: List<SleepGraphData>) {
        segments = graphList.map { data ->
            val kind = when (data.Type.lowercase()) {
                "awake" -> SleepKind.AWAKE
                "light_sleep" -> SleepKind.LIGHT
                "deep_sleep" -> SleepKind.DEEP
                "rem_sleep" -> SleepKind.REM
                else -> SleepKind.AWAKE
            }

            SleepSegmentData(
                kind = kind,
                start = LocalDateTime.parse(data.Start, formatter),
                end = LocalDateTime.parse(data.End, formatter)
            )
        }

        if (segments.isNotEmpty()) {
            startTime = segments.minOf { it.start }
            endTime = segments.maxOf { it.end }
            totalDurationMinutes = Duration.between(startTime, endTime).toMinutes()
            if (totalDurationMinutes <= 0) totalDurationMinutes = 1
        }

        invalidate()
    }

    // -------------------------
    // Optional: Direct typed binding
    // -------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    fun setSegments(segmentsList: List<SleepSegmentData>) {
        if (segmentsList.isEmpty()) return

        segments = segmentsList
        startTime = segments.minOf { it.start }
        endTime = segments.maxOf { it.end }
        totalDurationMinutes = Duration.between(startTime, endTime).toMinutes().coerceAtLeast(1)

        invalidate()
    }

    // -------------------------
    // Draw Timeline
    // -------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (segments.isEmpty() || totalDurationMinutes <= 0) return

        val chartWidth = width - leftMargin - rightMargin
        val chartHeight = height - topMargin - bottomMargin
        val rowHeight = chartHeight / SleepKind.ordered.size

        // -------------------------
        // Draw Y-axis (Sleep Kind)
        // -------------------------
        SleepKind.ordered.forEachIndexed { index, kind ->
            val y = topMargin + rowHeight * index + rowHeight / 2
            canvas.drawLine(leftMargin, y, leftMargin + chartWidth, y, gridPaint)
            canvas.drawText(kind.display, 20f, y + 10f, yLabelPaint)
        }

        // -------------------------
        // Draw X-axis (Time) Dynamic
        // -------------------------
        val intervals = 4
        startTime?.let { start ->
            for (i in 0..intervals) {
                val ratio = i / intervals.toFloat()
                val x = leftMargin + ratio * chartWidth

                val minutes = (ratio * totalDurationMinutes).toLong()
                val labelTime = start.plusMinutes(minutes)
                val label = labelTime.format(DateTimeFormatter.ofPattern("h:mm a"))

                canvas.drawLine(x, height - bottomMargin, x, height - bottomMargin + 12f, gridPaint)
                canvas.drawText(label, x, height - 8f, timeLabelPaint)
            }
        }

        // -------------------------
        // Draw Bars
        // -------------------------
        barPaint.style = Paint.Style.FILL

        segments.forEach { seg ->
            val yCenter = topMargin + rowHeight * seg.kind.index + rowHeight / 2

            val xStart = leftMargin + (Duration.between(startTime, seg.start).toMinutes().toFloat() / totalDurationMinutes) * chartWidth
            val xEnd = leftMargin + (Duration.between(startTime, seg.end).toMinutes().toFloat() / totalDurationMinutes) * chartWidth

            barPaint.color = seg.kind.color
            val rect = RectF(xStart, yCenter - baselineHeight / 2, xEnd, yCenter + baselineHeight / 2)
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint)
        }

        // -------------------------
        // Full connecting line
        // -------------------------
        val path = Path()
        segments.forEachIndexed { index, seg ->
            val yCenter = topMargin + rowHeight * seg.kind.index + rowHeight / 2
            val topEdge = yCenter - baselineHeight / 2
            val xStart = leftMargin + (Duration.between(startTime, seg.start).toMinutes().toFloat() / totalDurationMinutes) * chartWidth
            val xEnd = leftMargin + (Duration.between(startTime, seg.end).toMinutes().toFloat() / totalDurationMinutes) * chartWidth

            if (index == 0) path.moveTo(xStart, topEdge)
            path.lineTo(xEnd, topEdge)

            if (index < segments.size - 1) {
                val next = segments[index + 1]
                val nextYCenter = topMargin + rowHeight * next.kind.index + rowHeight / 2
                val nextTop = nextYCenter - baselineHeight / 2
                val nextX = leftMargin + (Duration.between(startTime, next.start).toMinutes().toFloat() / totalDurationMinutes) * chartWidth
                path.lineTo(nextX, nextTop)
            }
        }

        canvas.drawPath(path, linePaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(800, widthMeasureSpec)
        val height = resolveSize(350, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}