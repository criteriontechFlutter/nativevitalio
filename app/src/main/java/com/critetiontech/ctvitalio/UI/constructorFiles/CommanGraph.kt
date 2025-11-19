package com.critetiontech.ctvitalio.UI.constructorFiles
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class HeartRateGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // === DATA MODEL ===
    data class HeartRatePoint(val timestamp: Long, val bpm: Int)

    private val dataPoints = mutableListOf<HeartRatePoint>()
    private var lowestHeartRate = 56
      var graphTile = "Heart Rate"

    // === AXIS CONTROL ===
    private var autoYAxis = true
    private var autoXAxis = true
    private var startTime: Long? = null
    private var endTime: Long? = null

    var minBpmValue = 50
        private set
    var maxBpmValue = 140
        private set

    var yAxisGridLines = mutableListOf<Int>()
    var timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    var numberOfTimeLabels = 5
    var thresholdValue: Int? = null

    // === STYLE & VISUALS ===
    var showGradient = true

    val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E91E63") // pink line
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val thresholdPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2196F3")
        strokeWidth = 3f
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#757575")
        textSize = 32f
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#424242")
        textSize = 36f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val bpmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#212121")
        textSize = 64f
        typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 1.2f
    }


    private val path = Path()
    private val fillPath = Path()
    private val padding = 80f
    private val topPadding = 180f

    // === PUBLIC API ===
    fun setData(points: List<HeartRatePoint>) {
        dataPoints.clear()
        dataPoints.addAll(points)
        updateDynamicRanges()
        invalidate()
    }

    fun addDataPoint(timestamp: Long, bpm: Int) {
        dataPoints.add(HeartRatePoint(timestamp, bpm))
        updateDynamicRanges()
        invalidate()
    }

    fun clearData() {
        dataPoints.clear()
        invalidate()
    }

    fun setTimeRange(startTime: Long, endTime: Long) {
        this.startTime = startTime
        this.endTime = endTime
        autoXAxis = false
        invalidate()
    }

    fun setYAxisRange(min: Int, max: Int) {
        minBpmValue = min
        maxBpmValue = max
        autoYAxis = false
        invalidate()
    }

    fun resetDynamicAxes() {
        autoYAxis = true
        autoXAxis = true
        updateDynamicRanges()
        invalidate()
    }

    // === INTERNAL HELPERS ===
    private fun updateDynamicRanges() {
        if (dataPoints.isEmpty()) return

        if (autoYAxis) {
            minBpmValue = dataPoints.minOf { it.bpm } - 5
            maxBpmValue = dataPoints.maxOf { it.bpm } + 5
            lowestHeartRate = dataPoints.minOf { it.bpm }
            generateDynamicYGrid()
        }

        if (autoXAxis) {
            startTime = dataPoints.minOf { it.timestamp }
            endTime = dataPoints.maxOf { it.timestamp }
        }
    }

    private fun generateDynamicYGrid() {
        yAxisGridLines.clear()
        val range = maxBpmValue - minBpmValue
        val step = max(10, range / 4)
        var value = (minBpmValue / 10f).roundToInt() * 10
        while (value <= maxBpmValue) {
            yAxisGridLines.add(value)
            value += step
        }
    }

    // === DRAWING ===
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataPoints.isEmpty()) return

        val graphWidth = width - 2 * padding
        val graphHeight = height - topPadding - padding
        val minTime = startTime ?: dataPoints.minOf { it.timestamp }
        val maxTime = endTime ?: dataPoints.maxOf { it.timestamp }
        val timeRange = (maxTime - minTime).coerceAtLeast(1L)
        val range = (maxBpmValue - minBpmValue).coerceAtLeast(1)

        // ==== TITLE ====
        canvas.drawText(graphTile, padding, 50f, titlePaint)
        canvas.drawText("$lowestHeartRate bpm", padding, 120f, bpmPaint)

        // ==== Y-AXIS GRID ====
        yAxisGridLines.forEach { bpm ->
            if (bpm in minBpmValue..maxBpmValue) {
                val y = topPadding + graphHeight - ((bpm - minBpmValue).toFloat() / range * graphHeight)
                canvas.drawLine(padding, y, width - padding, y, gridPaint)
                canvas.drawText(bpm.toString(), 20f, y + 10f, textPaint)
            }
        }

        // ==== THRESHOLD LINE ====
        thresholdValue?.let {
            val y = topPadding + graphHeight - ((it - minBpmValue).toFloat() / range * graphHeight)
            canvas.drawLine(padding, y, width - padding, y, thresholdPaint)
        }

        // ==== X-AXIS LABELS ====
        val labelCount = numberOfTimeLabels.coerceAtMost(dataPoints.size)
        val timeStep = if (labelCount > 1) timeRange / (labelCount - 1) else 1L
        for (i in 0 until labelCount) {
            val timestamp = minTime + i * timeStep
            val x = padding + (i.toFloat() / (labelCount - 1)) * graphWidth
            val label = timeFormat.format(Date(timestamp))
            val tw = textPaint.measureText(label)
            canvas.drawText(label, x - tw / 2, height - 30f, textPaint)
        }

        // ==== GRAPH LINE (SMOOTH CURVE) ====
        val sortedPoints = dataPoints.sortedBy { it.timestamp }
        path.reset()
        fillPath.reset()

        if (sortedPoints.size > 1) {
            for (i in 0 until sortedPoints.size - 1) {
                val p1 = sortedPoints[i]
                val p2 = sortedPoints[i + 1]

                val x1 = padding + ((p1.timestamp - minTime).toFloat() / timeRange) * graphWidth
                val y1 = topPadding + graphHeight - ((p1.bpm - minBpmValue).toFloat() / range * graphHeight)

                val x2 = padding + ((p2.timestamp - minTime).toFloat() / timeRange) * graphWidth
                val y2 = topPadding + graphHeight - ((p2.bpm - minBpmValue).toFloat() / range * graphHeight)

                if (i == 0) path.moveTo(x1, y1)

                val midX = (x1 + x2) / 2
                path.cubicTo(midX, y1, midX, y2, x2, y2)
            }
        }

        // Gradient fill below line
        if (showGradient) {
            fillPath.addPath(path)
            fillPath.lineTo(padding + graphWidth, topPadding + graphHeight)
            fillPath.lineTo(padding, topPadding + graphHeight)
            fillPath.close()

            val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    Color.parseColor("#33E91E63"),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
                style = Paint.Style.FILL
            }
            canvas.drawPath(fillPath, gradientPaint)
        }

        // Draw main line
        canvas.drawPath(path, linePaint)

        // Optional debug points
//        sortedPoints.forEach { p ->
//            val x = padding + ((p.timestamp - minTime).toFloat() / timeRange) * graphWidth
//            val y = topPadding + graphHeight - ((p.bpm - minBpmValue).toFloat() / range * graphHeight)
//            canvas.drawCircle(x, y, 4f, debugPointPaint)
//        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 900
        val desiredHeight = 550
        val w = resolveSize(desiredWidth, widthMeasureSpec)
        val h = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(w, h)
    }
}