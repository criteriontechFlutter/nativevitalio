package com.critetiontech.ctvitalio.sync.data

import com.critetiontech.ctvitalio.sync.domain.DayAnswer
import com.critetiontech.ctvitalio.sync.domain.DayChip
import com.critetiontech.ctvitalio.sync.domain.DeviceCalendarEvent
import com.critetiontech.ctvitalio.sync.domain.EveningEntry
import com.critetiontech.ctvitalio.sync.domain.ExperimentRecord
import com.critetiontech.ctvitalio.sync.domain.HourMean
import com.critetiontech.ctvitalio.sync.domain.Night
import com.critetiontech.ctvitalio.sync.domain.PatternAnswer
import com.critetiontech.ctvitalio.sync.domain.PatternAnswerRecord
import com.critetiontech.ctvitalio.sync.domain.PatternGate
import com.critetiontech.ctvitalio.sync.domain.SyncDates

/**
 * Dummy data for the seven states. These are nights, not canned sentences: the state and every
 * string on screen are computed from them by the real domain code, which is what makes the
 * fixtures a test of the rules as well as a way to see the screens.
 *
 * Every date is relative to today, so the numbers stay coherent whenever the app is opened.
 */
object SyncFixtures {
    enum class Fixture { BASELINING, IN_SYNC, DRIFTING, OUT_OF_SYNC, RECOVERING, QUIET, LOW_WEAR }

    const val USUAL_SLEEP = 400 // 6h 40m
    const val USUAL_RHR = 57
    const val USUAL_HRV = 44
    const val USUAL_BED = 700 // 23:40
    const val USUAL_RECOVERY = 68
    const val USUAL_SPO2 = 97
    const val USUAL_STEPS = 7200

    private const val HISTORY_NIGHTS = 48

    /** Rising toward last night and averaging 4 bpm over, so the dots slide away as on the States sheet. */
    private val DRIFT_RHR = intArrayOf(5, 5, 4, 4, 4, 4, 4, 3, 3)

    private val SLEEP_CYCLE = intArrayOf(-12, -6, 0, 0, 6, 12)
    private val RHR_CYCLE = intArrayOf(-2, -1, 0, 0, 1, 2)
    private val HRV_CYCLE = intArrayOf(-4, -2, 0, 0, 2, 4)
    private val RECOVERY_CYCLE = intArrayOf(-4, -2, 0, 0, 2, 4)
    private val STEPS_CYCLE = intArrayOf(-600, -300, 0, 0, 300, 600)
    private val BED_CYCLE = intArrayOf(-15, -5, 0, 0, 5, 15)

    /** A night that sits inside every band. [back] 1 is last night. */
    private fun steadyNight(today: Int, back: Int): Night {
        val i = Math.floorMod(today - back, SLEEP_CYCLE.size)
        return Night(
            day = today - back,
            sleepMin = USUAL_SLEEP + SLEEP_CYCLE[i],
            bedMin = USUAL_BED + BED_CYCLE[i],
            rhr = USUAL_RHR + RHR_CYCLE[i],
            hrv = USUAL_HRV + HRV_CYCLE[i],
            recovery = USUAL_RECOVERY + RECOVERY_CYCLE[i],
            spo2 = USUAL_SPO2,
            steps = USUAL_STEPS + STEPS_CYCLE[i],
        )
    }

    /** Quiet-looking values for the judged fortnight, inside the band but not on the line. */
    private fun insideNight(today: Int, back: Int): Night {
        val wobble = intArrayOf(-3, 2, -1, 4, -4, 1, 3, -2, 0)[back % 9]
        return steadyNight(today, back).copy(
            sleepMin = USUAL_SLEEP + wobble,
            rhr = USUAL_RHR + wobble.coerceIn(-1, 1),
        )
    }

    private fun history(today: Int, oldestBack: Int = HISTORY_NIGHTS, judged: (Int) -> Night?): List<Night> =
        (oldestBack downTo 1).map { back ->
            judged(back) ?: steadyNight(today, back)
        }

    /** The canvas's last night: 6h 45m, 23:40 to 06:25, RHR 58, HRV 42, recovery 71, SpO2 97, 6,410 steps. */
    private fun canvasLastNight(today: Int) = Night(
        day = today - 1,
        sleepMin = 405,
        bedMin = 700,
        rhr = 58,
        hrv = 42,
        recovery = 71,
        spo2 = 97,
        steps = 6410,
    )

    fun nights(fixture: Fixture, today: Int): List<Night> = when (fixture) {
        Fixture.BASELINING -> (4 downTo 1).map { insideNight(today, it) }

        // 11 of 14 inside, and two nights ago is the 6h 50m the record row quotes.
        Fixture.IN_SYNC -> history(today) { back ->
            when {
                back == 1 -> canvasLastNight(today)
                back == 2 -> insideNight(today, back).copy(sleepMin = 410)
                back == 8 || back == 11 -> insideNight(today, back).copy(sleepMin = 385)
                back <= 14 -> insideNight(today, back)
                else -> null
            }
        }

        // Nine nights with resting heart rate 4 bpm over; sleep still inside.
        Fixture.DRIFTING -> history(today) { back ->
            when {
                back <= 9 -> insideNight(today, back).copy(rhr = USUAL_RHR + DRIFT_RHR[back - 1])
                back <= 14 -> insideNight(today, back)
                else -> null
            }
        }

        // Nineteen nights outside, which reads as about three weeks. Any longer and the run starts
        // moving its own usual (see SYNC_NOTES.md). For the last ten nights sleep is short as well.
        Fixture.OUT_OF_SYNC -> history(today) { back ->
            when {
                back <= 10 -> insideNight(today, back).copy(sleepMin = USUAL_SLEEP - 40, rhr = USUAL_RHR + 5)
                back <= 19 -> insideNight(today, back).copy(rhr = USUAL_RHR + 5)
                else -> null
            }
        }

        // Back inside since a weekend at least seven nights ago, and not yet a full fortnight.
        Fixture.RECOVERING -> {
            val inside = nightsBackToWeekend(today)
            history(today) { back ->
                when {
                    back <= inside -> insideNight(today, back).copy(
                        sleepMin = USUAL_SLEEP - 1 - back % 3,
                        rhr = USUAL_RHR,
                    )
                    back <= inside + 15 -> insideNight(today, back).copy(
                        // Eases toward the usual as it nears the return, so the dots come back.
                        sleepMin = USUAL_SLEEP - 12 - (back - inside) * 3,
                        rhr = USUAL_RHR + 4,
                    )
                    else -> null
                }
            }
        }

        // A week of travel: heart rate is unreadable, and two nights the ring stayed off.
        Fixture.QUIET -> history(today) { back ->
            when {
                back == 3 || back == 5 -> Night(day = today - back)
                back <= 14 -> insideNight(today, back)
                else -> null
            }
        }

        // Sleep on 3 nights of the last 14, resting heart rate on 6.
        Fixture.LOW_WEAR -> history(today) { back ->
            when (back) {
                2, 6, 12 -> insideNight(today, back)
                4, 9, 13 -> Night(day = today - back, rhr = USUAL_RHR)
                in 1..14 -> Night(day = today - back)
                else -> null
            }
        }
    }

    /** Nights since the most recent Saturday that is at least seven nights ago. */
    private fun nightsBackToWeekend(today: Int): Int =
        (7..13).first { SyncDates.weekday(today - it) == SyncDates.SATURDAY }

    /** Days the app cannot read, from the person's own travel and unwell chips. */
    fun suppressedDays(fixture: Fixture, today: Int): Set<Int> =
        if (fixture == Fixture.QUIET) (1..5).map { today - it }.toSet() else emptySet()

    /** Evening entries already in the record. Wednesday and Thursday's match the canvas. */
    fun seededEntries(fixture: Fixture, today: Int): List<EveningEntry> = when (fixture) {
        Fixture.BASELINING -> emptyList()
        Fixture.QUIET -> (1..5).map { EveningEntry(today - it, DayAnswer.HEAVY, setOf(DayChip.TRAVEL)) }
        else -> listOf(
            EveningEntry(today - 1, DayAnswer.HEAVY, setOf(DayChip.WORK)),
            EveningEntry(today - 2, DayAnswer.FINE),
        )
    }

    /** Hour means for a day. Fridays and Thursdays run hot in the afternoon, as on the canvas. */
    fun hours(day: Int, lastHourExclusive: Int = 19): List<HourMean> {
        val weekday = SyncDates.weekday(day)
        val hot = weekday == 4 || weekday == 5
        val offsets = if (hot) {
            intArrayOf(1, 2, 0, 3, 1, 0, 9, 8, 5, 2, 0)
        } else {
            intArrayOf(1, 0, 1, -1, 0, 1, 2, 1, 0, 1, 0)
        }
        return offsets.toList().mapIndexedNotNull { i, offset ->
            val hour = 8 + i
            if (hour < lastHourExclusive) HourMean(hour, HOUR_USUAL + offset) else null
        }
    }

    const val HOUR_USUAL = 72

    /** 28 days whose median is [HOUR_USUAL] and whose band is one beat either side. */
    fun hourHistory(): List<List<HourMean>> {
        val cycle = intArrayOf(-2, -1, 0, 0, 1, 2)
        return (0 until 28).map { d ->
            (8..18).map { hour -> HourMean(hour, HOUR_USUAL + cycle[d % cycle.size]) }
        }
    }

    /** Dummy calendar standing in for the device calendar. Titles stay in this object. */
    fun calendar(): List<DeviceCalendarEvent> = listOf(
        DeviceCalendarEvent("evt-planning", 9 * 60 + 30, 10 * 60 + 30, 6, "Weekly planning"),
        DeviceCalendarEvent("evt-vendor", 11 * 60 + 30, 12 * 60 + 30, 3, "Vendor call"),
        DeviceCalendarEvent("evt-review", 14 * 60, 15 * 60 + 30, 8, "Q4 planning review"),
        DeviceCalendarEvent("evt-one-to-one", 16 * 60, 17 * 60, 2, "1:1"),
    )

    /** The evt-review mark on the canvas. */
    fun seededMarks(): Set<String> = setOf("evt-review")

    /** The week on the canvas: five nights under the usual, seventy minutes borrowed at the weekend. */
    fun readBackWeek(monday: Int): List<Night> {
        val sleep = intArrayOf(340, 335, 350, 315, 345, 435, 435)
        return sleep.mapIndexed { i, minutes ->
            Night(
                day = monday + i,
                sleepMin = minutes,
                bedMin = 730,
                rhr = 60,
                hrv = 39,
                recovery = USUAL_RECOVERY - 3,
                spo2 = USUAL_SPO2,
                steps = USUAL_STEPS,
            )
        }
    }

    fun readBackHotHours(monday: Int): List<Pair<Int, Int>> = listOf(
        monday to 0,
        monday + 1 to 1,
        monday + 2 to 0,
        monday + 3 to 3,
        monday + 4 to 1,
        monday + 5 to 0,
        monday + 6 to 0,
    )

    fun readBackChips(): Map<DayChip, Int> = mapOf(DayChip.WORK to 2)

    val EXPERIMENT_TEMPLATES = listOf(
        "finish_by_seven" to "experiment.finish_by_seven",
        "no_screens" to "experiment.no_screens",
        "morning_walk" to "experiment.morning_walk",
        "lights_out" to "experiment.lights_out",
        "lunch_away" to "experiment.lunch_away",
    )

    /** The canvas's running experiment, on day 9 of 14. */
    fun seededExperiment(today: Int) = ExperimentRecord("finish_by_seven", today - 8)

    /** Before sits well under the usual; after climbs back toward it. */
    fun experimentNights(startDay: Int, today: Int): List<Night> {
        val before = intArrayOf(-55, -60, -50, -62, -58, -48, -60, -56, -50, -64, -52, -60, -54, -50)
        val after = intArrayOf(-42, -38, -40, -30, -32, -22, -24, -17, -8, -6, -4, -2, 0, 2)
        val beforeNights = before.mapIndexed { i, delta ->
            experimentNight(startDay - before.size + i, delta)
        }
        val afterNights = (0 until (today - startDay).coerceIn(0, after.size)).map { i ->
            experimentNight(startDay + i, after[i])
        }
        return beforeNights + afterNights
    }

    private fun experimentNight(day: Int, sleepDelta: Int) = Night(
        day = day,
        sleepMin = USUAL_SLEEP + sleepDelta,
        rhr = USUAL_RHR + 2,
    )

    val PATTERN_KEYS = mapOf(
        "meetings_sleep" to "pattern.meetings_sleep",
        "chip_leads_body" to "pattern.chip_leads_body",
        "weekday_hot" to "pattern.weekday_hot",
        "weekend_gives_back" to "pattern.weekend_gives_back",
    )

    fun patternCandidates() = listOf(
        PatternGate.Candidate("chip_leads_body", conditionNights = 9, effectRhrBpm = 3),
        PatternGate.Candidate("weekday_hot", conditionNights = 6, effectRhrBpm = 9),
        PatternGate.Candidate("weekend_gives_back", conditionNights = 10, effectSleepMin = 60),
        PatternGate.Candidate("meetings_sleep", conditionNights = 8, effectSleepMin = -40),
    )

    /** Three already answered, newest first as drawn; "not really" is kept like any other answer. */
    fun seededPatternAnswers(today: Int) = listOf(
        PatternAnswerRecord("chip_leads_body", PatternAnswer.TRUE, today - 11),
        PatternAnswerRecord("weekday_hot", PatternAnswer.NOT_REALLY, today - 18),
        PatternAnswerRecord("weekend_gives_back", PatternAnswer.TRUE, today - 25),
    )

    const val PATTERN_EVENING_ENTRIES = 24

    fun heavierStretches(): List<Set<DayChip>> = listOf(
        setOf(DayChip.WORK),
        setOf(DayChip.WORK),
        setOf(DayChip.WORK),
        setOf(DayChip.WORK),
        setOf(DayChip.TRAVEL),
        emptySet(),
    )
}
