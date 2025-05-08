package com.criterion.nativevitalio.model

data class CountryModel(
    val id: Int,
    val countryName: String,
    val status: Boolean,
    val userId: Int,
    val countryCode: String
)
data class StateModel(
    val id: Int,
    val countryId: Int,
    val stateName: String,
    val status: Boolean,
    val userId: Int,
    val countryName: String,
    val clientId: Int
)


data class CityModel(
    val id: Int,
    val name: String,
    val stateId: Int,
    val stateName: String,
    val status: Boolean,
    val userId: Int,
    val clientId: Int
)
data class ApiResponse<T>(
    val status: Int,
    val responseValue: T
)