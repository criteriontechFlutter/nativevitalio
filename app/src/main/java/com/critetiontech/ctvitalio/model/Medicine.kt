package com.critetiontech.ctvitalio.model
data class Medicine(
    val id: Int,
    val pid: Int,
    val medicinename: String,
    val dosageType: String,
    val time: String,
    val dosageStrength: Double,
    val frequency: String,
    val remark: String,
    val startdate: String,
    val enddate: String,
    val clientId: Int,
    val status: Int,
    val createdDate: String,
    var isTaken: Boolean = false // UI state for "Taken"
)
