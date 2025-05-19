package com.criterion.nativevitalio.model

data class VitalReminder(
    val parameterId: Int,
    val parameterTypeId: Int,
    val name: String,
    var quantity: Int,
    var frequencyType: String,
    var isCheck: Boolean
)