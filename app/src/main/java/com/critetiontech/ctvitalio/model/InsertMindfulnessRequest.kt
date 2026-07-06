package com.critetiontech.ctvitalio.model

import com.google.gson.annotations.SerializedName

data class InsertMindfulnessRequest(
    @SerializedName("pid") val pid: Int,
    @SerializedName("exerciseId") val exerciseId: Int,
    @SerializedName("duration") val duration: Int,
    @SerializedName("totalSteps") val totalSteps: Int,
    @SerializedName("mindfulnessJson") val mindfulnessJson: String,
    @SerializedName("userId") val userId: Int,
    @SerializedName("clientId") val clientId: Int
)
