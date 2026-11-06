package com.critetiontech.ctvitalio.sync.domain

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/** Everything the state section renders. The section itself looks the same in every state. */
data class StateSectionModel(
    val state: SyncState,
    val hold: Hold?,
    val leadKey: String,
    val lead: String,
    val reason: String,
    val eyebrowLabel: String,
    /** One slot per night, oldest first. Null is an unworn night and draws as a gap. */
    val dots: List<Float?>,
    val dashedUsual: Boolean,
    val showAttentionTick: Boolean,
    val showDoors: Boolean,
)

object StateSection {
    private const val SLOTS = 14

    fun build(nights: List<Night>, suppressedDays: Set<Int>): StateSectionModel {
        val evaluation = SyncStateMachine.replay(nights, suppressedDays)
        val sleep = Usuals.band(nights, Metric.SLEEP)
        val rhr = Usuals.band(nights, Metric.RHR)
        val hold = evaluation.hold
        val state = evaluation.state

        val leadKey: String
        val lead: String
        val reason: String
        when {
            hold == Hold.LOW_WEAR -> {
                val recent = nights.takeLast(SLOTS)
                leadKey = "state.low_wear.lead"
                lead = SyncCopy.text(leadKey, "n" to recent.count { it.sleepMin != null })
                reason = SyncCopy.text("state.low_wear.reason", "r" to recent.count { it.rhr != null })
            }
            hold == Hold.QUIET -> {
                leadKey = "state.quiet.lead"
                lead = SyncCopy.text(leadKey)
                reason = SyncCopy.text("state.quiet.reason")
            }
            state == SyncState.BASELINING -> {
                val worn = nights.count { it.worn }
                leadKey = "state.baselining.lead"
                lead = SyncCopy.text(leadKey)
                reason = SyncCopy.text(
                    "state.baselining.reason",
                    "n" to worn,
                    "more" to max(SyncStateMachine.BASELINE_NIGHTS - worn, 0),
                )
            }
            state == SyncState.IN_SYNC -> {
                val count = NightReading.rangeCount(nights)
                leadKey = "state.in_sync.lead"
                lead = SyncCopy.text(leadKey)
                reason = SyncCopy.text("state.in_sync.reason", "n" to count.inRange, "worn" to count.worn)
            }
            state == SyncState.DRIFTING -> {
                leadKey = "state.drifting.lead"
                lead = SyncCopy.text(leadKey)
                reason = driftingReason(nights, sleep, rhr)
            }
            state == SyncState.OUT_OF_SYNC -> {
                val lastNight = nights.last().day
                val since = evaluation.driftSinceDay ?: lastNight
                val weeks = ((lastNight - since) / 7.0).roundToInt().coerceAtLeast(1)
                leadKey = "state.out_of_sync.lead"
                lead = SyncCopy.text(leadKey, "weeks" to SyncFormat.weeks(weeks))
                reason = outOfSyncReason(nights, sleep, rhr)
            }
            else -> {
                leadKey = "state.recovering.lead"
                lead = SyncCopy.text(leadKey, "when" to whenBack(evaluation.backSinceDay ?: nights.last().day))
                reason = recoveringReason(nights, sleep, rhr)
            }
        }

        val first = nights.takeLast(SLOTS).firstOrNull()?.day
        val last = nights.lastOrNull()?.day
        val label = if (state == SyncState.BASELINING || first == null || last == null) {
            SyncCopy.text("state.baselining.label", "n" to nights.count { it.worn })
        } else {
            "${SyncDates.shortDate(last - SLOTS + 1)} – ${SyncDates.shortDate(last)}"
        }

        return StateSectionModel(
            state = state,
            hold = hold,
            leadKey = leadKey,
            lead = lead,
            reason = reason,
            eyebrowLabel = label,
            dots = dots(nights, sleep, rhr, state, hold),
            dashedUsual = state == SyncState.BASELINING,
            showAttentionTick = hold == null && state == SyncState.OUT_OF_SYNC,
            showDoors = hold == null && (state == SyncState.DRIFTING || state == SyncState.OUT_OF_SYNC),
        )
    }

    /** Sat or Sun reads as "the weekend"; any other day is named. Only the slot varies. */
    private fun whenBack(day: Int): String {
        val weekday = SyncDates.weekday(day)
        return if (weekday == SyncDates.SATURDAY || weekday == SyncDates.SUNDAY) {
            "the weekend"
        } else {
            SyncDates.weekdayName(day)
        }
    }

    /**
     * The canvas draws one row of dots but the state reads two metrics. Each dot shows whichever of
     * sleep or resting heart rate sits furthest from its usual, with heavier always upward, so
     * drifting dots slide up as on the States sheet. This is not a composite: nothing is averaged.
     */
    private fun dots(
        nights: List<Night>,
        sleep: Band?,
        rhr: Band?,
        state: SyncState,
        hold: Hold?,
    ): List<Float?> {
        val recent = nights.takeLast(SLOTS)
        val padded: List<Night?> = when {
            state == SyncState.BASELINING -> recent
            else -> List(SLOTS - recent.size) { null } + recent
        }
        return padded.map { night ->
            if (night == null) return@map null
            if (state == SyncState.BASELINING) return@map baselineDot(night, nights)
            if (hold == Hold.LOW_WEAR) {
                return@map night.sleepMin?.let { value -> sleep?.let { -it.normalized(value) } }
            }
            val fromSleep = night.sleepMin?.let { value -> sleep?.let { -it.normalized(value) } }
            val fromRhr = night.rhr?.let { value -> rhr?.let { it.normalized(value) } }
            listOfNotNull(fromSleep, fromRhr).maxByOrNull { abs(it) }
        }
    }

    /** Before night 7 there is no band, so dots only wobble a little around the dashed line. */
    private fun baselineDot(night: Night, nights: List<Night>): Float? {
        val value = night.sleepMin ?: return null
        val values = nights.mapNotNull { it.sleepMin }
        if (values.isEmpty()) return null
        return ((value - values.average()) / 15.0).toFloat().coerceIn(-1f, 1f)
    }

    private fun driftingReason(nights: List<Night>, sleep: Band?, rhr: Band?): String {
        val recent = nights.takeLast(SLOTS).filter { it.worn }
        val rhrOut = rhr?.let { band -> recent.mapNotNull { it.rhr }.filter { !band.contains(it) } }.orEmpty()
        val sleepOut = sleep?.let { band -> recent.mapNotNull { it.sleepMin }.filter { !band.contains(it) } }.orEmpty()
        return if (rhr != null && rhrOut.size >= sleepOut.size && rhrOut.isNotEmpty()) {
            SyncCopy.text(
                "state.drifting.reason.rhr",
                "d" to abs(rhrOut.average() - rhr.usual).roundToInt(),
                "n" to rhrOut.size,
            )
        } else if (sleep != null && sleepOut.isNotEmpty()) {
            SyncCopy.text(
                "state.drifting.reason.sleep",
                "d" to abs(sleep.usual - sleepOut.average()).roundToInt(),
                "n" to sleepOut.size,
            )
        } else {
            ""
        }
    }

    private fun outOfSyncReason(nights: List<Night>, sleep: Band?, rhr: Band?): String {
        if (sleep == null || rhr == null) return ""
        val both = nights.takeLast(SLOTS).filter {
            it.worn && !sleep.contains(it.sleepMin!!) && !rhr.contains(it.rhr!!)
        }
        if (both.isEmpty()) return ""
        return SyncCopy.text(
            "state.out_of_sync.reason",
            "s" to abs(sleep.usual - both.map { it.sleepMin!! }.average()).roundToInt(),
            "r" to abs(both.map { it.rhr!! }.average() - rhr.usual).roundToInt(),
            "n" to both.size,
        )
    }

    private fun recoveringReason(nights: List<Night>, sleep: Band?, rhr: Band?): String {
        val sleepCloser = closer(nights, Metric.SLEEP, sleep)
        val rhrCloser = closer(nights, Metric.RHR, rhr)
        return when {
            sleepCloser != null && sleepCloser > 0 ->
                SyncCopy.text("state.recovering.reason.sleep", "m" to sleepCloser)
            rhrCloser != null && rhrCloser > 0 ->
                SyncCopy.text("state.recovering.reason.rhr", "m" to rhrCloser)
            else -> ""
        }
    }

    /** How much nearer the usual this fortnight sat than the one before it. */
    private fun closer(nights: List<Night>, metric: Metric, band: Band?): Int? {
        if (band == null) return null
        fun distance(list: List<Night>): Double? {
            val values = list.mapNotNull(metric.read)
            return if (values.isEmpty()) null else values.map { abs(it - band.usual) }.average()
        }
        val now = distance(nights.takeLast(SLOTS)) ?: return null
        val before = distance(nights.dropLast(SLOTS).takeLast(SLOTS)) ?: return null
        return (before - now).roundToInt()
    }
}
