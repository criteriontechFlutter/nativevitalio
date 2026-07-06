package com.critetiontech.ctvitalio.model

import com.google.gson.annotations.SerializedName

data class InsertMindfulnessResponse(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String,
    @SerializedName("responseValue") val responseValue: String?
)
