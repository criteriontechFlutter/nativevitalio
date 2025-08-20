package com.critetiontech.ctvitalio.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

data class NewChallengeModel(
    val id: Int,
    val title: String,
    val description: String,
    val rewardPoints: Int,
    val clientId: Int,
    val startsIn: String,
    val startDate: String,
    val endDate: String,
    val peopleJoined: String  // ← change from List<NewPerson> to String
) : Serializable {
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

