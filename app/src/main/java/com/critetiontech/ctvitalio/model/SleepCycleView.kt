package com.critetiontech.ctvitalio.model
import LegendItem
import SleepCycle
import SleepCyclesData
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
 import com.critetiontech.ctvitalio.R
import java.text.SimpleDateFormat
import java.util.*
val Int.dp: Int
    get() = (this * android.content.res.Resources.getSystem().displayMetrics.density).toInt()

class SleepCycleView {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Setup sleep cycle graph from your JSON data
     */
    fun setupSleepCycleGraph(
        context: Context,
        sleepGraphContainer: FrameLayout,
        tvSleepCycleCount: TextView,
        cyclesData: SleepCyclesData,
        timeLabels: List<TextView>? = null  // Optional: pass 5 TextViews for time labels
    ) {
        // Update header text
        val totalCycles = cyclesData.fullCount + cyclesData.partialCount
        tvSleepCycleCount.text = "Sleep Cycle $totalCycles"

        // Clear existing views
        sleepGraphContainer.removeAllViews()

        // Calculate total duration for weight distribution
        val totalDuration = calculateTotalDuration(cyclesData.cycles)

        // Build sleep bars
        for (cycle in cyclesData.cycles) {
            val bar = View(context)
            val duration = calculateDuration(cycle.startTime, cycle.endTime)
            val weight = duration.toFloat() / totalDuration.toFloat()

            val params = LinearLayout.LayoutParams(0, 40.dp, weight)
            params.setMargins(2.dp, 0, 2.dp, 0)
            bar.layoutParams = params

            // Set background based on cycle type and color
            bar.background = when (cycle.cycleType) {
                "complete" -> {
                    cycle.color?.let {
                        createBarDrawable(it, lightenColor(it))
                    } ?: createBarDrawable("#008A46", "#5AE3B1")
                }
                "partial" -> {
                    cycle.color?.let {
                        createBarDrawable(it, lightenColor(it))
                    } ?: createBarDrawable("#004223", "#5AE3B1")
                }
                else -> createBarDrawable("#F5F5F5", "#FFFFFF")
            }

            sleepGraphContainer.addView(bar)
        }

        // Update time labels if provided
        timeLabels?.let { labels ->
            if (labels.size >= 5) {
                updateTimeLabels(labels, cyclesData.cycles.first().startTime)
            }
        }
    }

    /**
     * Setup with movement indicators (optional)
     */
    fun setupWithMovements(
        context: Context,
        sleepGraphContainer: LinearLayout,
        movementRowContainer: LinearLayout,
        cyclesData: SleepCyclesData,
        movements: List<Boolean>
    ) {
        // Build movement indicators
        movementRowContainer.removeAllViews()

        for (hasMovement in movements) {
            val bar = View(context)
            val params = LinearLayout.LayoutParams(3.dp, LinearLayout.LayoutParams.MATCH_PARENT)
            params.setMargins(1.dp, 0, 1.dp, 0)
            bar.layoutParams = params
            bar.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 2.dp.toFloat()
                setColor(if (hasMovement) 0xFF4BA3FF.toInt() else 0xFFD0D0D0.toInt())
            }
            movementRowContainer.addView(bar)
        }
    }

    /**
     * Create gradient drawable for sleep bars
     */
    private fun createBarDrawable(startColor: String, endColor: String): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(
                android.graphics.Color.parseColor(startColor),
                android.graphics.Color.parseColor(endColor)
            )
        ).apply {
            cornerRadius = 4.dp.toFloat()
        }
    }

    /**
     * Calculate duration between two timestamps in milliseconds
     */
    private fun calculateDuration(startTime: String, endTime: String): Long {
        val start = dateFormat.parse(startTime)
        val end = dateFormat.parse(endTime)
        return end.time - start.time
    }

    /**
     * Calculate total duration of all cycles
     */
    private fun calculateTotalDuration(cycles: List<SleepCycle>): Long {
        if (cycles.isEmpty()) return 0
        val firstStart = dateFormat.parse(cycles.first().startTime)
        val lastEnd = dateFormat.parse(cycles.last().endTime)
        return lastEnd.time - firstStart.time
    }

    /**
     * Lighten a hex color for gradient effect
     */
    private fun lightenColor(hexColor: String): String {
        val color = android.graphics.Color.parseColor(hexColor)
        val r = (android.graphics.Color.red(color) * 1.3).toInt().coerceAtMost(255)
        val g = (android.graphics.Color.green(color) * 1.3).toInt().coerceAtMost(255)
        val b = (android.graphics.Color.blue(color) * 1.3).toInt().coerceAtMost(255)
        return String.format("#%02X%02X%02X", r, g, b)
    }

    /**
     * Update time labels based on sleep start time
     * Displays 5 evenly spaced time points across the sleep period
     */
    private fun updateTimeLabels(labels: List<TextView>, startTimeStr: String) {
        val startTime = dateFormat.parse(startTimeStr)
        val calendar = Calendar.getInstance()
        calendar.time = startTime

        val timeFormat = SimpleDateFormat("h a", Locale.getDefault()) // e.g., "11 PM"

        // Calculate time interval between labels
        // Assuming 8 hour sleep period, each label is 2 hours apart
        val hoursInterval = 2

        for (i in 0 until minOf(labels.size, 5)) {
            labels[i].text = timeFormat.format(calendar.time)
            calendar.add(Calendar.HOUR_OF_DAY, hoursInterval)
        }
    }

    /**
     * Alternative: Update time labels with custom hour interval
     */
    fun updateTimeLabelsCustom(
        labels: List<TextView>,
        startTimeStr: String,
        endTimeStr: String
    ) {
        val startTime = dateFormat.parse(startTimeStr)
        val endTime = dateFormat.parse(endTimeStr)
        val calendar = Calendar.getInstance()
        calendar.time = startTime

        val timeFormat = SimpleDateFormat("h a", Locale.getDefault())

        // Calculate total duration and interval
        val totalMinutes = (endTime.time - startTime.time) / (1000 * 60)
        val intervalMinutes = totalMinutes / (labels.size - 1)

        for (i in labels.indices) {
            labels[i].text = timeFormat.format(calendar.time)
            calendar.add(Calendar.MINUTE, intervalMinutes.toInt())
        }
    }

    /**
     * Parse your JSON data into SleepCyclesData
     */
    fun parseFromJson(): SleepCyclesData {
        val cycles = listOf(
            SleepCycle("2025-11-17T18:22:00Z", "2025-11-17T18:27:00Z", "none", null),
            SleepCycle("2025-11-17T18:27:00Z", "2025-11-17T18:47:00Z", "partial", "#004223"),
            SleepCycle("2025-11-17T18:47:00Z", "2025-11-17T20:47:00Z", "none", null),
            SleepCycle("2025-11-17T20:47:00Z", "2025-11-17T22:12:00Z", "complete", "#008A46"),
            SleepCycle("2025-11-17T22:12:00Z", "2025-11-17T22:17:00Z", "none", null),
            SleepCycle("2025-11-17T22:17:00Z", "2025-11-17T23:52:00Z", "complete", "#008A46"),
            SleepCycle("2025-11-17T23:52:00Z", "2025-11-17T23:57:00Z", "none", null),
            SleepCycle("2025-11-17T23:57:00Z", "2025-11-18T01:12:00Z", "complete", "#008A46"),
            SleepCycle("2025-11-18T01:12:00Z", "2025-11-18T01:17:00Z", "none", null),
            SleepCycle("2025-11-18T01:17:00Z", "2025-11-18T01:22:00Z", "partial", "#004223"),
            SleepCycle("2025-11-18T01:22:00Z", "2025-11-18T01:27:00Z", "none", null)
        )

        val fullCount = cycles.count { it.cycleType == "complete" }
        val partialCount = cycles.count { it.cycleType == "partial" }

        return SleepCyclesData(
            title = "Sleep Cycles • ${fullCount + partialCount}",
            cycles = cycles,
            fullCount = fullCount,
            partialCount = partialCount
        )
    }
}
