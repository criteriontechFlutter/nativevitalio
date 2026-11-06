package com.critetiontech.ctvitalio.sync.domain

/**
 * The arc from section 3, replayed one night at a time because transitions are evaluated once a
 * day after the night's data lands, never intraday.
 *
 *   Baselining -> In sync (night 7)
 *   In sync -> Drifting (5 of 7 nights outside the band)
 *   Drifting -> Out of sync (both metrics out on 10 of 14 nights)
 *   Drifting or Out of sync -> Recovering (7 nights back inside)
 *   Recovering -> In sync (a fortnight inside; the spec marks this as a proposal)
 *
 * The diagram has no way out of Recovering except In sync, so a relapse stays in Recovering. That
 * is deliberately faithful to the spec and is raised as an open question in SYNC_NOTES.md.
 */
object SyncStateMachine {
    const val BASELINE_NIGHTS = 7
    const val DRIFT_WINDOW = 7
    const val DRIFT_NIGHTS_OUTSIDE = 5
    const val OUT_OF_SYNC_WINDOW = 14
    const val OUT_OF_SYNC_NIGHTS = 10
    const val NIGHTS_BACK_INSIDE = 7
    const val FORTNIGHT_INSIDE = 14

    /** Not fixed by the spec: fewer than half of the last fortnight's nights with sleep. */
    const val LOW_WEAR_BELOW_SLEEP_NIGHTS = 7

    /** "1 to 3 days show nothing new", so Quiet appears from the fourth suppressed day. */
    const val QUIET_FROM_SUPPRESSED_DAYS = 4

    data class Evaluation(
        val state: SyncState,
        val hold: Hold?,
        /** First night outside the band in the run that led to Drifting. */
        val driftSinceDay: Int?,
        /** First night of the run back inside the band. */
        val backSinceDay: Int?,
    )

    fun replay(nights: List<Night>, suppressedDays: Set<Int> = emptySet()): Evaluation {
        var state = SyncState.BASELINING
        var worn = 0
        var driftSince: Int? = null
        var backSince: Int? = null
        val inside = nights.indices.map { NightReading.inside(nights, it) }

        for (i in nights.indices) {
            if (nights[i].worn) worn++
            if (state == SyncState.BASELINING) {
                if (worn >= BASELINE_NIGHTS) state = SyncState.IN_SYNC
                continue
            }
            // A stretch the app cannot read holds the last state. Nothing resets.
            if (nights[i].day in suppressedDays || inside[i] == null) continue

            val insideRun = insideRun(inside, i)
            when (state) {
                SyncState.IN_SYNC -> {
                    val recent = (i - DRIFT_WINDOW + 1).coerceAtLeast(0)..i
                    val outside = recent.filter { inside[it] == false }
                    if (outside.size >= DRIFT_NIGHTS_OUTSIDE) {
                        state = SyncState.DRIFTING
                        driftSince = nights[outside.first()].day
                    }
                }
                SyncState.DRIFTING, SyncState.OUT_OF_SYNC -> {
                    if (insideRun >= NIGHTS_BACK_INSIDE) {
                        state = SyncState.RECOVERING
                        backSince = runStartDay(nights, inside, i)
                    } else if (state == SyncState.DRIFTING) {
                        val recent = (i - OUT_OF_SYNC_WINDOW + 1).coerceAtLeast(0)..i
                        if (recent.count { NightReading.bothOutside(nights, it) } >= OUT_OF_SYNC_NIGHTS) {
                            state = SyncState.OUT_OF_SYNC
                        }
                    }
                }
                SyncState.RECOVERING -> {
                    if (insideRun >= FORTNIGHT_INSIDE) {
                        state = SyncState.IN_SYNC
                        driftSince = null
                        backSince = null
                    }
                }
                SyncState.BASELINING -> Unit
            }
        }
        return Evaluation(state, holdFor(state, nights, suppressedDays), driftSince, backSince)
    }

    private fun insideRun(inside: List<Boolean?>, end: Int): Int {
        var run = 0
        for (i in end downTo 0) {
            when (inside[i]) {
                true -> run++
                false -> return run
                null -> Unit // An unworn night neither breaks nor extends the run.
            }
        }
        return run
    }

    private fun runStartDay(nights: List<Night>, inside: List<Boolean?>, end: Int): Int {
        var start = nights[end].day
        for (i in end downTo 0) {
            when (inside[i]) {
                true -> start = nights[i].day
                false -> return start
                null -> Unit
            }
        }
        return start
    }

    private fun holdFor(state: SyncState, nights: List<Night>, suppressedDays: Set<Int>): Hold? {
        if (state == SyncState.BASELINING || nights.size < NightReading.FORTNIGHT) return null
        val sleepNights = nights.takeLast(NightReading.FORTNIGHT).count { it.sleepMin != null }
        if (sleepNights < LOW_WEAR_BELOW_SLEEP_NIGHTS) return Hold.LOW_WEAR

        var suppressedRun = 0
        for (night in nights.asReversed()) {
            if (night.day !in suppressedDays) break
            suppressedRun++
        }
        return if (suppressedRun >= QUIET_FROM_SUPPRESSED_DAYS) Hold.QUIET else null
    }
}

/**
 * Nudge budget from section 5: at most 1 in any 7 days, 3 in any 28, none while Quiet. A nudge is
 * the current lead sentence verbatim. Push delivery itself is not built yet; this is the rule it
 * has to pass through.
 */
object NudgePolicy {
    fun allowed(sentDays: List<Int>, today: Int, hold: Hold?): Boolean {
        if (hold == Hold.QUIET) return false
        val inLastWeek = sentDays.count { today - it in 0..6 }
        val inLastFourWeeks = sentDays.count { today - it in 0..27 }
        return inLastWeek < 1 && inLastFourWeeks < 3
    }
}
