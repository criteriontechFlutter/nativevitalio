package com.critetiontech.ctvitalio.model

data class BPLog(
    val systolic: Int,
    val diastolic: Int,
    val map: Int,
    val time: String,
    val status: String
)
data class BloodPressureResponse(
    val status: Int,
    val message: String,
    val responseValue: ResponseValueBPLog?
)

data class ResponseValueBPLog(
    val Summary: Summary?,
    val TodayLogs: List<TodayLog>?,
    val WeeklyMapGraph: List<WeeklyMapGraph>?,
    val WeeklyMapTrend: List<WeeklyMapTrend>?
)
data class Summary(
    val systolic: Double,
    val diastolic: Double,
    val unit: String,
    val vitalDateTime: String,
    val dayMinMAP: Double,
    val dayMaxMAP: Double,
    val weekAvgMAP: Double
)
data class TodayLog(
    val systolic: Double,
    val diastolic: Double,
    val map: Double,
    val time: String,
    val dateTime: String,
    val unit: String,
    val status: String
)
data class WeeklyMapGraph(
    val date: String,
    val dayName: String,
    val mapValue: Double ,
    val avgValue: Double
)
data class WeeklyMapTrend(
    val date: String,
    val dayName: String,
    val minMAP: Double,
    val maxMAP: Double
)

data class GlucoseResponse(
    val status: Int,
    val message: String,
    val responseValue: GlucoseResponseValue
)

data class GlucoseResponseValue(
    val Summary: GlucoseSummary,
    val TodayLogs: List<GlucoseLog>,
    val GlucoseAvgGraph: List<WeeklyMapGraph>,
    val GlucoseMonthlyGraph: List<MonthlyGraph>,
    val GlucoseTrendGraph: List<TrendGraph>
)
data class TrendGraph(
    val date: String,
    val dayName: String,
    val minValue: Double,
    val maxValue: Double
)
data class GlucoseSummary(
    val vmValue: Double,
    val unit: String,
    val dayMin: Double,
    val dayMax: Double,
    val weekAvg: Double
)
data class MonthlyGraph(
    val date: String? = null,
    val avgValue: Double? = 0.0
)
data class GlucoseLog(
    val value: Double,
    val unit: String,
    val time: String,
    val status: String
)