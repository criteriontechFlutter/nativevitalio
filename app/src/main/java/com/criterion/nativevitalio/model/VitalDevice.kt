package com.criterion.nativevitalio.model
data class VitalDevice(
    val id: Int,
    val isAutoConnect: Boolean,
    val name: String,
    val modal: String,
    val deviceType: String,
    val image: String,
    val device: String,
    val suuid: String,
    val cuuid: String,
    val isPairing: Boolean,
    val dataType: List<String>
)