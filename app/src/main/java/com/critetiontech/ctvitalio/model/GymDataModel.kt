package com.critetiontech.ctvitalio.model

data class GymResponse(
    val status: Int,
    val message: String,
    val responseValue: List<Gym>
)

data class Gym(
    val gymName: String,
    val description: String,
    val contactNumber: String,
    val email: String,
    val address: String,
    val city: String,
    val state: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val imageURL: String
)