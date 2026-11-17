package com.critetiontech.vitalioCorporate.model

data class BingoTask(
    val id: Int,
    val emoji: String,
    val title: String,
    var completed: Boolean = false
)
