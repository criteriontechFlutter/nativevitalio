package com.critetiontech.ctvitalio.model

import com.google.gson.annotations.SerializedName

/**
 * LeaderboardItem.kt
 *
 * Model for a single leaderboard entry.
 * Parsed from the leaderboardData JSON field of the Patient object.
 */
data class LeaderboardItem(
    val rank: Int,
    val empId: String,
    @SerializedName("empName")
    val empName: String,
    val totalPoints: Int,
    val imageURL: String
)
