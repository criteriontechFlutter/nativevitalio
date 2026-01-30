package com.critetiontech.ctvitalio.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class WellnessResponse(
    val status: Int,
    val message: String,
    val responseValue: MovementIndexResponseValue
)

data class MovementIndexResponseValue(
    val vitals: List<VitalItem>,
    val stepsGraph: List<Any>, // empty array in your JSON
    val movementIndexWeek: List<IndexWeekItem>,
    val stressIndexWeek: List<IndexWeekItem>,
    val recoveryIndexWeek: List<IndexWeekItem>,
    val workoutFrequency: WorkoutFrequency?,
    val hrvTrend: HrvTrend?
)
data class VitalItem(
    val pmId: Int?,
    val vmId: Int?,
    val vitalName: String,
    val vmValue: String,
    val vitalDate: String?,
    val vitalTime: String?,
    val vitalDateTime: String?,
    val goalTarget: String?
)
data class IndexWeekItem(
    val date: String,
    val dayName: String,
    val vmValue: Double,
    val goalTarget: Double?
)
data class SleepSummary(
    val sleepStart: String,
    val sleepEnd: String,
    val totalSleep: String,
    val sleepCycles: String,
    val durationPercent: Float
)
data class WorkoutFrequency(
    val totalWeekMinutes: Int,
    val dayWiseMinutes: List<WorkoutDayItem>
)

data class WorkoutDayItem(
    val date: String,
    val dayName: String,
    val minutes: Int
)
data class HrvTrend(
    val hrvZone: String?,
    val hrvBaseline: String?,
    val weeklyHrvAvg: List<HrvAvgItem>
)

data class HrvAvgItem(
    val date: String,
    val dayName: String,
    val vmValue: String
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

//fun getLatestVital(vitalList: List<WellnessItem>, vitalName: String): Reading? {
//    val vital = vitalList.firstOrNull {
//        it.vitalName.equals(vitalName, ignoreCase = true)
//    } ?: return null
//
//    val readings = vital.decodedReadings()
//    if (readings.isEmpty()) return null
//
//    return readings.maxByOrNull { it.vitalDateTime }
//}


