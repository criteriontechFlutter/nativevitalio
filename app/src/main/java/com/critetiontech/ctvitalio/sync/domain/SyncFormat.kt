package com.critetiontech.ctvitalio.sync.domain

import java.util.Locale

object SyncFormat {
    const val CARET_UP = "▲"
    const val CARET_DOWN = "▼"

    /** "6h 45m" */
    fun duration(minutes: Int) = "${minutes / 60}h ${"%02d".format(Locale.ENGLISH, minutes % 60)}m"

    /** "23:40" from a minute of the day. */
    fun clock(minuteOfDay: Int) =
        "%02d:%02d".format(Locale.ENGLISH, minuteOfDay / 60, minuteOfDay % 60)

    /** "23:40" from minutes since the noon before bed. */
    fun bedtime(bedMin: Int) = clock((bedMin + 720) % 1440)

    fun thousands(value: Int) = "%,d".format(Locale.ENGLISH, value)

    fun caret(caret: Caret) = when (caret) {
        Caret.UP -> " $CARET_UP"
        Caret.DOWN -> " $CARET_DOWN"
        Caret.NONE -> ""
    }

    private val ONES = listOf(
        "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
        "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen",
    )
    private val TENS = listOf("", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety")

    /** Numbers read as words in sentences: "seventy minutes". Falls back to digits past 999. */
    fun words(n: Int): String = when {
        n < 0 || n > 999 -> n.toString()
        n < 20 -> ONES[n]
        n < 100 -> TENS[n / 10] + if (n % 10 > 0) "-${ONES[n % 10]}" else ""
        else -> "${ONES[n / 100]} hundred" + if (n % 100 > 0) " and ${words(n % 100)}" else ""
    }

    fun capitalized(s: String) = s.replaceFirstChar { it.uppercase(Locale.ENGLISH) }

    /** "two o'clock" or "half past six", from a 24-hour hour and minute. */
    fun spokenTime(hour: Int, minute: Int = 0): String {
        val twelve = if (hour % 12 == 0) 12 else hour % 12
        return if (minute == 0) "${words(twelve)} o'clock" else "half past ${words(twelve)}"
    }

    /** "once", "twice", "three times" */
    fun times(n: Int) = when (n) {
        1 -> "once"
        2 -> "twice"
        else -> "${words(n)} times"
    }

    /** "three weeks", "a week" */
    fun weeks(n: Int) = if (n <= 1) "a week" else "${words(n)} weeks"
}
