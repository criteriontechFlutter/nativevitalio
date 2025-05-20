package com.criterion.nativevitalio.model

data class OtpResponse(
    val status: Int,
    val message: String,
    val responseValue: String,
    val isRegisterd: Int
)