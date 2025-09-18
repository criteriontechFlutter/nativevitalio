package com.critetiontech.ctvitalio.model
data class Medicine(
    val time: String,
    val name: String,
    val dose: String,
    val frequency: String,
    val note: String,
    var isTaken: Boolean = false,  // Mark Taken state
    var isSkipped: Boolean = false // Skip state
)