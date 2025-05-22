package com.critetiontech.ctvitalio.model

data class VitalReminder(
    val parameterId: Int,
    val parameterTypeId: Int,
    val name: String,
    var quantity: Int,
    var frequencyType: String,
    var isCheck: Boolean
)