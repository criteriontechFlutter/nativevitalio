package com.critetiontech.ctvitalio.model

import java.io.Serializable

data class DashboardActiveChallenges(
    val challengeId: Int,
    val title: String,
    val description: String,
    val duration: Int,
    val streakDay: Int,
    val streakDays: String,
    val streakMessage: String?
) : Serializable

data class DashboardActiveChallengesWrapper(
    val challenges: String   // JSON string containing an array of PriorityAction
)