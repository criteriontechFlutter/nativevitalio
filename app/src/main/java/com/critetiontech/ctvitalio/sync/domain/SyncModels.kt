package com.critetiontech.ctvitalio.sync.domain

/**
 * One night from the ring. [day] is the epoch day of the evening the night began, so the
 * night of Thursday 5 November has the day of 5 November and is "last night" on the 6th.
 *
 * Any field can be null: rings come off at night, and the spec treats that as the normal path.
 *
 * [bedMin] counts minutes from the noon before bed, so 23:40 (700) and 00:10 (730) compare correctly.
 */
data class Night(
    val day: Int,
    val sleepMin: Int? = null,
    val bedMin: Int? = null,
    val rhr: Int? = null,
    val hrv: Int? = null,
    val recovery: Int? = null,
    val spo2: Int? = null,
    val steps: Int? = null,
) {
    /** The state section only reads a night when both sleep and resting heart rate landed. */
    val worn: Boolean get() = sleepMin != null && rhr != null

    /** Minute of the day the person got up, derived from bed and sleep. */
    val wakeMinuteOfDay: Int?
        get() = if (bedMin != null && sleepMin != null) (bedMin + sleepMin + 720) % 1440 else null

    /** Minute of the day the person went to bed. */
    val bedMinuteOfDay: Int? get() = bedMin?.let { (it + 720) % 1440 }
}

/**
 * Night metrics with the proposed caret tolerances from section 5. Recovery, SpO2 and steps have
 * no tolerance in the spec, so any real difference earns a caret, which matches the canvas.
 */
enum class Metric(val tolerance: Int, val read: (Night) -> Int?) {
    SLEEP(5, Night::sleepMin),
    RHR(1, Night::rhr),
    HRV(2, Night::hrv),
    BEDTIME(10, Night::bedMin),
    RECOVERY(1, Night::recovery),
    SPO2(1, Night::spo2),
    STEPS(1, Night::steps),
}

enum class Caret { UP, DOWN, NONE }

/** The person's usual for one metric and the band around it. */
data class Band(val usual: Double, val low: Double, val high: Double) {
    fun contains(value: Int) = value >= low && value <= high

    /** Distance from the usual where the edge of the band is roughly 1. Used only for drawing. */
    fun normalized(value: Int): Float {
        val half = ((high - low) / 2.0).coerceAtLeast(1.0)
        return ((value - usual) / half).toFloat()
    }
}

/** The five states on the arc. Quiet and Low wear are [Hold]s on top of one of these. */
enum class SyncState { BASELINING, IN_SYNC, DRIFTING, OUT_OF_SYNC, RECOVERING }

/** Holds on the current state: nothing resets while one is in place. */
enum class Hold { QUIET, LOW_WEAR }

enum class DayAnswer(val word: String) { FINE("fine"), HEAVY("heavy"), ROUGH("rough") }

enum class DayChip(val word: String) { WORK("work"), HOME("home"), TRAVEL("travel"), UNWELL("unwell") }

/** The person's evening entry. Written locally first so it shows at once, offline included. */
data class EveningEntry(
    val day: Int,
    val answer: DayAnswer,
    val chips: Set<DayChip> = emptySet(),
    val line: String? = null,
)

/** Mean heart rate for a clock hour. */
data class HourMean(val hour: Int, val bpm: Int)

/** What is allowed to leave the device for a calendar event: never the title. */
data class ServerCalendarEvent(val id: String, val startMin: Int, val endMin: Int, val attendees: Int)

/**
 * A calendar event as read on the device. The title stays here: it is rendered from this object
 * and has no path to the server. [toString] is redacted so it cannot reach a log or crash report.
 */
class DeviceCalendarEvent(
    val id: String,
    val startMin: Int,
    val endMin: Int,
    val attendees: Int,
    val title: String,
) {
    fun toServer() = ServerCalendarEvent(id, startMin, endMin, attendees)
    override fun toString() = "DeviceCalendarEvent(id=$id, startMin=$startMin, endMin=$endMin, attendees=$attendees)"
}

/** A mark stores the event id and when it was made, never the title. */
data class EventMark(val eventId: String, val markedAtMillis: Long)

enum class PatternAnswer(val word: String) { TRUE("true"), NOT_REALLY("not really") }

/** A stored answer. "not really" carries the same weight as "true". */
data class PatternAnswerRecord(val patternId: String, val answer: PatternAnswer, val day: Int)

/** An experiment the person picked. [endDay] is set when it finishes or is stopped. */
data class ExperimentRecord(
    val templateId: String,
    val startDay: Int,
    val endDay: Int? = null,
    val stopped: Boolean = false,
)
