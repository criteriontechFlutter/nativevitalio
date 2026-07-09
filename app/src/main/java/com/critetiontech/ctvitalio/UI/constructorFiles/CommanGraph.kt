package com.critetiontech.ctvitalio.UI.constructorFiles
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class HeartRateGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class HeartRatePoint(val timestamp: Long, val bpm: Int)

    private val dataPoints = mutableListOf<HeartRatePoint>()

    // ================= HEADER =================
    private var graphTitle: String = "Heart Rate"
    private var graphDisplayValue: String = "--"
    private var graphUnit: String = "bpm"

    fun bindHeader(title: String, value: Number, unit: String) {
        graphTitle = title
        graphDisplayValue = value.toString()
        graphUnit = unit
        invalidate()
    }

    // ================= AXIS CONTROL =================
    private var autoYAxis = true
    private var autoXAxis = true
    private var startTime: Long? = null
    private var endTime: Long? = null

    var minBpmValue = 50
        private set
    var maxBpmValue = 140
        private set

    var yAxisGridLines = mutableListOf<Int>()
    var numberOfTimeLabels = 5
    var timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    var thresholdValue: Int? = null
    var showGradient = true

    // ================= PAINTS =================
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E91E63")
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 1.2f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#757575")
        textSize = 28f
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#424242")
        textSize = 36f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#212121")
        textSize = 60f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val thresholdPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        strokeWidth = 3f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val path = Path()
    private val fillPath = Path()

    private val padding = 80f
    private val topPadding = 180f

    // ================= PUBLIC FUNCTIONS =================

    fun setData(points: List<HeartRatePoint>) {
        dataPoints.clear()
        dataPoints.addAll(points)
        updateDynamicRanges()
        invalidate()
    }

    fun setTimeRange(start: Long, end: Long) {
        startTime = start
        endTime = end
        autoXAxis = false
        invalidate()
    }

    fun setYAxisRange(min: Int, max: Int) {
        minBpmValue = min
        maxBpmValue = max
        autoYAxis = false
        generateYGrid()
        invalidate()
    }

    fun resetDynamicAxes() {
        autoYAxis = true
        autoXAxis = true
        updateDynamicRanges()
        invalidate()
    }

    // ================= DYNAMIC RANGE =================

    private fun updateDynamicRanges() {
        if (dataPoints.isEmpty()) return

        if (autoYAxis) {
            val maxData = dataPoints.maxOf { it.bpm }
            val minData = dataPoints.minOf { it.bpm }

            minBpmValue = (minData - 10).coerceAtLeast(0)
            maxBpmValue = ((maxData + 10) / 10) * 10

            generateYGrid()
        }

        if (autoXAxis) {
            startTime = dataPoints.minOf { it.timestamp }
            endTime = dataPoints.maxOf { it.timestamp }
        }
    }

    private fun generateYGrid() {
        yAxisGridLines.clear()
        val range = maxBpmValue - minBpmValue
        val step = max(10, range / 4)

        var value = minBpmValue
        while (value <= maxBpmValue) {
            yAxisGridLines.add(value)
            value += step
        }

        if (!yAxisGridLines.contains(maxBpmValue))
            yAxisGridLines.add(maxBpmValue)

        yAxisGridLines.sort()
    }

    // ================= DRAW =================

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataPoints.isEmpty()) return

        val graphWidth = width - 2 * padding
        val graphHeight = height - topPadding - padding

        val minTime = startTime ?: dataPoints.minOf { it.timestamp }
        val maxTime = endTime ?: dataPoints.maxOf { it.timestamp }

        val timeRange = (maxTime - minTime).coerceAtLeast(1)
        val valueRange = (maxBpmValue - minBpmValue).coerceAtLeast(1)

        // HEADER
        canvas.drawText(graphTitle, padding, 50f, titlePaint)
        canvas.drawText("$graphDisplayValue $graphUnit", padding, 120f, valuePaint)

        // Y GRID
        yAxisGridLines.forEach { bpm ->
            val y = topPadding + graphHeight -
                    ((bpm - minBpmValue).toFloat() / valueRange * graphHeight)

            canvas.drawLine(padding, y, width - padding, y, gridPaint)
            canvas.drawText(bpm.toString(), 20f, y + 8f, textPaint)
        }

        // THRESHOLD
        thresholdValue?.let {
            val y = topPadding + graphHeight -
                    ((it - minBpmValue).toFloat() / valueRange * graphHeight)

            canvas.drawLine(padding, y, width - padding, y, thresholdPaint)
        }

        // X LABELS
        val labelCount = numberOfTimeLabels.coerceAtMost(dataPoints.size)
        val timeStep = if (labelCount > 1) timeRange / (labelCount - 1) else 1L

        for (i in 0 until labelCount) {
            val timestamp = minTime + i * timeStep
            val x = padding + (i.toFloat() / (labelCount - 1)) * graphWidth
            val label = timeFormat.format(Date(timestamp))
            canvas.drawText(label, x - textPaint.measureText(label) / 2,
                height - 30f, textPaint)
        }

        // CURVE
        val sorted = dataPoints.sortedBy { it.timestamp }
        path.reset()
        fillPath.reset()

        for (i in 0 until sorted.size - 1) {

            val p1 = sorted[i]
            val p2 = sorted[i + 1]

            val x1 = padding + ((p1.timestamp - minTime).toFloat() / timeRange) * graphWidth
            val y1 = topPadding + graphHeight -
                    ((p1.bpm - minBpmValue).toFloat() / valueRange * graphHeight)

            val x2 = padding + ((p2.timestamp - minTime).toFloat() / timeRange) * graphWidth
            val y2 = topPadding + graphHeight -
                    ((p2.bpm - minBpmValue).toFloat() / valueRange * graphHeight)

            if (i == 0) path.moveTo(x1, y1)

            val midX = (x1 + x2) / 2
            path.cubicTo(midX, y1, midX, y2, x2, y2)
        }

        // GRADIENT
        if (showGradient) {
            fillPath.addPath(path)
            fillPath.lineTo(padding + graphWidth, topPadding + graphHeight)
            fillPath.lineTo(padding, topPadding + graphHeight)
            fillPath.close()

            val gradientPaint = Paint().apply {
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    Color.parseColor("#33E91E63"),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
            }

            canvas.drawPath(fillPath, gradientPaint)
        }

        canvas.drawPath(path, linePaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            resolveSize(900, widthMeasureSpec),
            resolveSize(550, heightMeasureSpec)
        )
    }
}