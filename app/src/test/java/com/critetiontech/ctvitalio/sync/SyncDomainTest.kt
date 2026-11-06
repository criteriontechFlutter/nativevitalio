package com.critetiontech.ctvitalio.sync

import com.critetiontech.ctvitalio.sync.data.SyncFixtures
import com.critetiontech.ctvitalio.sync.data.SyncFixtures.Fixture
import com.critetiontech.ctvitalio.sync.domain.Caret
import com.critetiontech.ctvitalio.sync.domain.DayChip
import com.critetiontech.ctvitalio.sync.domain.DeviceState
import com.critetiontech.ctvitalio.sync.domain.ExperimentCalc
import com.critetiontech.ctvitalio.sync.domain.Hold
import com.critetiontech.ctvitalio.sync.domain.HomeLayout
import com.critetiontech.ctvitalio.sync.domain.HomeSection
import com.critetiontech.ctvitalio.sync.domain.HotHours
import com.critetiontech.ctvitalio.sync.domain.Metric
import com.critetiontech.ctvitalio.sync.domain.Moment
import com.critetiontech.ctvitalio.sync.domain.Night
import com.critetiontech.ctvitalio.sync.domain.NightReading
import com.critetiontech.ctvitalio.sync.domain.NudgePolicy
import com.critetiontech.ctvitalio.sync.domain.PatternAnswer
import com.critetiontech.ctvitalio.sync.domain.PatternAnswerRecord
import com.critetiontech.ctvitalio.sync.domain.PatternGate
import com.critetiontech.ctvitalio.sync.domain.ReadBack
import com.critetiontech.ctvitalio.sync.domain.StateSection
import com.critetiontech.ctvitalio.sync.domain.SyncDates
import com.critetiontech.ctvitalio.sync.domain.SyncFormat
import com.critetiontech.ctvitalio.sync.domain.SyncState
import com.critetiontech.ctvitalio.sync.domain.SyncStateMachine
import com.critetiontech.ctvitalio.sync.domain.Usuals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncDomainTest {
    /** Friday 6 November 2026, the date on the canvas. */
    private val friday = SyncDates.epochDayOf(2026, 10, 6)

    private fun section(fixture: Fixture, today: Int = friday) =
        StateSection.build(SyncFixtures.nights(fixture, today), SyncFixtures.suppressedDays(fixture, today))

    // Dates

    @Test
    fun dates_match_the_canvas() {
        assertEquals(5, SyncDates.weekday(friday))
        assertEquals("Friday 6 November", SyncDates.longDate(friday))
        assertEquals("23 Oct", SyncDates.shortDate(friday - 14))
        assertEquals(SyncDates.SUNDAY, SyncDates.weekday(SyncDates.sundayOnOrBefore(friday)))
    }

    // The seven states, each computed from nights rather than asserted by a label.

    @Test
    fun baselining_fixture() {
        val model = section(Fixture.BASELINING)
        assertEquals(SyncState.BASELINING, model.state)
        assertEquals("Your numbers are here. The comparisons need a few more nights.", model.lead)
        assertEquals("4 nights in. About 3 more before the comparisons mean anything.", model.reason)
        assertTrue(model.dashedUsual)
        assertEquals("4 nights in", model.eyebrowLabel)
    }

    @Test
    fun in_sync_fixture() {
        val model = section(Fixture.IN_SYNC)
        assertEquals(SyncState.IN_SYNC, model.state)
        assertEquals("You've been in your usual range these two weeks.", model.lead)
        assertEquals("11 of 14 nights sat in your usual range.", model.reason)
        assertEquals("23 Oct – 5 Nov", model.eyebrowLabel)
        assertFalse(model.showDoors)
        assertFalse(model.showAttentionTick)
    }

    @Test
    fun drifting_fixture() {
        val model = section(Fixture.DRIFTING)
        assertEquals(SyncState.DRIFTING, model.state)
        assertEquals("The last two weeks have run heavier than your usual.", model.lead)
        assertEquals("Your resting heart rate has sat 4 bpm above usual for 9 nights.", model.reason)
        assertTrue(model.showDoors)
        assertFalse(model.showAttentionTick)
    }

    @Test
    fun out_of_sync_fixture() {
        val model = section(Fixture.OUT_OF_SYNC)
        assertEquals(SyncState.OUT_OF_SYNC, model.state)
        assertEquals("You've been out of sync for about three weeks.", model.lead)
        assertEquals(
            "Sleep 40 min short, resting heart rate 5 bpm above — both, on 10 of 14 nights.",
            model.reason,
        )
        assertTrue(model.showDoors)
        assertTrue(model.showAttentionTick)
    }

    @Test
    fun recovering_fixture_on_every_weekday() {
        for (offset in 0 until 7) {
            val model = section(Fixture.RECOVERING, friday + offset)
            assertEquals(SyncState.RECOVERING, model.state)
            assertEquals("You've been back in sync since the weekend.", model.lead)
            assertTrue(model.reason, model.reason.startsWith("Sleep came back first"))
            assertFalse(model.showDoors)
        }
    }

    @Test
    fun quiet_fixture_holds_without_resetting() {
        val nights = SyncFixtures.nights(Fixture.QUIET, friday)
        val evaluation = SyncStateMachine.replay(nights, SyncFixtures.suppressedDays(Fixture.QUIET, friday))
        assertEquals(Hold.QUIET, evaluation.hold)
        assertEquals(SyncState.IN_SYNC, evaluation.state)
        val model = section(Fixture.QUIET)
        assertEquals("Nothing worth saying this week.", model.lead)
        assertEquals("Some stretches don't tell you much, and this is one of them.", model.reason)
    }

    @Test
    fun one_to_three_suppressed_days_show_nothing_new() {
        val nights = SyncFixtures.nights(Fixture.IN_SYNC, friday)
        for (days in 1..3) {
            val suppressed = (1..days).map { friday - it }.toSet()
            assertNull(SyncStateMachine.replay(nights, suppressed).hold)
        }
        assertEquals(Hold.QUIET, SyncStateMachine.replay(nights, (1..4).map { friday - it }.toSet()).hold)
    }

    @Test
    fun low_wear_fixture() {
        val model = section(Fixture.LOW_WEAR)
        assertEquals(Hold.LOW_WEAR, model.hold)
        assertEquals("3 nights of the last 14.", model.lead)
        assertEquals("Resting heart rate is there for 6 nights. Sleep isn't.", model.reason)
        assertEquals(3, model.dots.count { it != null })
        assertEquals(14, model.dots.size)
    }

    @Test
    fun attention_tick_appears_on_out_of_sync_only() {
        for (fixture in Fixture.values()) {
            assertEquals(fixture.name, fixture == Fixture.OUT_OF_SYNC, section(fixture).showAttentionTick)
        }
    }

    @Test
    fun usual_arrives_on_night_7_and_not_before() {
        val six = (6 downTo 1).map { Night(day = friday - it, sleepMin = 400, rhr = 57) }
        assertNull(Usuals.band(six, Metric.SLEEP))
        val seven = (7 downTo 1).map { Night(day = friday - it, sleepMin = 400, rhr = 57) }
        assertNotNull(Usuals.band(seven, Metric.SLEEP))
        assertEquals(SyncState.BASELINING, SyncStateMachine.replay(six).state)
        assertEquals(SyncState.IN_SYNC, SyncStateMachine.replay(seven).state)
    }

    @Test
    fun the_judged_fortnight_never_moves_its_own_usual() {
        val steady = SyncFixtures.nights(Fixture.IN_SYNC, friday)
        val shifted = steady.mapIndexed { i, night ->
            if (i >= steady.size - 14) night.copy(sleepMin = 200) else night
        }
        assertEquals(Usuals.band(steady, Metric.SLEEP), Usuals.band(shifted, Metric.SLEEP))
    }

    @Test
    fun usual_matches_the_canvas_numbers() {
        val nights = SyncFixtures.nights(Fixture.IN_SYNC, friday)
        assertEquals(400.0, Usuals.band(nights, Metric.SLEEP)!!.usual, 0.01)
        assertEquals(57.0, Usuals.band(nights, Metric.RHR)!!.usual, 0.01)
        assertEquals(44.0, Usuals.band(nights, Metric.HRV)!!.usual, 0.01)
        assertEquals(68.0, Usuals.band(nights, Metric.RECOVERY)!!.usual, 0.01)
        assertEquals(97.0, Usuals.band(nights, Metric.SPO2)!!.usual, 0.01)
        assertEquals(7200.0, Usuals.band(nights, Metric.STEPS)!!.usual, 0.01)
    }

    @Test
    fun carets_match_the_canvas_and_ignore_small_differences() {
        assertEquals(Caret.UP, Usuals.caret(405, 400.0, Metric.SLEEP))
        assertEquals(Caret.NONE, Usuals.caret(403, 400.0, Metric.SLEEP))
        assertEquals(Caret.UP, Usuals.caret(58, 57.0, Metric.RHR))
        assertEquals(Caret.DOWN, Usuals.caret(42, 44.0, Metric.HRV))
        assertEquals(Caret.NONE, Usuals.caret(97, 97.0, Metric.SPO2))
        assertEquals(Caret.DOWN, Usuals.caret(6410, 7200.0, Metric.STEPS))
        assertEquals(Caret.UP, Usuals.caret(730, 700.0, Metric.BEDTIME))
    }

    @Test
    fun unworn_nights_are_excluded_from_the_range_count() {
        val nights = SyncFixtures.nights(Fixture.QUIET, friday)
        assertEquals(12, NightReading.rangeCount(nights).worn)
    }

    @Test
    fun bedtime_and_wake_read_across_midnight() {
        val night = Night(day = friday, sleepMin = 405, bedMin = 700)
        assertEquals(23 * 60 + 40, night.bedMinuteOfDay)
        assertEquals(6 * 60 + 25, night.wakeMinuteOfDay)
        assertEquals("00:10", SyncFormat.bedtime(730))
    }

    // Layout is a function of time and device only.

    @Test
    fun morning_and_evening_sections_follow_the_spec_order() {
        assertEquals(
            listOf(HomeSection.LAST_TWO_WEEKS, HomeSection.LAST_NIGHT, HomeSection.RECORD, HomeSection.HELP),
            HomeLayout.sections(Moment(friday, 9), DeviceState.OK),
        )
        assertEquals(
            listOf(HomeSection.QUESTION, HomeSection.YOUR_DAY, HomeSection.LAST_TWO_WEEKS, HomeSection.HELP),
            HomeLayout.sections(Moment(friday, 17), DeviceState.OK),
        )
    }

    @Test
    fun the_question_is_gone_after_three_and_stays_until_then() {
        assertTrue(HomeSection.QUESTION in HomeLayout.sections(Moment(friday, 2, 59), DeviceState.OK))
        assertFalse(HomeSection.QUESTION in HomeLayout.sections(Moment(friday, 3), DeviceState.OK))
        assertFalse(HomeSection.QUESTION in HomeLayout.sections(Moment(friday, 16, 59), DeviceState.OK))
    }

    @Test
    fun on_sunday_the_check_in_card_takes_the_question_slot() {
        val sunday = SyncDates.sundayOnOrBefore(friday)
        val sections = HomeLayout.sections(Moment(sunday, 20), DeviceState.OK)
        assertEquals(HomeSection.CHECK_IN, sections.first())
        assertFalse(HomeSection.QUESTION in sections)
        val monday = HomeLayout.sections(Moment(sunday + 1, 20), DeviceState.OK)
        assertFalse(HomeSection.CHECK_IN in monday)
    }

    @Test
    fun ring_section_appears_by_device_state() {
        val moment = Moment(friday, 9)
        assertFalse(HomeSection.RING in HomeLayout.sections(moment, DeviceState.OK))
        for (device in listOf(DeviceState.DISCONNECTED, DeviceState.UNCHARGED, DeviceState.NOT_WORN)) {
            assertTrue(HomeSection.RING in HomeLayout.sections(moment, device))
        }
    }

    /** Snapshot rule from the checklist: in-sync and out-of-sync Home differ only inside the state section. */
    @Test
    fun in_sync_and_out_of_sync_home_have_identical_layout() {
        for (hour in listOf(9, 20)) {
            val moment = Moment(friday, hour)
            val inSync = HomeLayout.sections(moment, deviceFor(Fixture.IN_SYNC))
            val outOfSync = HomeLayout.sections(moment, deviceFor(Fixture.OUT_OF_SYNC))
            assertEquals(inSync, outOfSync)
        }
    }

    private fun deviceFor(fixture: Fixture): DeviceState {
        val sleepNights = SyncFixtures.nights(fixture, friday).takeLast(14).count { it.sleepMin != null }
        return if (sleepNights < 7) DeviceState.NOT_WORN else DeviceState.OK
    }

    @Test
    fun only_you_see_this_shows_on_the_first_three_answers() {
        assertTrue(HomeLayout.showOnlyYouSeeThis(1))
        assertTrue(HomeLayout.showOnlyYouSeeThis(3))
        assertFalse(HomeLayout.showOnlyYouSeeThis(4))
    }

    // Hot hours

    @Test
    fun your_day_sentence_matches_the_canvas() {
        val bands = HotHours.bands(SyncFixtures.hourHistory())
        val reading = HotHours.read(SyncFixtures.hours(friday), bands)
        assertEquals(listOf(14, 15), reading.hotHours)
        assertEquals(18, reading.backHour)
        assertEquals(
            "Two hours ran hot from two o'clock. You got it back by half past six.",
            HotHours.yourDaySentence(reading, 18),
        )
        assertEquals("Ran hot from two; back by half past six.", HotHours.recordNote(reading, 18))
    }

    @Test
    fun a_hot_hour_always_names_when_it_came_back_or_that_it_has_not() {
        val bands = HotHours.bands(SyncFixtures.hourHistory())
        val stillHot = SyncFixtures.hours(friday).filter { it.hour <= 15 }
        val reading = HotHours.read(stillHot, bands)
        assertNull(reading.backHour)
        assertEquals(
            "Two hours ran hot from two o'clock. You hadn't got it back by four o'clock.",
            HotHours.yourDaySentence(reading, 15),
        )
    }

    @Test
    fun a_quiet_day_says_nothing_ran_hot() {
        val bands = HotHours.bands(SyncFixtures.hourHistory())
        val wednesday = friday - 2
        assertEquals(emptyList<Int>(), HotHours.read(SyncFixtures.hours(wednesday), bands).hotHours)
    }

    // Read-back

    private fun readBackInput(): ReadBack.Input {
        val monday = SyncDates.sundayOnOrBefore(friday) - 6
        return ReadBack.Input(
            SyncFixtures.readBackWeek(monday),
            SyncFixtures.USUAL_SLEEP.toDouble(),
            SyncFixtures.readBackHotHours(monday),
            SyncFixtures.readBackChips(),
        )
    }

    @Test
    fun read_back_matches_the_canvas() {
        assertEquals(
            listOf(
                "Five nights under your usual.",
                "Thursday ran hottest; you had it back by Saturday evening.",
                "You borrowed seventy minutes of sleep from the weekend.",
                "You marked *work* twice.",
            ),
            ReadBack.sentences(readBackInput()),
        )
    }

    @Test
    fun read_back_drops_each_sentence_when_its_slot_is_empty() {
        val full = readBackInput()
        assertEquals(3, ReadBack.sentences(full.copy(chipCounts = emptyMap())).size)
        assertEquals(3, ReadBack.sentences(full.copy(hotHoursByDay = null)).size)
        assertEquals(
            listOf("Thursday ran hottest; you had it back by Saturday evening.", "You marked *work* twice."),
            ReadBack.sentences(full.copy(usualSleep = null)),
        )
        assertEquals(
            emptyList<String>(),
            ReadBack.sentences(ReadBack.Input(emptyList(), null, null, emptyMap())),
        )
    }

    @Test
    fun read_back_is_generated_at_six_on_sunday() {
        val sunday = SyncDates.sundayOnOrBefore(friday)
        assertFalse(ReadBack.isReady(Moment(sunday, 17, 59)))
        assertTrue(ReadBack.isReady(Moment(sunday, 18)))
        assertFalse(ReadBack.isReady(Moment(sunday + 1, 18)))
        assertEquals(sunday - 7, ReadBack.latestSunday(Moment(sunday, 17)))
        assertEquals(sunday, ReadBack.latestSunday(Moment(sunday, 18)))
        assertEquals(sunday, ReadBack.latestSunday(Moment(sunday + 3, 9)))
    }

    // Patterns

    private val strong = PatternGate.Candidate("a", conditionNights = 5, effectSleepMin = -30)

    @Test
    fun pattern_gates_hold_at_their_edges() {
        assertTrue(PatternGate.passes(strong, 21, 14))
        assertFalse(PatternGate.passes(strong, 20, 14))
        assertFalse(PatternGate.passes(strong, 21, 13))
        assertFalse(PatternGate.passes(strong.copy(conditionNights = 4), 21, 14))
        assertFalse(PatternGate.passes(strong.copy(effectSleepMin = -29), 21, 14))
        assertTrue(PatternGate.passes(strong.copy(effectSleepMin = 0, effectRhrBpm = 3), 21, 14))
    }

    @Test
    fun an_answered_pattern_is_never_asked_again_whichever_way_it_went() {
        for (answer in PatternAnswer.values()) {
            val answers = listOf(PatternAnswerRecord("a", answer, friday))
            assertNull(PatternGate.nextToAsk(listOf(strong), answers, 31, 24))
        }
        assertEquals("a", PatternGate.nextToAsk(listOf(strong), emptyList(), 31, 24)?.id)
    }

    @Test
    fun stretch_sentence_matches_the_canvas() {
        assertEquals(
            "You marked *work* on four of them, *travel* on one, and nothing on one.",
            PatternGate.stretchSentence(SyncFixtures.heavierStretches()),
        )
        assertEquals(
            "You marked *work* on one of them.",
            PatternGate.stretchSentence(listOf(setOf(DayChip.WORK))),
        )
    }

    // Experiments

    @Test
    fun experiment_sentence_reports_only_past_the_threshold() {
        assertEquals(
            "Sleep is 25 minutes closer to your usual than the fortnight before. Resting heart rate hasn't moved yet.",
            ExperimentCalc.sentence(ExperimentCalc.Comparison(25, 1)),
        )
        assertEquals(
            "Sleep hasn't moved yet. Resting heart rate is 2 bpm closer to your usual than the fortnight before.",
            ExperimentCalc.sentence(ExperimentCalc.Comparison(14, 2)),
        )
        assertEquals(
            "Sleep is 20 minutes further from your usual than the fortnight before. Resting heart rate hasn't moved yet.",
            ExperimentCalc.sentence(ExperimentCalc.Comparison(-20, null)),
        )
    }

    @Test
    fun running_experiment_fixture_is_day_nine_of_fourteen() {
        val start = SyncFixtures.seededExperiment(friday).startDay
        assertEquals(9, ExperimentCalc.dayNumber(start, friday))
        assertFalse(ExperimentCalc.isFinished(start, start + 13))
        assertTrue(ExperimentCalc.isFinished(start, start + 14))
        val nights = SyncFixtures.experimentNights(start, friday)
        val before = ExperimentCalc.before(nights, start)
        val after = ExperimentCalc.after(nights, start)
        assertEquals(14, before.size)
        assertEquals(8, after.size)
        val comparison = ExperimentCalc.compare(
            before,
            after,
            SyncFixtures.USUAL_SLEEP.toDouble(),
            SyncFixtures.USUAL_RHR.toDouble(),
        )
        assertEquals(0, comparison.rhrCloserBpm)
        assertTrue("${comparison.sleepCloserMin}", comparison.sleepCloserMin!! >= 15)
    }

    // Nudges

    @Test
    fun nudge_budget_is_one_in_seven_and_three_in_twenty_eight_and_none_while_quiet() {
        assertTrue(NudgePolicy.allowed(emptyList(), friday, null))
        assertFalse(NudgePolicy.allowed(emptyList(), friday, Hold.QUIET))
        assertFalse(NudgePolicy.allowed(listOf(friday - 6), friday, null))
        assertTrue(NudgePolicy.allowed(listOf(friday - 7), friday, null))
        assertFalse(NudgePolicy.allowed(listOf(friday - 7, friday - 14, friday - 21), friday, null))
        assertTrue(NudgePolicy.allowed(listOf(friday - 14, friday - 21, friday - 28), friday, null))
    }

    // Words

    @Test
    fun numbers_read_as_words() {
        assertEquals("seventy", SyncFormat.words(70))
        assertEquals("twenty-five", SyncFormat.words(25))
        assertEquals("half past six", SyncFormat.spokenTime(18, 30))
        assertEquals("two o'clock", SyncFormat.spokenTime(14))
    }
}
