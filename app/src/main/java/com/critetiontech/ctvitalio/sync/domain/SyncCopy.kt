package com.critetiontech.ctvitalio.sync.domain

/**
 * The one string table. The app renders nothing that is not in here (a test fails if a Sync layout
 * sets its own text). Only the Drifting and Recovering lead sentences are approved. Everything else
 * is a draft awaiting clinical review and ships behind [draftsEnabled].
 *
 * `{name}` is a slot. `*word*` is the person's own word, rendered in italics.
 */
object SyncCopy {
    data class Entry(val text: String, val approved: Boolean = false)

    /** Debug builds show drafts. A release build must not until clinical review signs them off. */
    @Volatile
    var draftsEnabled: Boolean = true

    private fun approved(text: String) = Entry(text, approved = true)
    private fun draft(text: String) = Entry(text)

    val table: Map<String, Entry> = mapOf(
        // The state section. Keyed by state and slot.
        "state.eyebrow" to draft("The last two weeks"),
        "state.baselining.lead" to draft("Your numbers are here. The comparisons need a few more nights."),
        "state.baselining.reason" to draft("{n} nights in. About {more} more before the comparisons mean anything."),
        "state.baselining.label" to draft("{n} nights in"),
        "state.in_sync.lead" to draft("You've been in your usual range these two weeks."),
        "state.in_sync.reason" to draft("{n} of {worn} nights sat in your usual range."),
        "state.drifting.lead" to approved("The last two weeks have run heavier than your usual."),
        "state.drifting.reason.rhr" to draft("Your resting heart rate has sat {d} bpm above usual for {n} nights."),
        "state.drifting.reason.sleep" to draft("Your sleep has sat {d} min under usual for {n} nights."),
        "state.out_of_sync.lead" to draft("You've been out of sync for about {weeks}."),
        "state.out_of_sync.reason" to draft("Sleep {s} min short, resting heart rate {r} bpm above — both, on {n} of 14 nights."),
        "state.recovering.lead" to approved("You've been back in sync since {when}."),
        "state.recovering.reason.sleep" to draft("Sleep came back first — {m} min closer to usual than the fortnight before."),
        "state.recovering.reason.rhr" to draft("Resting heart rate came back first — {m} bpm closer to usual than the fortnight before."),
        "state.quiet.lead" to draft("Nothing worth saying this week."),
        "state.quiet.reason" to draft("Some stretches don't tell you much, and this is one of them."),
        "state.low_wear.lead" to draft("{n} nights of the last 14."),
        "state.low_wear.reason" to draft("Resting heart rate is there for {r} nights. Sleep isn't."),

        // The drawing.
        "drawing.your_usual" to draft("your usual"),
        "drawing.your_usual_from_night_7" to draft("your usual, from night 7"),
        "drawing.last_night" to draft("last night"),
        "drawing.fortnight_before" to draft("the fortnight before"),
        "drawing.since" to draft("since {date}"),
        "drawing.usual" to draft("usual"),
        "drawing.plus_ten" to draft("+10"),

        // Doors.
        "door.whats_going_on" to draft("What's going on?"),
        "door.might_help" to draft("Something that might help"),
        "door.read_back" to draft("Your week, read back"),

        // Tabs and header.
        "tab.home" to draft("Home"),
        "tab.your_data" to draft("Your data"),
        "header.wordmark" to draft("Sync"),
        "header.morning" to draft("Sync · morning"),
        "header.evening" to draft("Sync · evening"),

        // Ring section, shown by device state.
        "ring.title" to draft("Ring"),
        "ring.last_sent" to draft("last sent {day} night"),
        "ring.never_sent" to draft("nothing sent yet"),
        "ring.body" to draft("On at night is what the comparisons need."),

        // Last night.
        "last_night.title" to draft("Last night"),
        "last_night.label" to draft("to bed {bed} · up {up}"),
        "last_night.not_there" to draft("not there"),
        "last_night.no_sleep" to draft("Sleep isn't there for last night."),
        "metric.rhr" to draft("Resting heart rate"),
        "metric.hrv" to draft("HRV"),
        "metric.recovery" to draft("Ring's recovery"),
        "metric.spo2" to draft("SpO₂"),
        "metric.steps" to draft("Steps"),
        "metric.sleep" to draft("Sleep"),
        "metric.bed" to draft("To bed"),
        "unit.bpm" to draft("bpm"),
        "unit.ms" to draft("ms"),
        "usual.value" to draft("usual {value}"),
        "usual.countdown_one" to draft("usual in 1 night"),
        "usual.countdown" to draft("usual in {n} nights"),

        // Your record.
        "record.title" to draft("Your record"),
        "record.this_week" to draft("this week"),
        "record.nothing_hot" to draft("Nothing ran hot."),
        "record.hot_and_back" to draft("Ran hot from {from}; back by {back}."),
        "record.hot_not_back" to draft("Ran hot from {from}; not back by {back}."),
        "record.slept" to draft("Slept {slept}, usual {usual}{caret}."),

        // The evening question.
        "today.title" to draft("Today"),
        "today.label" to draft("five seconds"),
        "today.question" to draft("How was today?"),
        "today.chips_prompt" to draft("Anything in it? Optional."),
        "today.only_you" to draft("Only you see this."),
        "today.saved" to draft("Saved."),
        "today.line_hint" to draft("A line of your own. Optional."),

        // The check-in card and read-back.
        "check_in.title" to draft("Your sync · this week's check-in"),
        "check_in.body" to draft("Seventeen questions, about two minutes. Only you see this."),
        "read_back.title" to draft("Your week, read back"),
        "read_back.label" to draft("{from} – {to}"),
        "read_back.not_ready" to draft("Your week is read back on Sunday at six."),
        "read_back.nights_under" to draft("{count} under your usual."),
        "read_back.hottest" to draft("{hot} ran hottest; you had it back by {back} evening."),
        "read_back.borrowed" to draft("You borrowed {minutes} minutes of sleep from the weekend."),
        "read_back.chips" to draft("You marked {chips}."),

        // Your day.
        "your_day.title" to draft("Your day"),
        "your_day.label" to draft("beside your calendar"),
        "your_day.nothing_hot" to draft("Nothing ran hot today."),
        "your_day.hot_and_back" to draft("{hours} ran hot from {from}. You got it back by {back}."),
        "your_day.hot_not_back" to draft("{hours} ran hot from {from}. You hadn't got it back by {back}."),
        "your_day.calendar_column" to draft("Your calendar · tap to mark one"),
        "your_day.heart_column" to draft("Heart rate, v. usual"),
        "your_day.that_one" to draft("that one"),
        "your_day.pattern_eyebrow" to draft("Over the last four weeks"),

        // Patterns.
        "patterns.title" to draft("Patterns"),
        "patterns.label" to draft("after {weeks} weeks · {nights} nights"),
        "patterns.intro" to draft("What your own nights and evening words say. You decide whether each one is true."),
        "patterns.you_said" to draft("You said *{answer}* · {date}"),
        "patterns.not_yet" to draft("Nothing to ask you yet."),
        "patterns.record_label" to draft("the last {count} heavier stretches"),
        "patterns.footnote" to draft("Built from your nights and your evening words. A new pattern needs three or four weeks."),
        "pattern.meetings_sleep" to draft("Nights after a day with five or more meetings run forty minutes short."),
        "pattern.chip_leads_body" to draft("Weeks you mark *home*, your resting heart rate rises before your sleep does."),
        "pattern.weekday_hot" to draft("Thursday reviews run hot for you; you get it back by the evening."),
        "pattern.weekend_gives_back" to draft("Weekends give you back about an hour of sleep a night."),

        // Experiments.
        "experiments.title" to draft("Experiments"),
        "experiments.label" to draft("one at a time"),
        "experiments.intro" to draft("Pick one. The app measures before and after, against your own usual."),
        "experiments.running" to draft("Running"),
        "experiments.day_of" to draft("day {n} of 14"),
        "experiments.stop" to draft("Stop this one"),
        "experiments.others" to draft("Others you could try"),
        "experiments.others_label" to draft("two weeks each"),
        "experiments.earlier" to draft("Your earlier ones"),
        "experiments.stopped_on" to draft("You stopped this on {date}."),
        "experiments.finished_on" to draft("Finished on {date}."),
        "experiments.sleep_closer" to draft("Sleep is {n} minutes closer to your usual than the fortnight before."),
        "experiments.sleep_further" to draft("Sleep is {n} minutes further from your usual than the fortnight before."),
        "experiments.sleep_flat" to draft("Sleep hasn't moved yet."),
        "experiments.rhr_closer" to draft("Resting heart rate is {n} bpm closer to your usual than the fortnight before."),
        "experiments.rhr_further" to draft("Resting heart rate is {n} bpm further from your usual than the fortnight before."),
        "experiments.rhr_flat" to draft("Resting heart rate hasn't moved yet."),
        "experiment.finish_by_seven" to draft("Finish by seven."),
        // Named on the canvas. "Screens" is close to the banned word "screen"; see SYNC_NOTES.md.
        "experiment.no_screens" to draft("No screens after ten."),
        "experiment.morning_walk" to draft("A walk before the first meeting."),
        "experiment.lights_out" to draft("Lights out by half past eleven."),
        "experiment.lunch_away" to draft("Lunch away from the desk."),

        // Account.
        "account.title" to draft("Account"),
        "account.delete" to draft("Delete my record"),
        "account.delete_note" to draft("Evening entries, marks, pattern answers and experiments. One tap, no reason needed, and nothing else changes."),
        "account.deleted" to draft("Your record is gone."),
        "account.withdraw" to draft("Stop using my de-identified data"),
        "account.withdraw_note" to draft("One tap. Future use stops."),
        "account.withdrawn" to draft("Future use has stopped."),

        // Stand-ins for screens whose specs are not in this document.
        "placeholder.might_help" to draft("Something that might help"),
        "placeholder.whats_going_on" to draft("What's going on?"),
        "placeholder.your_data" to draft("Your data"),
        "placeholder.check_in" to draft("This week's check-in"),
        "placeholder.body" to draft("This part is specified in another document and has not been built yet."),
        "placeholder.not_built" to draft("not built"),

        // The States reference sheet.
        "states.title" to draft("One section, seven states"),
        "states.intro" to draft("Same place at the top of Home, every day. Only the sentence, the reason line and the drawing change."),
        "states.name.baselining" to draft("Baselining · nights 1–6"),
        "states.name.in_sync" to draft("In sync"),
        "states.name.drifting" to draft("Drifting"),
        "states.name.out_of_sync" to draft("Out of sync"),
        "states.name.recovering" to draft("Recovering · back in sync"),
        "states.name.quiet" to draft("Quiet · a suppressed stretch"),
        "states.name.low_wear" to draft("Low wear · the ring section appears"),
        "states.approved" to draft("approved"),
        "states.draft" to draft("draft"),
    )

    /** Fills slots. Returns an empty string for a draft while drafts are off, so the view stays empty. */
    fun text(key: String, vararg slots: Pair<String, Any>): String {
        val entry = table[key] ?: error("Not in the string table: $key")
        if (!entry.approved && !draftsEnabled) return ""
        return slots.fold(entry.text) { acc, (name, value) -> acc.replace("{$name}", value.toString()) }
    }

    fun isApproved(key: String): Boolean = table[key]?.approved == true
}
