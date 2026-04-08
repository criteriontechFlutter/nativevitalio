package com.critetiontech.ctvitalio.model

import java.io.Serializable

data class DashboardActiveChallenges(
    val challengeId: Int = 0,
    val title: String = "",
    val description: String = "",
    val duration: Int = 0,
    val streakDay: Int = 0,
    val progress: Int = 0,
    val streakDays: String = "",
    val streakMessage: String? = null,
    val graphData: String = ""
) : Serializable

data class DashboardActiveChallengesWrapper(
    val challenges: String   // JSON string containing an array of PriorityAction
)

data class ResponseValueModel(
    val joinedChallenges: List<DashboardActiveChallenges>,
    val pendingChallenges: List<PendingChallenge>
)

data class PendingChallenge(
    val challengeId: Int,
    val categoryId: Int,
    val challengeTypeId: Int,
    val title: String,
    val description: String,
    val duration: Int,
    val peopleJoined: List<PeopleJoined>
)

data class PeopleJoined(
    val empId: String,
    val employeeName: String,
    val imageURL: String
)