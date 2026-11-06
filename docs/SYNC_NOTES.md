# Sync daily loop: build notes

Companion to "Sync — daily loop build spec" (6 Nov 2026). It records what the build assumes, where
the spec disagrees with itself, and what is still missing.

## How to see it

Debug builds add a second launcher icon, **Sync (preview)**. Long-press the date in the header to switch
state and time of day, open the States sheet, or reset the dummy data.

All data is dummy (`sync/data/SyncFixtures.kt`), but the fixtures are nights, not canned sentences: the
state and every string are computed from them by the real domain code.

```
adb shell am start -n com.critetiontech.ctvitalio/.sync.ui.SyncPreviewLauncher \
  --es sync_fixture DRIFTING --es sync_time EVENING --es sync_screen your_day
```

- `sync_fixture`: BASELINING, IN_SYNC, DRIFTING, OUT_OF_SYNC, RECOVERING, QUIET, LOW_WEAR
- `sync_time`: LIVE, MORNING, EVENING, SUNDAY_EVENING
- `sync_screen`: your_day, read_back, patterns, experiments, states, account

## Design: Vitalio theme, not the spec's look

Section 8 of the spec describes a "consulting room" look (olive, paper, Literata). By decision of the
developer, Sync instead uses Vitalio's own theme: blue header, rounded sheet, white 20 dp cards, Lato, blue
accents, and the app's dark mode through `values-night`. Colours are in `res/values/sync_colors.xml`.

**Replaced from the spec:** the palette, fonts, hairline sections, and "nothing boxed except what can be
tapped". Home morning now scrolls instead of fitting one screen.

**Kept, because they are product rules rather than looks:** no score, gauge, ring, progress bar or streak; no
colour by direction (every dot is the same blue); the Out of sync tick as the only attention colour; every
number beside the person's usual; the copy rules; the privacy rules; layout by time and device only.

**Easier flow:** the evening chips and free line stay hidden until the day is answered, so the first decision
is a single tap. Tapping another answer changes it.

**Motion:** sections rise in one after another on first open; the drawing draws its usual, then pops its dots
in; screens slide with a shared axis; a pill slides between tabs; choices fill with a small bounce, and open
their options in place; every tappable has a ripple, press scale and light haptic. Animators follow the
system's animation setting. Numbers never animate, since a count-up would briefly show a value that never
happened.

## Where the spec disagrees with itself

| Topic | The conflict | What the build does |
|---|---|---|
| Caret tolerance | Text says "more than" a tolerance; the canvas draws a caret at exactly it (6h 45m v 6h 40m) | Follows the canvas (`Usuals.caret`) |
| "score" | Banned word, but Recovery must be "labelled as the ring's score" | Label reads "Ring's recovery" |
| "screen" | Banned word, but the canvas has "No screens after ten." | Kept as drawn; needs a copy decision |
| Usual from night 7 | The window is nights 15 to 42 back, which do not exist on night 7 | Uses all worn nights until 7 older ones exist |
| Experiment "before" | "14 nights" in one place, "14 worn nights" in another | Worn nights |
| Read-back timing | Generated Sunday 18:00 for Mon to Sun, but Sunday night has not happened | Nights are labelled by the day the person woke |
| Canvas dates | "Sunday 2 November" and "Mon 27 Oct" are not 2026 dates; "day 9, since 28 Oct" is day 10 on 6 Nov | Dates are computed: Sunday 1 November, Mon 26 Oct, since 29 Oct |

## Questions the spec leaves open

These change behaviour, so each needs an owner. The current default is in brackets.

1. **Relapse from Recovering.** The diagram has no arrow from Recovering back to Drifting, so someone who
   relapses keeps reading "You've been back in sync". [Follows the diagram. This looks wrong for a health
   product and should be decided first.]
2. **The usual moves during a long drift.** Once a run of bad nights is more than about 21 long, those
   nights enter the usual's window, so the usual drifts toward them and the band widens. A 21-night run
   already turned "5 bpm above" into "4 bpm above". The data team should test this on COHORT_NOV2026.
3. **Low wear threshold** is undefined. [Fewer than 7 of the last 14 nights with sleep.]
4. **What makes a day suppressed for Quiet** is undefined. [The fixture uses travel chips. Quiet shows from
   the 4th consecutive day, per "1 to 3 days show nothing new".]
5. **Which metric a state dot shows** is undefined, since the state reads two metrics. [Whichever of sleep
   or resting heart rate is furthest from its usual, heavier upward. Nothing is averaged.]
6. **"Outside the band"** for the 5-of-7 rule. [Either sleep or resting heart rate outside.]
7. **"Since the weekend"** is an approved string, but the 7-night rule means a return is rarely that recent.
   [The date is a slot: "the weekend" for Sat or Sun, otherwise the weekday. Clinical review should confirm
   that editing a slot keeps the approval.]
8. **Drifting shows "Something that might help" twice**: in the section's doors, and in its fixed place
   lower down. Both are specified, so both are built.
9. **The eight "open decisions" in section 10 of the spec** are still open. Your day and Recovery and SpO2
    are built as drawn.

## Not built, and why

- **Specified in other documents, which were not available:** onboarding and consent, Your data, the weekly
  check-in, What's going on?, Something that might help and its exercises, the team view. Each door opens a
  labelled placeholder.
- **Backend and hardware:** real ring data, the server, real calendar reading (no `READ_CALENDAR` permission
  has been added; `SyncFixtures.calendar()` stands in), upload of pilot metrics, push delivery of nudges
  (`NudgePolicy` is the rule it must pass through).
- **Pattern effect calculations:** the gates are real and tested, but the four templates' effects are fixture
  values until there is a pipeline.
- **Connection to the Vitalio app:** `SyncActivity` is reachable only from the debug launcher. A release build
  has no way in, and drafts are hidden there until clinical review.

## Rules enforced by tests

`SyncRulesTest` and `SyncDomainTest` run with `./gradlew :app:testDebugUnitTest`.

- Layout comes from `HomeLayout.sections(moment, device)`, which is never given the state. In-sync and
  out-of-sync Home produce the same section list.
- Calendar titles do not appear in server payloads, `toString()` or marks.
- Banned words, verbs and instructions do not appear in the string table, and Sync code sets no literal text.
- Only the Drifting and Recovering leads are approved, and drafts render empty when the flag is off.
- All seven states are computed from nights, and the canvas's numbers and sentences are asserted.

The checklist's layout snapshot test is at the section-list level. A pixel-level version needs an instrumented
screenshot test, which has not been added.
