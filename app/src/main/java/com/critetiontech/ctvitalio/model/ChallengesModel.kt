package com.critetiontech.ctvitalio.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class JoinedChallenge(
    val id: Int,
    val title: String,
    val description: String,
    val rewardPoints: Int,
    val durationDays: Int,
    val clientId: Int,
    val daysLeft: Int,
    val daysAchieved: String,
    val daysAchieved1: String,
    val peopleJoined: String  // ← change from List<NewPerson> to String
) {
    // Helper to decode JSON string to List<NewPerson>
    fun getPeopleJoinedList(): List<Person> {
        return try {
            Gson().fromJson(
                peopleJoined,
                object : TypeToken<List<Person>>() {}.type
            )
        } catch (e: Exception) {
            emptyList()
        }
    }
}

data class Person(
    val empId: String,
    val employeeName: String,
    val imageURL: String
)
