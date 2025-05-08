package com.critetiontech.ctvitalio.model

data class CountryModel(
    val id: Int,
    val countryName: String,
    val status: Boolean,
    val userId: Int,
    val countryCode: String
)
data class StateModel(
    val name: String,
    val cities: List<String>
)data class ApiResponse<T>(
    val status: Int,
    val responseValue: T
)