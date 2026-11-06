package com.critetiontech.ctvitalio.sync.ui

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.sync.data.SyncFixtures
import com.critetiontech.ctvitalio.sync.domain.Band
import com.critetiontech.ctvitalio.sync.domain.DeviceCalendarEvent
import com.critetiontech.ctvitalio.sync.domain.DeviceState
import com.critetiontech.ctvitalio.sync.domain.ExperimentCalc
import com.critetiontech.ctvitalio.sync.domain.ExperimentRecord
import com.critetiontech.ctvitalio.sync.domain.HotHours
import com.critetiontech.ctvitalio.sync.domain.Metric
import com.critetiontech.ctvitalio.sync.domain.Night
import com.critetiontech.ctvitalio.sync.domain.PatternAnswer
import com.critetiontech.ctvitalio.sync.domain.PatternGate
import com.critetiontech.ctvitalio.sync.domain.ReadBack
import com.critetiontech.ctvitalio.sync.domain.StateSection
import com.critetiontech.ctvitalio.sync.domain.SyncDates
import com.critetiontech.ctvitalio.sync.domain.SyncFormat
import com.critetiontech.ctvitalio.sync.domain.Usuals
import com.critetiontech.ctvitalio.sync.ui.SyncUi.Companion.WRAP

/** A check-in card, shared by Home on Sunday and the read-back. */
internal fun SyncUi.checkInCard(onClick: () -> Unit): View = card().apply {
    val inner = row()
    val words = column()
    words.addWithMargins(title(copy("check_in.title"), 16f))
    words.addWithMargins(small(copy("check_in.body"), 13f), top = 4)
    inner.addWithMargins(words, width = 0, weight = 1f)
    inner.addWithMargins(chevronBadge(), start = 10, width = dp(32), height = dp(32))
    addWithMargins(inner)
    pressable(this, onClick = onClick)
}

/** Your day: calendar blocks beside hourly heart rate against the usual band. */
class SyncYourDayFragment : SyncSheetFragment() {
    private val rowHeight = 44
    private val blocks = mutableMapOf<String, Block>()

    /** Marking a block animates in place, so it never rebuilds the screen. */
    override fun rebuildKey(snapshot: SyncSnapshot): Any =
        (super.rebuildKey(snapshot) as SyncSnapshot).copy(marks = emptySet())

    override fun update(snapshot: SyncSnapshot) {
        blocks.forEach { (id, block) -> block.setMarked(id in snapshot.marks) }
    }

    override fun onDestroyView() {
        blocks.clear()
        super.onDestroyView()
    }

    override fun build(body: LinearLayout, snapshot: SyncSnapshot) = with(ui) {
        blocks.clear()
        val moment = snapshot.moment
        val lastHour = moment.lastFinishedHour
        val hours = SyncFixtures.hours(moment.logicalDay, lastHour + 1)
        val bands = HotHours.bands(SyncFixtures.hourHistory())
        val reading = HotHours.read(hours, bands)

        body.addCard(
            card().apply {
                addWithMargins(sectionHeader(copy("your_day.title"), copy("your_day.label")))
                addWithMargins(lead(HotHours.yourDaySentence(reading, lastHour)), top = 8)
            },
        )

        // The calendar is read on the device when this screen opens. Titles are rendered here only.
        val events = SyncFixtures.calendar()
        val lastRow = maxOf(19, events.maxOf { (it.endMin + 59) / 60 })
        val rows = lastRow - FIRST_HOUR
        val heartWidth = dp(104)
        val hourWidth = dp(28)

        val timelineCard = card(padding = 14)
        val columns = row()
        columns.addWithMargins(small(copy("your_day.calendar_column"), 12f), start = 28, width = 0, weight = 1f)
        columns.addWithMargins(small(copy("your_day.heart_column"), 12f), start = 6, width = heartWidth)
        timelineCard.addWithMargins(columns)

        val usualAt = 0.28f
        val captions = FrameLayout(context)
        captions.addView(
            small(copy("drawing.usual"), 12f),
            FrameLayout.LayoutParams(WRAP, WRAP).apply {
                gravity = Gravity.END
                marginEnd = (heartWidth * (1 - usualAt)).toInt() - dp(14)
            },
        )
        captions.addView(
            small(copy("drawing.plus_ten"), 12f),
            FrameLayout.LayoutParams(WRAP, WRAP).apply {
                gravity = Gravity.END
                marginEnd = (heartWidth * (1 - usualAt)).toInt() - dp(44)
            },
        )
        timelineCard.addWithMargins(captions, top = 2)

        val timeline = FrameLayout(context)
        for (hour in FIRST_HOUR..lastRow) {
            val top = dp((hour - FIRST_HOUR) * rowHeight)
            timeline.addView(
                small("%02d".format(hour), 12f),
                FrameLayout.LayoutParams(hourWidth, WRAP).apply { topMargin = top },
            )
            timeline.addView(
                divider(),
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, dp(1).coerceAtLeast(1)).apply {
                    topMargin = top + dp(8)
                    marginStart = hourWidth
                    marginEnd = heartWidth + dp(6)
                },
            )
        }

        events.forEach { event ->
            val block = Block(event)
            blocks[event.id] = block
            timeline.addView(
                block.view,
                FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, WRAP).apply {
                    topMargin = dp(8) + ((event.startMin / 60f - FIRST_HOUR) * dp(rowHeight)).toInt()
                    marginStart = hourWidth
                    marginEnd = heartWidth + dp(12)
                    height = maxOf(dp(44), ((event.endMin - event.startMin) / 60f * dp(rowHeight)).toInt())
                },
            )
        }

        val byHour = hours.associateBy { it.hour }
        val dots = (FIRST_HOUR until lastRow).map { hour ->
            byHour[hour]?.let { (it.bpm - (bands[hour]?.usual ?: it.bpm.toDouble())).toFloat() / BPM_PER_DOT_UNIT }
        }
        val notes = reading.hotHours.associate { hour ->
            val usual = bands[hour]?.usual ?: 0.0
            (hour - FIRST_HOUR) to "${SyncFormat.CARET_UP}${(byHour[hour]!!.bpm - usual).toInt()}"
        }
        timeline.addView(
            SyncDrawingView(context).apply {
                spec = SyncDrawingView.Spec(
                    dots = dots,
                    vertical = true,
                    slots = rows,
                    dotNotes = notes,
                    rowHeightDp = rowHeight.toFloat(),
                    usualAtFraction = usualAt,
                )
                revealOnEntrance(order = 1)
            },
            FrameLayout.LayoutParams(heartWidth, FrameLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.END
                topMargin = dp(8)
            },
        )
        timelineCard.addWithMargins(timeline, top = 4)
        body.addCard(timelineCard)

        body.addCard(
            card().apply {
                addWithMargins(small(copy("your_day.pattern_eyebrow"), 13f))
                val line = row()
                line.addWithMargins(lead(copy("pattern.weekday_hot"), 16f), width = 0, weight = 1f)
                line.addWithMargins(chevronBadge(), start = 12, width = dp(32), height = dp(32))
                addWithMargins(line, top = 4)
                pressable(this) { navigate(R.id.sync_patterns) }
            },
        )
    }

    /** A tappable calendar block. Marking fills it with the accent colour, with a small bounce. */
    private inner class Block(event: DeviceCalendarEvent) {
        private val shape: GradientDrawable = ui.rounded(ui.tint, 12f)
        private val titleView: TextView = ui.text(event.title, 13f, ui.ink, ui.bold).apply { maxLines = 1 }
        private val timeView: TextView
        private var marked = false
        private var first = true
        private val times = "${SyncFormat.clock(event.startMin)}–${SyncFormat.clock(event.endMin)}"
        val view: LinearLayout

        init {
            timeView = ui.small(times, 12f)
            view = ui.column().apply {
                background = shape
                setPadding(ui.dp(10), ui.dp(4), ui.dp(6), ui.dp(4))
                gravity = Gravity.CENTER_VERTICAL
                minimumHeight = ui.dp(44)
                addView(titleView)
                addView(timeView)
                ui.pressable(this) { vm.toggleMark(event.id) }
            }
        }

        fun setMarked(value: Boolean) {
            if (value == marked && !first) return
            val animate = !first
            first = false
            marked = value
            timeView.text = if (value) "$times · ${ui.copy("your_day.that_one")}" else times
            val fill = if (value) ui.accent else ui.tint
            val titleColor = if (value) ui.onAccent else ui.ink
            val timeColor = if (value) ui.onAccent else ui.muted
            if (!animate) {
                shape.setColor(fill)
                titleView.setTextColor(titleColor)
                timeView.setTextColor(timeColor)
                return
            }
            val fromFill = shape.color?.defaultColor ?: ui.tint
            val fromTitle = titleView.currentTextColor
            val fromTime = timeView.currentTextColor
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 200
                addUpdateListener {
                    val t = it.animatedFraction
                    shape.setColor(ColorUtils.blendARGB(fromFill, fill, t))
                    titleView.setTextColor(ColorUtils.blendARGB(fromTitle, titleColor, t))
                    timeView.setTextColor(ColorUtils.blendARGB(fromTime, timeColor, t))
                }
                start()
            }
            if (value) {
                view.scaleX = 0.96f
                view.scaleY = 0.96f
                view.animate().scaleX(1f).scaleY(1f).setDuration(260).setInterpolator(OvershootInterpolator(3f)).start()
            }
        }
    }

    private companion object {
        const val FIRST_HOUR = 8

        /** Puts +10 about 35 dp from the usual. */
        const val BPM_PER_DOT_UNIT = 2f
    }
}

/** Sunday read-back: four sentences, seven nights, the week in numbers, the check-in card. */
class SyncReadBackFragment : SyncSheetFragment() {
    override fun headerDate(snapshot: SyncSnapshot) = SyncDates.longDate(ReadBack.latestSunday(snapshot.moment))

    override fun build(body: LinearLayout, snapshot: SyncSnapshot) = with(ui) {
        val sunday = ReadBack.latestSunday(snapshot.moment)
        val monday = sunday - 6
        val week = SyncFixtures.readBackWeek(monday)
        val sleep = Usuals.band(snapshot.nights, Metric.SLEEP)
        val rhr = Usuals.band(snapshot.nights, Metric.RHR)
        val hrv = Usuals.band(snapshot.nights, Metric.HRV)
        val bed = Usuals.band(snapshot.nights, Metric.BEDTIME)

        val sentences = ReadBack.sentences(
            ReadBack.Input(week, sleep?.usual, SyncFixtures.readBackHotHours(monday), SyncFixtures.readBackChips()),
        )
        body.addCard(
            card().apply {
                addWithMargins(
                    sectionHeader(
                        copy("read_back.title"),
                        copy("read_back.label", "from" to SyncDates.weekdayDate(monday), "to" to SyncDates.weekdayDate(sunday)),
                    ),
                )
                if (sentences.isEmpty()) addWithMargins(small(copy("read_back.not_ready"), 14f), top = 8)
                sentences.forEachIndexed { i, sentence ->
                    addWithMargins(lead(styled(sentence), 18f), top = if (i == 0) 10 else 10)
                }
                if (sleep != null) {
                    addWithMargins(
                        SyncDrawingView(context).apply {
                            spec = SyncDrawingView.Spec(
                                dots = week.map { night -> night.sleepMin?.let { sleep.normalized(it) } },
                                emphasizeLast = false,
                                captionTopStart = copy("drawing.your_usual"),
                                labels = week.map { SyncDates.weekdayShort(it.day) },
                            )
                            revealOnEntrance()
                        },
                        top = 14,
                    )
                }
            },
        )

        val worn = week.filter { it.worn }
        fun mean(metric: Metric) = worn.mapNotNull(metric.read).takeIf { it.isNotEmpty() }?.average()?.toInt()
        fun usual(value: Int, metric: Metric, band: Band?, format: (Int) -> String): String =
            band?.let {
                copy("usual.value", "value" to format(it.usual.toInt())) +
                    SyncFormat.caret(Usuals.caret(value, it.usual, metric))
            }.orEmpty()

        val rows = listOfNotNull(
            mean(Metric.SLEEP)?.let { v -> Triple(copy("metric.sleep"), SyncFormat.duration(v) to null, usual(v, Metric.SLEEP, sleep, SyncFormat::duration)) },
            mean(Metric.RHR)?.let { v -> Triple(copy("metric.rhr"), v.toString() to copy("unit.bpm"), usual(v, Metric.RHR, rhr) { it.toString() }) },
            mean(Metric.HRV)?.let { v -> Triple(copy("metric.hrv"), v.toString() to copy("unit.ms"), usual(v, Metric.HRV, hrv) { it.toString() }) },
            mean(Metric.BEDTIME)?.let { v -> Triple(copy("metric.bed"), SyncFormat.bedtime(v) to null, usual(v, Metric.BEDTIME, bed, SyncFormat::bedtime)) },
        )
        if (rows.isNotEmpty()) {
            body.addCard(
                card(padding = 14).apply {
                    rows.forEachIndexed { i, (label, value, usualText) ->
                        if (i > 0) addDivider()
                        addWithMargins(ledgerRow(label, value.first, value.second, usualText))
                    }
                },
            )
        }

        // Appears Sunday, gone Monday. The read-back itself stays reachable all week.
        if (snapshot.moment.isSunday) {
            body.addCard(checkInCard { openPlaceholder("placeholder.check_in") })
        }
    }
}

/** Patterns: one to answer, the answered ones with their answers, and the chips across heavier stretches. */
class SyncPatternsFragment : SyncSheetFragment() {
    override fun build(body: LinearLayout, snapshot: SyncSnapshot) = with(ui) {
        val worn = snapshot.nights.count { it.worn }
        body.addCard(
            column().apply {
                addWithMargins(
                    sectionHeader(copy("patterns.title"), copy("patterns.label", "weeks" to (worn / 7), "nights" to worn)),
                )
                addWithMargins(small(copy("patterns.intro"), 14f), top = 6)
            },
        )

        val entries = if (snapshot.recordDeleted) snapshot.entries.size else SyncFixtures.PATTERN_EVENING_ENTRIES
        val next = PatternGate.nextToAsk(SyncFixtures.patternCandidates(), snapshot.patternAnswers, worn, entries)
        if (next != null) {
            lateinit var askCard: LinearLayout
            askCard = card().apply {
                addWithMargins(lead(styled(copy(SyncFixtures.PATTERN_KEYS.getValue(next.id))), 18f))
                val choices = row()
                val answers = PatternAnswer.values().toList()
                answers.forEachIndexed { i, answer ->
                    lateinit var toggle: SyncToggle
                    toggle = SyncToggle(ui, answer.word, 15f, 14f, page) {
                        // The choice lands first, then the card lifts away and the answer joins the list.
                        toggle.setOn(true, animate = true)
                        askCard.animate().alpha(0f).translationY(-dp(10).toFloat())
                            .setStartDelay(220).setDuration(180)
                            .withEndAction { vm.answerPattern(next.id, answer) }
                            .start()
                    }
                    choices.addWithMargins(toggle, end = if (i < answers.lastIndex) 8 else 0, width = 0, height = dp(48), weight = 1f)
                }
                addWithMargins(choices, top = 14)
            }
            body.addCard(askCard)
        } else {
            body.addCard(card().apply { addWithMargins(small(copy("patterns.not_yet"), 14f)) })
        }

        if (snapshot.patternAnswers.isNotEmpty()) {
            body.addCard(
                card().apply {
                    snapshot.patternAnswers.sortedByDescending { it.day }.forEachIndexed { i, record ->
                        if (i > 0) addDivider(top = 12, bottom = 12)
                        addWithMargins(lead(styled(copy(SyncFixtures.PATTERN_KEYS.getValue(record.patternId))), 16f))
                        addWithMargins(
                            small(
                                styled(copy("patterns.you_said", "answer" to record.answer.word, "date" to SyncDates.shortDate(record.day))),
                                13f,
                            ),
                            top = 4,
                        )
                    }
                },
            )
        }

        val stretches = SyncFixtures.heavierStretches()
        body.addCard(
            card().apply {
                PatternGate.stretchSentence(stretches)?.let { sentence ->
                    addWithMargins(sectionHeader(copy("record.title"), copy("patterns.record_label", "count" to SyncFormat.words(stretches.size))))
                    addWithMargins(lead(styled(sentence), 16f), top = 8)
                }
                addWithMargins(small(copy("patterns.footnote"), 13f), top = 8)
            },
        )
    }
}

/** Experiments: one at a time. Stop is one tap with no dialog, and the record keeps it. */
class SyncExperimentsFragment : SyncSheetFragment() {
    override fun build(body: LinearLayout, snapshot: SyncSnapshot) = with(ui) {
        body.addCard(
            column().apply {
                addWithMargins(sectionHeader(copy("experiments.title"), copy("experiments.label")))
                addWithMargins(small(copy("experiments.intro"), 14f), top = 6)
            },
        )

        val running = snapshot.runningExperiment
        if (running != null) body.addCard(runningCard(running, snapshot.moment.day))

        body.addCard(
            card(padding = 0).apply {
                setPadding(dp(16), dp(14), dp(12), dp(6))
                addWithMargins(sectionHeader(copy("experiments.others"), copy("experiments.others_label")), end = 4, bottom = 4)
                val others = SyncFixtures.EXPERIMENT_TEMPLATES.filter { it.first != running?.templateId }
                others.forEachIndexed { i, (id, key) ->
                    if (i > 0) addDivider()
                    addWithMargins(doorRow(copy(key)) {
                        // One at a time: a row does nothing until the running one is stopped.
                        if (snapshot.runningExperiment == null) {
                            TransitionManager.beginDelayedTransition(body, AutoTransition().setDuration(260))
                            vm.startExperiment(id)
                        }
                    })
                }
            },
        )

        val earlier = snapshot.experiments.filter { it.endDay != null }
        if (earlier.isNotEmpty()) {
            body.addCard(
                card().apply {
                    addWithMargins(sectionHeader(copy("experiments.earlier")))
                    earlier.forEach { record ->
                        val key = SyncFixtures.EXPERIMENT_TEMPLATES.first { it.first == record.templateId }.second
                        addWithMargins(lead(copy(key), 16f), top = 10)
                        val date = SyncDates.shortDate(record.endDay!!)
                        addWithMargins(
                            small(
                                if (record.stopped) copy("experiments.stopped_on", "date" to date) else copy("experiments.finished_on", "date" to date),
                                13f,
                            ),
                            top = 2,
                        )
                    }
                },
            )
        }
    }

    private fun runningCard(running: ExperimentRecord, today: Int): View = with(ui) {
        val key = SyncFixtures.EXPERIMENT_TEMPLATES.first { it.first == running.templateId }.second
        val nights = SyncFixtures.experimentNights(running.startDay, today)
        val before = ExperimentCalc.before(nights, running.startDay)
        val after = ExperimentCalc.after(nights, running.startDay)
        val usualSleep = SyncFixtures.USUAL_SLEEP.toDouble()
        val comparison = ExperimentCalc.compare(before, after, usualSleep, SyncFixtures.USUAL_RHR.toDouble())
        val shown: List<Night> = before + after

        lateinit var runningView: LinearLayout
        runningView = card().apply {
            addWithMargins(
                sectionHeader(
                    copy("experiments.running"),
                    copy("experiments.day_of", "n" to ExperimentCalc.dayNumber(running.startDay, today)),
                ),
            )
            addWithMargins(lead(copy(key), 22f).apply { typeface = bold }, top = 8)
            addWithMargins(
                SyncDrawingView(context).apply {
                    spec = SyncDrawingView.Spec(
                        dots = shown.map { ((it.sleepMin ?: 400) - usualSleep).toFloat() / SLEEP_MIN_PER_DOT_UNIT },
                        captionStart = copy("drawing.fortnight_before"),
                        captionEnd = copy("drawing.since", "date" to SyncDates.shortDate(running.startDay)),
                        dividerBeforeSlot = before.size.takeIf { after.isNotEmpty() },
                    )
                    revealOnEntrance(order = 1)
                },
                top = 8,
            )
            addWithMargins(small(ExperimentCalc.sentence(comparison), 14f, ink), top = 8)
            addWithMargins(
                pillButton(copy("experiments.stop")) {
                    runningView.animate().alpha(0f).scaleY(0.96f).setDuration(160)
                        .withEndAction { vm.stopRunningExperiment() }
                        .start()
                },
                top = 12,
                width = WRAP,
            )
        }
        runningView
    }

    private companion object {
        const val SLEEP_MIN_PER_DOT_UNIT = 6f
    }
}

/** The States reference sheet for QA: every string and drawing per state. Doors stay inert here. */
class SyncStatesFragment : SyncSheetFragment() {
    override fun build(body: LinearLayout, snapshot: SyncSnapshot) = with(ui) {
        body.addCard(
            column().apply {
                addWithMargins(title(copy("states.title")))
                addWithMargins(small(copy("states.intro"), 14f), top = 6)
            },
        )
        val today = snapshot.moment.day
        val names = mapOf(
            SyncFixtures.Fixture.BASELINING to "states.name.baselining",
            SyncFixtures.Fixture.IN_SYNC to "states.name.in_sync",
            SyncFixtures.Fixture.DRIFTING to "states.name.drifting",
            SyncFixtures.Fixture.OUT_OF_SYNC to "states.name.out_of_sync",
            SyncFixtures.Fixture.RECOVERING to "states.name.recovering",
            SyncFixtures.Fixture.QUIET to "states.name.quiet",
            SyncFixtures.Fixture.LOW_WEAR to "states.name.low_wear",
        )
        SyncFixtures.Fixture.values().forEach { fixture ->
            val nights = SyncFixtures.nights(fixture, today)
            val model = StateSection.build(nights, SyncFixtures.suppressedDays(fixture, today))
            body.addWithMargins(
                small("${copy(names.getValue(fixture))} · ${approvalLabel(model.leadKey)}", 12f),
                top = 22,
                bottom = 8,
            )
            body.addWithMargins(stateCard(model, doorsEnabled = false))
            if (fixture == SyncFixtures.Fixture.LOW_WEAR) {
                ringCard(nights, DeviceState.NOT_WORN)?.let { body.addWithMargins(it, top = 10) }
            }
        }
    }
}

/** Account: the person's record goes in one tap, with no reason and no dialog. */
class SyncAccountFragment : SyncSheetFragment() {
    override fun build(body: LinearLayout, snapshot: SyncSnapshot) = with(ui) {
        body.addCard(title(copy("account.title")))

        body.addCard(
            card().apply {
                if (snapshot.recordDeleted && snapshot.entries.isEmpty()) {
                    addWithMargins(lead(copy("account.deleted"), 16f))
                } else {
                    addWithMargins(doorRow(copy("account.delete")) {
                        TransitionManager.beginDelayedTransition(body, AutoTransition().setDuration(220))
                        vm.deleteRecord()
                    })
                    addWithMargins(small(copy("account.delete_note"), 13f))
                }
            },
        )

        body.addCard(
            card().apply {
                if (snapshot.trainingWithdrawn) {
                    addWithMargins(lead(copy("account.withdrawn"), 16f))
                } else {
                    addWithMargins(doorRow(copy("account.withdraw")) {
                        TransitionManager.beginDelayedTransition(body, AutoTransition().setDuration(220))
                        vm.withdrawTraining()
                    })
                    addWithMargins(small(copy("account.withdraw_note"), 13f))
                }
            },
        )
    }
}

/** A clearly marked stand-in for a screen specified in another document. */
class SyncPlaceholderFragment : SyncSheetFragment() {
    private val titleKey get() = requireArguments().getString(ARG_TITLE)!!

    override val showsBack get() = titleKey != "placeholder.your_data"

    override fun build(body: LinearLayout, snapshot: SyncSnapshot) = with(ui) {
        body.addCard(
            card().apply {
                addWithMargins(sectionHeader(copy(titleKey), copy("placeholder.not_built")))
                addWithMargins(small(copy("placeholder.body"), 14f), top = 10)
            },
        )
    }

    companion object {
        const val ARG_TITLE = "title_key"
    }
}
