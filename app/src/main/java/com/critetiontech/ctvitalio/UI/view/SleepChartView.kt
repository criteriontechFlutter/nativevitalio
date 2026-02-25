package com.critetiontech.ctvit
import android.graphics.*
import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONArray
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.critetiontech.ctvitalio.UI.fragments.SleepSegment
import java.text.SimpleDateFormat
import java.util.*
data class SleepSegmentData(
    val id: String = UUID.randomUUID().toString(),
    val kind: SleepKind,
    val start: Double,
    val end: Double,
    val labelValue: Int = 0
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

class SleepTimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var segments: List<SleepSegmentData> = emptyList()
    private var totalTime: Double = 0.0

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // ✅ Grey border connecting line
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
    }

    private val yLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 30f
    }

    private val leftMargin = 140f
    private val rightMargin = 40f
    private val topMargin = 18f
    private val bottomMargin = 18f
    private val baselineHeight = 18f
    private val cornerRadius = 6f

    private val timeLabels = listOf("2 AM", "5 AM", "8 AM", "11 AM")

    fun setSegments(data: List<SleepSegmentData>, totalTime: Double) {
        this.segments = data
        this.totalTime = totalTime
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {

        super.onDraw(canvas)

        if (segments.isEmpty() || totalTime <= 0) return

        val chartWidth = width - leftMargin - rightMargin
        val chartHeight = height - topMargin - bottomMargin
        val rowHeight = chartHeight / SleepKind.ordered.size

        // =====================
        // Y AXIS
        // =====================
        SleepKind.ordered.forEachIndexed { index, kind ->

            val y = topMargin + rowHeight * index + rowHeight / 2

            canvas.drawLine(leftMargin, y, leftMargin + chartWidth, y, gridPaint)

            canvas.drawText(
                kind.display,
                20f,
                y + 10f,
                yLabelPaint
            )
        }

        // =====================
        // X AXIS
        // =====================
        timeLabels.forEachIndexed { i, label ->

            val x = leftMargin + i * (chartWidth / (timeLabels.size - 1))
            val textWidth = timeLabelPaint.measureText(label)

            canvas.drawText(
                label,
                x - textWidth / 2,
                height - 20f,
                timeLabelPaint
            )
        }

        // =====================
        // DRAW BARS
        // =====================
        barPaint.style = Paint.Style.FILL

        segments.forEach { seg ->

            val yCenter = topMargin + rowHeight * seg.kind.index + rowHeight / 2

            val xStart =
                leftMargin + (seg.start.toFloat() / totalTime * chartWidth)

            val xEnd =
                leftMargin + (seg.end.toFloat() / totalTime * chartWidth)

            barPaint.color = seg.kind.color

            val rect = RectF(
                xStart.toFloat(),
                yCenter - baselineHeight / 2,
                xEnd.toFloat(),
                yCenter + baselineHeight / 2
            )

            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint)
        }

        // =====================
        // FULL BORDER CONNECT LINE (NO PADDING)
        // =====================
        val path = Path()

        segments.forEachIndexed { index, seg ->

            val yCenter = topMargin + rowHeight * seg.kind.index + rowHeight / 2

            val topEdge = yCenter - baselineHeight / 2

            val xStart =
                leftMargin + (seg.start.toFloat() / totalTime * chartWidth)

            val xEnd =
                leftMargin + (seg.end.toFloat() / totalTime * chartWidth)

            if (index == 0) {
                path.moveTo(xStart.toFloat(), topEdge)
            }

            // top horizontal
            path.lineTo(xEnd.toFloat(), topEdge)

            if (index < segments.size - 1) {

                val next = segments[index + 1]

                val nextYCenter =
                    topMargin + rowHeight * next.kind.index + rowHeight / 2

                val nextTop = nextYCenter - baselineHeight / 2

                val nextX =
                    leftMargin + (next.start.toFloat() / totalTime * chartWidth)

                // vertical transition
                path.lineTo(nextX.toFloat(), nextTop)
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