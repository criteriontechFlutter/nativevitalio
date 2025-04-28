package com.criterion.nativevitalio.model

data class VitalPosition(
    val id: Int,
    val remark: String,
    val module: String,
    val status: Boolean,
    val createdDate: String
)