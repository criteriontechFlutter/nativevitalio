package com.critetiontech.ctvitalio.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable

data class NewChallengeModel(
    val id: Int,
    val title: String?,
    val description: String?,
    val rewardPoints: Int,
    val clientId: Int,
    val startsIn: String?,
    val startDate: String?,
    val endDate: String?,
    val peopleJoined: String? // nullable
) : Serializable {
    // Safe hashCode
    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + rewardPoints
        result = 31 * result + clientId
        result = 31 * result + (startsIn?.hashCode() ?: 0)
        result = 31 * result + (startDate?.hashCode() ?: 0)
        result = 31 * result + (endDate?.hashCode() ?: 0)
        result = 31 * result + (peopleJoined?.hashCode() ?: 0)
        return result
    }

    // Helper to decode JSON string to List<Person>
    fun getPeopleJoinedList(): List<Person> {
        return try {
            Gson().fromJson(
                peopleJoined ?: "[]",
                object : TypeToken<List<Person>>() {}.type
            )
        } catch (e: Exception) {
            emptyList()
        }
    }
}

