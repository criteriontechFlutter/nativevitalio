package com.critetiontech.ctvitalio.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class WellnessResponse(
    val status: Int,
    val message: String,
    val responseValue: List<WellnessItem>
)

data class WellnessItem(
    val vmId: Int,
    val vitalName: String,
    val readings: String
) {
    fun decodedReadings(): List<Reading> {
        val type = object : TypeToken<List<Reading>>() {}.type
        return Gson().fromJson(readings, type)
    }
}
data class SleepSummary(
    val sleepStart: String,
    val sleepEnd: String,
    val totalSleep: String,
    val sleepCycles: String,
    val durationPercent: Float
)


data class Reading(
    val pmId: Int?,
    val vmValue: Double?,
    val vmValueText: String?,
    val vitalDate: String,
    val vitalTime: String,
    val clientID: String,
    val vitalDateTime: String
)

fun getLatestVital(vitalList: List<WellnessItem>, vitalName: String): Reading? {
    val vital = vitalList.firstOrNull {
        it.vitalName.equals(vitalName, ignoreCase = true)
    } ?: return null

    val readings = vital.decodedReadings()
    if (readings.isEmpty()) return null

    return readings.maxByOrNull { it.vitalDateTime }
}


