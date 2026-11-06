package com.critetiontech.ctvitalio.sync.domain

import java.util.Calendar
import java.util.TimeZone

/**
 * Epoch-day helpers. The app's minSdk is 24 and core library desugaring is off, so java.time
 * would crash on API 24 and 25; everything here goes through Calendar instead.
 */
object SyncDates {
    const val MONDAY = 1
    const val SATURDAY = 6
    const val SUNDAY = 7

    private val WEEKDAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private val MONTHS = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

    /** Monday is 1, Sunday is 7. Epoch day 0 was a Thursday. */
    fun weekday(day: Int): Int = Math.floorMod(day + 3, 7) + 1

    fun weekdayName(day: Int) = WEEKDAYS[weekday(day) - 1]
    fun weekdayShort(day: Int) = weekdayName(day).take(3)

    private fun utc(day: Int) = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = day * 86_400_000L
    }

    fun dayOfMonth(day: Int) = utc(day).get(Calendar.DAY_OF_MONTH)
    fun monthName(day: Int) = MONTHS[utc(day).get(Calendar.MONTH)]
    fun monthShort(day: Int) = monthName(day).take(3)

    /** "Friday 6 November" */
    fun longDate(day: Int) = "${weekdayName(day)} ${dayOfMonth(day)} ${monthName(day)}"

    /** "23 Oct" */
    fun shortDate(day: Int) = "${dayOfMonth(day)} ${monthShort(day)}"

    /** "Mon 26 Oct" */
    fun weekdayDate(day: Int) = "${weekdayShort(day)} ${shortDate(day)}"

    fun epochDayOf(year: Int, month: Int, dayOfMonth: Int): Int {
        val c = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        c.clear()
        c.set(year, month, dayOfMonth)
        return (c.timeInMillis / 86_400_000L).toInt()
    }

    /** The most recent Sunday on or before [day]. */
    fun sundayOnOrBefore(day: Int) = day - Math.floorMod(weekday(day), 7)
}

/** A local wall-clock moment. Layout and the evening window read this and nothing about the person. */
data class Moment(val day: Int, val hour: Int, val minute: Int = 0) {
    /** The evening runs to 03:00, so 01:00 on Saturday still belongs to Friday. */
    val logicalDay: Int get() = if (hour < HomeLayout.EVENING_ENDS_HOUR) day - 1 else day

    val isEvening: Boolean
        get() = hour >= HomeLayout.EVENING_STARTS_HOUR || hour < HomeLayout.EVENING_ENDS_HOUR

    val isSunday: Boolean get() = SyncDates.weekday(logicalDay) == SyncDates.SUNDAY

    /** The last clock hour with a finished mean today, within the 08:00 to 19:00 rows. */
    val lastFinishedHour: Int
        get() = if (hour < HomeLayout.EVENING_ENDS_HOUR) 18 else (hour - 1).coerceIn(8, 18)
}
