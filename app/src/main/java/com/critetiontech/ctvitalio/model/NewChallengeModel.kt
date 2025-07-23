package com.critetiontech.ctvitalio.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class NewChallengeModel(
    val id: Int,
    val title: String,
    val description: String,
    val rewardPoints: Int,
    val clientId: Int,
    val startsIn: String,
    val peopleJoined: String  // ← change from List<NewPerson> to String
) {
    // Helper to decode JSON string to List<NewPerson>
    fun getPeopleJoinedList(): List<NewPerson> {
        return try {
            Gson().fromJson(
                peopleJoined,
                object : TypeToken<List<NewPerson>>() {}.type
            )
        } catch (e: Exception) {
            emptyList()
        }
    }
}

data class NewPerson(
    val empId: String,
    val employeeName: String,
    val imageURL: String
)
