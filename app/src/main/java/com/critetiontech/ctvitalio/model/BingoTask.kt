package com.critetiontech.ctvitalio.model

data class BingoTask(
    val id: Int,
    val emoji: String,
    val title: String,
    var completed: Boolean = false
)
