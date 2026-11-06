package com.critetiontech.ctvitalio.sync.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.critetiontech.ctvitalio.sync.data.SyncFixtures
import com.critetiontech.ctvitalio.sync.data.SyncFixtures.Fixture
import com.critetiontech.ctvitalio.sync.data.SyncMetrics
import com.critetiontech.ctvitalio.sync.data.SyncStore
import com.critetiontech.ctvitalio.sync.domain.DayAnswer
import com.critetiontech.ctvitalio.sync.domain.DayChip
import com.critetiontech.ctvitalio.sync.domain.DeviceState
import com.critetiontech.ctvitalio.sync.domain.EveningEntry
import com.critetiontech.ctvitalio.sync.domain.ExperimentCalc
import com.critetiontech.ctvitalio.sync.domain.ExperimentRecord
import com.critetiontech.ctvitalio.sync.domain.Moment
import com.critetiontech.ctvitalio.sync.domain.Night
import com.critetiontech.ctvitalio.sync.domain.PatternAnswer
import com.critetiontech.ctvitalio.sync.domain.PatternAnswerRecord
import com.critetiontech.ctvitalio.sync.domain.StateSection
import com.critetiontech.ctvitalio.sync.domain.StateSectionModel
import com.critetiontech.ctvitalio.sync.domain.SyncDates
import java.util.Calendar

/** What the debug preview panel can pin. A release build always uses the real clock. */
enum class PreviewTime { LIVE, MORNING, EVENING, SUNDAY_EVENING }

/** A frozen view of everything Home and its screens render. */
data class SyncSnapshot(
    val moment: Moment,
    val fixture: Fixture,
    val nights: List<Night>,
    val state: StateSectionModel,
    val device: DeviceState,
    val entries: List<EveningEntry>,
    val marks: Set<String>,
    val patternAnswers: List<PatternAnswerRecord>,
    val experiments: List<ExperimentRecord>,
    val trainingWithdrawn: Boolean,
    val recordDeleted: Boolean,
) {
    val runningExperiment: ExperimentRecord? get() = experiments.firstOrNull { it.endDay == null }
    val todaysEntry: EveningEntry? get() = entries.firstOrNull { it.day == moment.logicalDay }
    val lastNight: Night? get() = nights.lastOrNull()
}

class SyncViewModel(application: Application) : AndroidViewModel(application) {
    private val store = SyncStore(application)
    private val metrics = SyncMetrics(application)

    private var fixture = Fixture.IN_SYNC
    private var previewTime = PreviewTime.LIVE

    private val _snapshot = MutableLiveData<SyncSnapshot>()
    val snapshot: LiveData<SyncSnapshot> = _snapshot

    init {
        seedIfNeeded(now().day)
        publish()
    }

    fun logHomeOpen() = metrics.logHomeOpen(now().hour)

    private fun now(): Moment {
        val c = Calendar.getInstance()
        val day = SyncDates.epochDayOf(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        return when (previewTime) {
            PreviewTime.LIVE -> Moment(day, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
            PreviewTime.MORNING -> Moment(day, 9)
            PreviewTime.EVENING -> Moment(day, 20)
            PreviewTime.SUNDAY_EVENING -> Moment(SyncDates.sundayOnOrBefore(day), 20)
        }
    }

    private fun seedIfNeeded(today: Int) {
        if (store.seededOnDay != null) return
        store.seededOnDay = today
        store.entries = SyncFixtures.seededEntries(fixture, today)
        store.markedEventIds = SyncFixtures.seededMarks()
        store.patternAnswers = SyncFixtures.seededPatternAnswers(today)
        store.experiments = listOf(SyncFixtures.seededExperiment(today))
    }

    /** Re-reads the clock too, so coming back to the app at 17:00 moves Home to its evening layout. */
    fun refresh() = publish()

    private fun publish() {
        val moment = now()
        val today = moment.day
        val nights = SyncFixtures.nights(fixture, today)
        finishExpiredExperiment(today)
        _snapshot.value = SyncSnapshot(
            moment = moment,
            fixture = fixture,
            nights = nights,
            state = StateSection.build(nights, SyncFixtures.suppressedDays(fixture, today)),
            device = deviceState(nights),
            entries = store.entries.sortedByDescending { it.day },
            marks = store.markedEventIds,
            patternAnswers = store.patternAnswers,
            experiments = store.experiments,
            trainingWithdrawn = store.trainingWithdrawn,
            recordDeleted = store.recordDeleted,
        )
    }

    /** A fact about the ring, read directly from the nights. It never consults the state. */
    private fun deviceState(nights: List<Night>): DeviceState {
        if (nights.size < 14) return DeviceState.OK
        val sleepNights = nights.takeLast(14).count { it.sleepMin != null }
        return if (sleepNights < 7) DeviceState.NOT_WORN else DeviceState.OK
    }

    private fun finishExpiredExperiment(today: Int) {
        val all = store.experiments
        val running = all.firstOrNull { it.endDay == null } ?: return
        if (ExperimentCalc.isFinished(running.startDay, today)) {
            store.experiments = all.map {
                if (it == running) it.copy(endDay = running.startDay + ExperimentCalc.LENGTH_DAYS) else it
            }
        }
    }

    // The evening entry. Written first and published straight after, so it is on screen at once.

    fun answerToday(answer: DayAnswer, pendingChips: Set<DayChip>) {
        val day = now().logicalDay
        val existing = store.entries.firstOrNull { it.day == day }
        if (existing == null) metrics.log(SyncMetrics.Event.EVENING_ENTRY_ANSWERED)
        val entry = existing?.copy(answer = answer) ?: EveningEntry(day, answer, pendingChips)
        store.entries = store.entries.filterNot { it.day == day } + entry
        publish()
    }

    fun toggleTodayChip(chip: DayChip) {
        val day = now().logicalDay
        val existing = store.entries.firstOrNull { it.day == day } ?: return
        val chips = if (chip in existing.chips) existing.chips - chip else existing.chips + chip
        store.entries = store.entries.filterNot { it.day == day } + existing.copy(chips = chips)
        publish()
    }

    fun setTodayLine(line: String) {
        val day = now().logicalDay
        val existing = store.entries.firstOrNull { it.day == day } ?: return
        val value = line.trim().ifEmpty { null }
        if (existing.line == value) return
        store.entries = store.entries.filterNot { it.day == day } + existing.copy(line = value)
        publish()
    }

    // Your day.

    fun toggleMark(eventId: String) {
        val marks = store.markedEventIds
        store.markedEventIds = if (eventId in marks) marks - eventId else marks + eventId
        publish()
    }

    // Patterns. Both answers are kept with the same weight.

    fun answerPattern(patternId: String, answer: PatternAnswer) {
        if (store.patternAnswers.any { it.patternId == patternId }) return
        store.patternAnswers = listOf(PatternAnswerRecord(patternId, answer, now().logicalDay)) + store.patternAnswers
        metrics.log(SyncMetrics.Event.PATTERN_ANSWERED)
        publish()
    }

    // Experiments. One at a time; stopping is one tap and keeps the record.

    fun startExperiment(templateId: String) {
        if (store.experiments.any { it.endDay == null }) return
        store.experiments = listOf(ExperimentRecord(templateId, now().day)) + store.experiments
        metrics.log(SyncMetrics.Event.EXPERIMENT_STARTED)
        publish()
    }

    fun stopRunningExperiment() {
        val running = store.experiments.firstOrNull { it.endDay == null } ?: return
        val today = now().day
        store.experiments = store.experiments.map {
            if (it == running) it.copy(endDay = today, stopped = true) else it
        }
        metrics.log(SyncMetrics.Event.EXPERIMENT_STOPPED)
        publish()
    }

    fun logReadBackOpened() = metrics.log(SyncMetrics.Event.READ_BACK_OPENED)

    // Account.

    fun deleteRecord() {
        store.deleteRecord()
        publish()
    }

    fun withdrawTraining() {
        store.trainingWithdrawn = true
        publish()
    }

    // Debug preview only.

    fun setPreview(newFixture: Fixture) {
        fixture = newFixture
        reseed()
    }

    fun setPreview(time: PreviewTime) {
        previewTime = time
        publish()
    }

    fun resetDummyData() = reseed()

    private fun reseed() {
        store.clearAll()
        seedIfNeeded(now().day)
        publish()
    }
}
