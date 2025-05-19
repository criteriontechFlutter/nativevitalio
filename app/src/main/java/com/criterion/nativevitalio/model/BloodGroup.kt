package com.criterion.nativevitalio.model

data class BloodGroup(
    val id: Int,
    val groupName: String,
    val status: Int,
    val userId: Int,
    val createdDate: String
)