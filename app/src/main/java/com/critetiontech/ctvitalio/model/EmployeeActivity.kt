package com.critetiontech.ctvitalio.model

data class EmployeeActivity(
    val id: Int,
    val pid: Int,
    val activityId: Int,
    val activityName: String,
    val startTime: String,
    val endTime: String,
    val clientId: Int,
    val userId: Int
)
data class EmployeeActivityResponse(
    val status: Int,
    val message: String,
    val responseValue: List<EmployeeActivity>
)

data class ActivityModel(
    val id: Int,
    val activityName: String,
    val category: String
)
data class ActivityResponse(
    val status: Int,
    val message: String,
    val responseValue: List<ActivityModel>
)