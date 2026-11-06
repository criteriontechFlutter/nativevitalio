package com.critetiontech.ctvitalio.sync.ui

import android.text.InputType
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.sync.data.SyncFixtures
import com.critetiontech.ctvitalio.sync.domain.Band
import com.critetiontech.ctvitalio.sync.domain.DayAnswer
import com.critetiontech.ctvitalio.sync.domain.DayChip
import com.critetiontech.ctvitalio.sync.domain.EveningEntry
import com.critetiontech.ctvitalio.sync.domain.HomeLayout
import com.critetiontech.ctvitalio.sync.domain.HomeSection
import com.critetiontech.ctvitalio.sync.domain.HotHours
import com.critetiontech.ctvitalio.sync.domain.Metric
import com.critetiontech.ctvitalio.sync.domain.Night
import com.critetiontech.ctvitalio.sync.domain.ReadBack
import com.critetiontech.ctvitalio.sync.domain.SyncDates
import com.critetiontech.ctvitalio.sync.domain.SyncFormat
import com.critetiontech.ctvitalio.sync.domain.SyncStateMachine
import com.critetiontech.ctvitalio.sync.domain.Usuals
import com.critetiontech.ctvitalio.sync.ui.SyncUi.Companion.WRAP

/** Home. Which sections show comes only from [HomeLayout], which cannot see the person's state. */
class SyncHomeFragment : SyncSheetFragment() {
    override val showsBack = false

    private var question: QuestionCard? = null

    override fun onResume() {
        super.onResume()
        vm.logHomeOpen()
    }

    override fun onDestroyView() {
        question = null
        super.onDestroyView()
    }

    override fun headerLabel(snapshot: SyncSnapshot) =
        ui.copy(if (snapshot.moment.isEvening) "header.evening" else "header.morning")

    /** In the evening, today's answer animates in place on the card rather than rebuilding Home. */
    override fun rebuildKey(snapshot: SyncSnapshot): Any {
        val base = super.rebuildKey(snapshot) as SyncSnapshot
        return if (snapshot.moment.isEvening) base.copy(entries = emptyList()) else base
    }

    override fun build(body: LinearLayout, snapshot: SyncSnapshot) {
        question = null
        HomeLayout.sections(snapshot.moment, snapshot.device).forEachIndexed { index, section ->
            when (section) {
                HomeSection.QUESTION -> body.addCard(QuestionCard().also { question = it }.view)
                HomeSection.CHECK_IN -> body.addCard(ui.checkInCard { openPlaceholder("placeholder.check_in") })
                HomeSection.YOUR_DAY -> body.addCard(yourDayCard(snapshot))
                HomeSection.LAST_TWO_WEEKS -> body.addCard(stateCard(snapshot.state, revealOrder = index))
                HomeSection.RING -> ringCard(snapshot.nights, snapshot.device)?.let { body.addCard(it) }
                HomeSection.LAST_NIGHT -> body.addCard(lastNightCard(snapshot))
                HomeSection.RECORD -> body.addCard(recordCard(snapshot))
                HomeSection.HELP -> body.addCard(
                    ui.doorCard(ui.copy("door.might_help")) { openPlaceholder("placeholder.might_help") },
                )
            }
        }
    }

    override fun update(snapshot: SyncSnapshot) {
        question?.bind(snapshot)
    }

    // Home, evening

    /**
     * Five seconds, one decision at a time: tap how the day was and it is saved at once. Only then
     * do the optional chips and line open beneath it, so nothing asks for attention before the answer.
     */
    private inner class QuestionCard {
        private val answers = DayAnswer.values().toList()
        private val chips = DayChip.values().toList()

        private val answerToggles = answers.map { answer ->
            SyncToggle(ui, answer.word, 16f, 14f, ui.page) { vm.answerToday(answer, emptySet()) }
        }
        private val chipToggles = chips.map { chip ->
            SyncToggle(ui, chip.word, 14f, 22f, ui.page) { vm.toggleTodayChip(chip) }
        }
        private val optional: LinearLayout
        private val saved: TextView
        private val onlyYou: TextView
        private val line: EditText
        val view: LinearLayout

        init {
            with(ui) {
                optional = column().apply { visibility = View.GONE }
                optional.addWithMargins(small(copy("today.chips_prompt"), 14f), top = 16)
                val chipRow = row()
                chipToggles.forEachIndexed { i, toggle ->
                    chipRow.addWithMargins(toggle, end = if (i < chipToggles.lastIndex) 6 else 0, width = 0, height = dp(44), weight = 1f)
                }
                optional.addWithMargins(chipRow, top = 8)
                line = freeLine()
                optional.addWithMargins(line, top = 8)

                saved = small(copy("today.saved"), 13f, accent)
                onlyYou = small(copy("today.only_you"), 12f)

                view = card().apply {
                    addWithMargins(sectionHeader(copy("today.title"), copy("today.label")))
                    addWithMargins(lead(copy("today.question"), 24f).apply { typeface = bold }, top = 10)
                    val answerRow = row()
                    answerToggles.forEachIndexed { i, toggle ->
                        answerRow.addWithMargins(toggle, end = if (i < answerToggles.lastIndex) 8 else 0, width = 0, height = dp(52), weight = 1f)
                    }
                    addWithMargins(answerRow, top = 14)
                    addWithMargins(optional)
                    val foot = row()
                    foot.addWithMargins(onlyYou, width = 0, weight = 1f)
                    foot.addWithMargins(saved, width = WRAP)
                    addWithMargins(foot, top = 12)
                }
                saved.alpha = 0f
            }
        }

        private var firstBind = true

        fun bind(snapshot: SyncSnapshot) {
            val entry = snapshot.todaysEntry
            val answered = entry != null
            val wasOpen = optional.visibility == View.VISIBLE
            val animate = !firstBind

            if (animate && answered != wasOpen) {
                TransitionManager.beginDelayedTransition(body, AutoTransition().setDuration(280))
            }
            answers.forEachIndexed { i, answer -> answerToggles[i].setOn(entry?.answer == answer, animate) }
            chips.forEachIndexed { i, chip -> chipToggles[i].setOn(entry?.chips?.contains(chip) == true, animate) }
            optional.visibility = if (answered) View.VISIBLE else View.GONE
            if (!line.hasFocus()) line.setText(entry?.line.orEmpty())

            val answersIncludingThisOne = snapshot.entries.size + if (answered) 0 else 1
            onlyYou.visibility = if (HomeLayout.showOnlyYouSeeThis(answersIncludingThisOne)) View.VISIBLE else View.INVISIBLE

            if (answered) {
                if (animate) saved.animate().alpha(1f).setDuration(220).start() else saved.alpha = 1f
            } else {
                saved.alpha = 0f
            }
            firstBind = false
        }

        private fun freeLine(): EditText = with(ui) {
            EditText(context).apply {
                hint = copy("today.line_hint")
                setHintTextColor(muted)
                setTextColor(ink)
                typeface = italic
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                background = rounded(page, 14f)
                minHeight = dp(44)
                setPadding(dp(14), dp(10), dp(14), dp(10))
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                imeOptions = EditorInfo.IME_ACTION_DONE
                setOnEditorActionListener { view, action, _ ->
                    if (action == EditorInfo.IME_ACTION_DONE) {
                        vm.setTodayLine(view.text.toString())
                        view.clearFocus()
                        true
                    } else {
                        false
                    }
                }
                setOnFocusChangeListener { view, hasFocus ->
                    if (!hasFocus) vm.setTodayLine((view as EditText).text.toString())
                }
            }
        }
    }

    private fun yourDayCard(snapshot: SyncSnapshot): View = with(ui) {
        val lastHour = snapshot.moment.lastFinishedHour
        val today = SyncFixtures.hours(snapshot.moment.logicalDay, lastHour + 1)
        val reading = HotHours.read(today, HotHours.bands(SyncFixtures.hourHistory()))
        card().apply {
            addWithMargins(sectionHeader(copy("your_day.title"), copy("your_day.label")))
            val line = row()
            line.addWithMargins(lead(HotHours.yourDaySentence(reading, lastHour), 18f), width = 0, weight = 1f)
            line.addWithMargins(chevronBadge(), start = 12, width = dp(32), height = dp(32))
            addWithMargins(line, top = 8)
            pressable(this) { navigate(R.id.sync_your_day) }
        }
    }

    // Home, morning

    private fun lastNightCard(snapshot: SyncSnapshot): View = with(ui) {
        val night = snapshot.lastNight
        val label = if (night?.bedMinuteOfDay != null && night.wakeMinuteOfDay != null) {
            copy(
                "last_night.label",
                "bed" to SyncFormat.clock(night.bedMinuteOfDay!!),
                "up" to SyncFormat.clock(night.wakeMinuteOfDay!!),
            )
        } else {
            copy("last_night.not_there")
        }
        val nights = snapshot.nights
        val sleep = night?.sleepMin

        card().apply {
            addWithMargins(sectionHeader(copy("last_night.title"), label))
            if (sleep == null) {
                addWithMargins(lead(copy("last_night.no_sleep"), 18f), top = 10)
            } else {
                val headline = row().apply { gravity = Gravity.BOTTOM }
                // The number stays still: counting up would briefly show a sleep time that never happened.
                headline.addWithMargins(text(SyncFormat.duration(sleep), 44f, ink, bold), width = WRAP)
                headline.addWithMargins(
                    small(usualText(sleep, Metric.SLEEP, nights, SyncFormat::duration), 14f).apply {
                        setPadding(0, 0, 0, dp(8))
                    },
                    start = 10,
                    width = WRAP,
                )
                addWithMargins(headline, top = 6)
            }
            night ?: return@apply

            val tiles = listOfNotNull(
                night.rhr?.let { metricTile(copy("metric.rhr"), it.toString(), usualText(it, Metric.RHR, nights)) },
                night.hrv?.let { metricTile(copy("metric.hrv"), it.toString(), usualText(it, Metric.HRV, nights)) },
                night.recovery?.let { metricTile(copy("metric.recovery"), it.toString(), usualText(it, Metric.RECOVERY, nights)) },
            )
            if (tiles.isNotEmpty()) {
                val tileRow = row().apply { gravity = Gravity.TOP }
                tiles.forEachIndexed { i, tile ->
                    tileRow.addWithMargins(
                        tile,
                        end = if (i < tiles.lastIndex) 8 else 0,
                        width = 0,
                        height = android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        weight = 1f,
                    )
                }
                addWithMargins(tileRow, top = 12)
            }

            val quiet = listOfNotNull(
                night.spo2?.let { "${copy("metric.spo2")} $it, ${usualText(it, Metric.SPO2, nights)}" },
                night.steps?.let {
                    "${copy("metric.steps")} ${SyncFormat.thousands(it)}, ${usualText(it, Metric.STEPS, nights, SyncFormat::thousands)}"
                },
            ).joinToString(" · ")
            if (quiet.isNotEmpty()) addWithMargins(small(quiet, 12f), top = 10)
        }
    }

    /** "usual 57 ▲", or the countdown until night 7. Every number sits beside the person's usual. */
    private fun usualText(
        value: Int,
        metric: Metric,
        nights: List<Night>,
        format: (Int) -> String = { it.toString() },
    ): String {
        val band: Band = Usuals.band(nights, metric) ?: return countdown(nights)
        val usual = band.usual.toInt()
        return ui.copy("usual.value", "value" to format(usual)) +
            SyncFormat.caret(Usuals.caret(value, band.usual, metric))
    }

    private fun countdown(nights: List<Night>): String {
        val left = (SyncStateMachine.BASELINE_NIGHTS - nights.count { it.worn }).coerceAtLeast(1)
        return if (left == 1) ui.copy("usual.countdown_one") else ui.copy("usual.countdown", "n" to left)
    }

    private fun recordCard(snapshot: SyncSnapshot): View = with(ui) {
        val logical = snapshot.moment.logicalDay
        val monday = logical - (SyncDates.weekday(logical) - SyncDates.MONDAY)
        val bands = HotHours.bands(SyncFixtures.hourHistory())
        val thisWeek = snapshot.entries.filter { it.day in monday..logical }.sortedByDescending { it.day }

        card().apply {
            addWithMargins(sectionHeader(copy("record.title"), copy("record.this_week")))
            addWithMargins(View(context), height = dp(4))
            thisWeek.forEachIndexed { i, entry ->
                if (i > 0) addDivider()
                val words = (listOf(entry.answer.word) + entry.chips.map { it.word }).joinToString(" · ")
                addWithMargins(recordRow(SyncDates.weekdayShort(entry.day), words, recordNote(entry, snapshot, bands)))
            }
            if (snapshot.nights.size >= SyncStateMachine.BASELINE_NIGHTS) {
                if (thisWeek.isNotEmpty()) addDivider()
                addWithMargins(readBackRow(snapshot))
            }
        }
    }

    private fun readBackRow(snapshot: SyncSnapshot): View = with(ui) {
        row().apply {
            minimumHeight = dp(52)
            addWithMargins(dayPill(SyncDates.weekdayShort(ReadBack.latestSunday(snapshot.moment))), end = 12, width = dp(46))
            addWithMargins(text(copy("door.read_back"), 16f, ink, medium), width = 0, weight = 1f)
            addWithMargins(chevronBadge(), start = 10, width = dp(32), height = dp(32))
            pressable(this, scale = 0.985f) {
                vm.logReadBackOpened()
                navigate(R.id.sync_read_back)
            }
        }
    }

    private fun recordNote(entry: EveningEntry, snapshot: SyncSnapshot, bands: Map<Int, Band>): String {
        val lastHour = if (entry.day == snapshot.moment.logicalDay) snapshot.moment.lastFinishedHour else 18
        val reading = HotHours.read(SyncFixtures.hours(entry.day, lastHour + 1), bands)
        val note = HotHours.recordNote(reading, lastHour) ?: run {
            val night = snapshot.nights.firstOrNull { it.day == entry.day }
            val band = Usuals.band(snapshot.nights, Metric.SLEEP)
            val slept = night?.sleepMin
            val nothing = ui.copy("record.nothing_hot")
            if (slept != null && band != null) {
                nothing + " " + ui.copy(
                    "record.slept",
                    "slept" to SyncFormat.duration(slept),
                    "usual" to SyncFormat.duration(band.usual.toInt()),
                    "caret" to SyncFormat.caret(Usuals.caret(slept, band.usual, Metric.SLEEP)),
                )
            } else {
                nothing
            }
        }
        return listOfNotNull(entry.line, note).joinToString("\n")
    }
}
