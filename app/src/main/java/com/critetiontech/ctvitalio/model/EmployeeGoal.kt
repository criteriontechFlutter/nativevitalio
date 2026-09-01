package com.critetiontech.ctvitalio.model

/**
 * EmployeeGoal.kt
 *
 * Represents a single smart goal assigned to or chosen by an employee.
 * Moved out of PrefsManager.kt into the correct model/ package.
 */
data class EmployeeGoal(
    val pid: Int,
    val vmId: Int,
    val goalId: Int,
    val vitalName: String,
    val targetValue: Int?,
    val unit: String
)
