package com.critetiontech.ctvitalio.sync.domain

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * The person's usual and its band (section 5). The thresholds are the spec's proposed defaults and
 * still need confirming against COHORT_NOV2026, so they live here as named constants.
 */
object Usuals {
    const val WINDOW_NIGHTS = 28
    const val JUDGED_FORTNIGHT = 14
    const val AVAILABLE_FROM_NIGHT = 7

    /**
     * The window is the latest 28 worn nights that end 14 nights ago, so the fortnight being judged
     * never moves its own reference. The spec also promises a usual from night 7, when no such
     * nights exist yet, so until seven older nights are available the window is every worn night
     * so far. Returns null, meaning "not enough nights", rather than failing.
     */
    fun band(nights: List<Night>, metric: Metric, asOfIndex: Int = nights.lastIndex): Band? {
        if (asOfIndex < 0) return null
        val history = nights.subList(0, asOfIndex + 1)
        val older = history.dropLast(JUDGED_FORTNIGHT).mapNotNull(metric.read)
        val window = if (older.size >= AVAILABLE_FROM_NIGHT) {
            older.takeLast(WINDOW_NIGHTS)
        } else {
            history.mapNotNull(metric.read)
        }
        if (window.size < AVAILABLE_FROM_NIGHT) return null
        val sorted = window.sorted()
        return Band(percentile(sorted, 0.5), percentile(sorted, 0.25), percentile(sorted, 0.75))
    }

    fun percentile(sorted: List<Int>, fraction: Double): Double {
        val position = (sorted.size - 1) * fraction
        val below = floor(position).toInt()
        val above = (below + 1).coerceAtMost(sorted.lastIndex)
        return sorted[below] + (sorted[above] - sorted[below]) * (position - below)
    }

    /**
     * The spec's text says "more than a tolerance", but its canvas draws a caret at exactly the
     * tolerance (6h 45m against 6h 40m, 58 against 57). This follows the canvas until confirmed.
     */
    fun caret(value: Int, usual: Double, metric: Metric): Caret {
        val difference = value - usual.roundToInt()
        return when {
            abs(difference) < metric.tolerance -> Caret.NONE
            difference > 0 -> Caret.UP
            else -> Caret.DOWN
        }
    }
}

/** What the last 14 nights say, for the reason line. */
data class RangeCount(val inRange: Int, val worn: Int)

object NightReading {
    const val FORTNIGHT = 14

    fun inside(nights: List<Night>, index: Int): Boolean? {
        val night = nights[index]
        if (!night.worn) return null
        val sleep = Usuals.band(nights, Metric.SLEEP, index) ?: return null
        val rhr = Usuals.band(nights, Metric.RHR, index) ?: return null
        return sleep.contains(night.sleepMin!!) && rhr.contains(night.rhr!!)
    }

    fun bothOutside(nights: List<Night>, index: Int): Boolean {
        val night = nights[index]
        if (!night.worn) return false
        val sleep = Usuals.band(nights, Metric.SLEEP, index) ?: return false
        val rhr = Usuals.band(nights, Metric.RHR, index) ?: return false
        return !sleep.contains(night.sleepMin!!) && !rhr.contains(night.rhr!!)
    }

    /** Unworn nights are excluded from the count, and show as gaps in the drawing. */
    fun rangeCount(nights: List<Night>): RangeCount {
        val start = (nights.size - FORTNIGHT).coerceAtLeast(0)
        val readings = (start until nights.size).mapNotNull { inside(nights, it) }
        return RangeCount(readings.count { it }, readings.size)
    }
}
