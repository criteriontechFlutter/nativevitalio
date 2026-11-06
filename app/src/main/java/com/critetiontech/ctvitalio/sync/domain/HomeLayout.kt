package com.critetiontech.ctvitalio.sync.domain

enum class DeviceState { OK, DISCONNECTED, UNCHARGED, NOT_WORN }

enum class HomeSection { QUESTION, CHECK_IN, YOUR_DAY, LAST_TWO_WEEKS, RING, LAST_NIGHT, RECORD, HELP }

/**
 * Which sections Home shows, top to bottom.
 *
 * Privacy rule: layout is a function of time and device only, so a screenshot of Home cannot reveal
 * a person's state by which sections are present. That is enforced here by the signature: this
 * function cannot read [SyncState] or [Hold] because it is never given them. Do not add them.
 */
object HomeLayout {
    const val EVENING_STARTS_HOUR = 17
    const val EVENING_ENDS_HOUR = 3

    fun sections(moment: Moment, device: DeviceState): List<HomeSection> {
        val ring = if (device == DeviceState.OK) emptyList() else listOf(HomeSection.RING)
        return if (moment.isEvening) {
            val top = if (moment.isSunday) HomeSection.CHECK_IN else HomeSection.QUESTION
            listOf(top, HomeSection.YOUR_DAY, HomeSection.LAST_TWO_WEEKS) + ring + HomeSection.HELP
        } else {
            listOf(HomeSection.LAST_TWO_WEEKS) + ring +
                listOf(HomeSection.LAST_NIGHT, HomeSection.RECORD, HomeSection.HELP)
        }
    }

    /** "Only you see this" shows on the first three evening answers, then not. */
    fun showOnlyYouSeeThis(answersIncludingThisOne: Int) = answersIncludingThisOne <= 3
}
