package com.critetiontech.vitalioCorporate.model // or your correct package

data class BaseResponse<T>(
    val status: Int,
    val message: String,
    val responseValue: T
)