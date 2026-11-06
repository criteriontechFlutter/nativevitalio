package com.critetiontech.ctvitalio.sync.domain

import kotlin.math.abs
import kotlin.math.roundToInt

/** Hot hours (section 5). The 6 bpm threshold must be validated on the daytime sample before shipping. */
object HotHours {
    const val HOT_ABOVE_BPM = 6
    const val TRAILING_DAYS = 28

    data class HourBand(val hour: Int, val band: Band)

    data class Reading(
        val hotHours: List<Int>,
        /** First later hour back inside that hour's band. Null if it never came back. */
        val backHour: Int?,
    )

    /** The usual for each clock hour: the median of that hour over the trailing 28 days. */
    fun bands(history: List<List<HourMean>>): Map<Int, Band> =
        history.takeLast(TRAILING_DAYS).flatten().groupBy { it.hour }.mapValues { (_, means) ->
            val sorted = means.map { it.bpm }.sorted()
            Band(
                Usuals.percentile(sorted, 0.5),
                Usuals.percentile(sorted, 0.25),
                Usuals.percentile(sorted, 0.75),
            )
        }

    fun read(today: List<HourMean>, bands: Map<Int, Band>): Reading {
        val sorted = today.sortedBy { it.hour }
        val hot = sorted.filter { mean ->
            bands[mean.hour]?.let { mean.bpm - it.usual >= HOT_ABOVE_BPM } == true
        }.map { it.hour }
        if (hot.isEmpty()) return Reading(emptyList(), null)
        val back = sorted.firstOrNull { mean ->
            mean.hour > hot.first() && mean.hour !in hot && bands[mean.hour]?.contains(mean.bpm) == true
        }?.hour
        return Reading(hot, back)
    }

    /**
     * Both directions, always: a sentence naming a hot hour also names when it came back. Each hour's
     * mean sits at the middle of the hour, so an hour back inside reads as half past it.
     */
    fun yourDaySentence(reading: Reading, lastHour: Int): String {
        if (reading.hotHours.isEmpty()) return SyncCopy.text("your_day.nothing_hot")
        val count = reading.hotHours.size
        val hours = SyncFormat.capitalized(SyncFormat.words(count)) + if (count == 1) " hour" else " hours"
        val from = SyncFormat.spokenTime(reading.hotHours.first())
        return if (reading.backHour != null) {
            SyncCopy.text(
                "your_day.hot_and_back",
                "hours" to hours,
                "from" to from,
                "back" to SyncFormat.spokenTime(reading.backHour, 30),
            )
        } else {
            SyncCopy.text(
                "your_day.hot_not_back",
                "hours" to hours,
                "from" to from,
                "back" to SyncFormat.spokenTime(lastHour + 1),
            )
        }
    }

    /** "Ran hot from two; back by half past six." for a record row. */
    fun recordNote(reading: Reading, lastHour: Int): String? {
        if (reading.hotHours.isEmpty()) return null
        val from = SyncFormat.spokenTime(reading.hotHours.first()).removeSuffix(" o'clock")
        return if (reading.backHour != null) {
            SyncCopy.text("record.hot_and_back", "from" to from, "back" to SyncFormat.spokenTime(reading.backHour, 30))
        } else {
            SyncCopy.text("record.hot_not_back", "from" to from, "back" to SyncFormat.spokenTime(lastHour + 1))
        }
    }
}

/**
 * Sunday's four sentences, filled from fixed templates and never generated freely. A slot with no
 * data drops its sentence; the read-back is never padded.
 */
object ReadBack {
    const val GENERATED_AT_HOUR = 18

    data class Input(
        /** Monday to Sunday, labelled by the day the person woke. */
        val week: List<Night>,
        val usualSleep: Double?,
        /** Hot-hour count per day of the week, Monday first. Null when there is no heart rate. */
        val hotHoursByDay: List<Pair<Int, Int>>?,
        val chipCounts: Map<DayChip, Int>,
    )

    /** Monday to Sunday's read-back exists from 18:00 on Sunday. */
    fun isReady(moment: Moment): Boolean =
        SyncDates.weekday(moment.day) == SyncDates.SUNDAY && moment.hour >= GENERATED_AT_HOUR

    /** The Sunday whose read-back the record should currently link to. */
    fun latestSunday(moment: Moment): Int {
        val sunday = SyncDates.sundayOnOrBefore(moment.day)
        return if (sunday == moment.day && moment.hour < GENERATED_AT_HOUR) sunday - 7 else sunday
    }

    fun sentences(input: Input): List<String> =
        listOfNotNull(nightsUnder(input), hottest(input), borrowed(input), chips(input))

    private fun nightsUnder(input: Input): String? {
        val usual = input.usualSleep ?: return null
        val worn = input.week.mapNotNull { it.sleepMin }
        if (worn.isEmpty()) return null
        val under = worn.count { Usuals.caret(it, usual, Metric.SLEEP) == Caret.DOWN }
        if (under == 0) return null
        val count = SyncFormat.capitalized(SyncFormat.words(under)) + if (under == 1) " night" else " nights"
        return SyncCopy.text("read_back.nights_under", "count" to count)
    }

    private fun hottest(input: Input): String? {
        val days = input.hotHoursByDay ?: return null
        val hottest = days.filter { it.second > 0 }.maxByOrNull { it.second } ?: return null
        val back = days.firstOrNull { it.first > hottest.first && it.second == 0 } ?: return null
        return SyncCopy.text(
            "read_back.hottest",
            "hot" to SyncDates.weekdayName(hottest.first),
            "back" to SyncDates.weekdayName(back.first),
        )
    }

    private fun borrowed(input: Input): String? {
        val usual = input.usualSleep ?: return null
        if (input.week.size < 7) return null
        val minutes = input.week.takeLast(2).mapNotNull { it.sleepMin }
            .sumOf { (it - usual).coerceAtLeast(0.0) }.roundToInt()
        if (minutes <= 0) return null
        return SyncCopy.text("read_back.borrowed", "minutes" to SyncFormat.words(minutes))
    }

    private fun chips(input: Input): String? {
        val counts = input.chipCounts.filterValues { it > 0 }.entries.sortedByDescending { it.value }
        if (counts.isEmpty()) return null
        val parts = counts.map { "*${it.key.word}* ${SyncFormat.times(it.value)}" }
        val joined = if (parts.size == 1) {
            parts.single()
        } else {
            parts.dropLast(1).joinToString(", ") + " and " + parts.last()
        }
        return SyncCopy.text("read_back.chips", "chips" to joined)
    }
}

/** Pattern gates (section 5). All four numbers are proposals to confirm against cohort data. */
object PatternGate {
    const val MIN_WORN_NIGHTS = 21
    const val MIN_EVENING_ENTRIES = 14
    const val MIN_CONDITION_NIGHTS = 5
    const val MIN_EFFECT_SLEEP_MIN = 30
    const val MIN_EFFECT_RHR_BPM = 3

    data class Candidate(
        val id: String,
        val conditionNights: Int,
        val effectSleepMin: Int = 0,
        val effectRhrBpm: Int = 0,
    )

    fun passes(candidate: Candidate, wornNights: Int, eveningEntries: Int): Boolean =
        wornNights >= MIN_WORN_NIGHTS &&
            eveningEntries >= MIN_EVENING_ENTRIES &&
            candidate.conditionNights >= MIN_CONDITION_NIGHTS &&
            (abs(candidate.effectSleepMin) >= MIN_EFFECT_SLEEP_MIN || abs(candidate.effectRhrBpm) >= MIN_EFFECT_RHR_BPM)

    /** One unanswered pattern at a time, and an answered pattern is never asked again. */
    fun nextToAsk(
        candidates: List<Candidate>,
        answers: List<PatternAnswerRecord>,
        wornNights: Int,
        eveningEntries: Int,
    ): Candidate? {
        val answered = answers.map { it.patternId }.toSet()
        return candidates.firstOrNull { it.id !in answered && passes(it, wornNights, eveningEntries) }
    }

    /** "You marked *work* on four of them, *travel* on one, and nothing on one." */
    fun stretchSentence(stretches: List<Set<DayChip>>): String? {
        if (stretches.isEmpty()) return null
        val counts = DayChip.values().associateWith { chip -> stretches.count { chip in it } }
            .filterValues { it > 0 }.entries.sortedByDescending { it.value }
        val nothing = stretches.count { it.isEmpty() }
        val parts = counts.mapIndexed { index, entry ->
            val of = if (index == 0) " of them" else ""
            "*${entry.key.word}* on ${SyncFormat.words(entry.value)}$of"
        }.toMutableList()
        if (nothing > 0) parts += "nothing on ${SyncFormat.words(nothing)}"
        val joined = when (parts.size) {
            0 -> return null
            1 -> parts.single()
            else -> parts.dropLast(1).joinToString(", ") + ", and " + parts.last()
        }
        return SyncCopy.text("read_back.chips", "chips" to joined)
    }
}

/** Experiment comparison (section 5). Before is the 14 worn nights before the start date. */
object ExperimentCalc {
    const val LENGTH_DAYS = 14
    const val REPORT_SLEEP_MIN = 15
    const val REPORT_RHR_BPM = 2

    data class Comparison(val sleepCloserMin: Int?, val rhrCloserBpm: Int?)

    fun dayNumber(startDay: Int, today: Int) = (today - startDay + 1).coerceIn(1, LENGTH_DAYS)

    fun isFinished(startDay: Int, today: Int) = today - startDay >= LENGTH_DAYS

    fun before(nights: List<Night>, startDay: Int) =
        nights.filter { it.day < startDay && it.worn }.takeLast(LENGTH_DAYS)

    fun after(nights: List<Night>, startDay: Int) = nights.filter { it.day >= startDay && it.worn }

    fun compare(before: List<Night>, after: List<Night>, sleepUsual: Double?, rhrUsual: Double?): Comparison {
        fun closer(metric: Metric, usual: Double?): Int? {
            if (usual == null) return null
            val b = before.mapNotNull(metric.read)
            val a = after.mapNotNull(metric.read)
            if (b.isEmpty() || a.isEmpty()) return null
            return (b.map { abs(it - usual) }.average() - a.map { abs(it - usual) }.average()).roundToInt()
        }
        return Comparison(closer(Metric.SLEEP, sleepUsual), closer(Metric.RHR, rhrUsual))
    }

    /** Reports a change only at 15 min or 2 bpm; otherwise it says "hasn't moved yet". */
    fun sentence(comparison: Comparison): String {
        val sleep = comparison.sleepCloserMin
        val rhr = comparison.rhrCloserBpm
        val sleepText = when {
            sleep == null || abs(sleep) < REPORT_SLEEP_MIN -> SyncCopy.text("experiments.sleep_flat")
            sleep > 0 -> SyncCopy.text("experiments.sleep_closer", "n" to sleep)
            else -> SyncCopy.text("experiments.sleep_further", "n" to -sleep)
        }
        val rhrText = when {
            rhr == null || abs(rhr) < REPORT_RHR_BPM -> SyncCopy.text("experiments.rhr_flat")
            rhr > 0 -> SyncCopy.text("experiments.rhr_closer", "n" to rhr)
            else -> SyncCopy.text("experiments.rhr_further", "n" to -rhr)
        }
        return "$sleepText $rhrText"
    }
}
