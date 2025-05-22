package com.critetiontech.ctvitalio.model

data class BotMessage(
    val message: String,
    val isUser: Boolean,
    val options: List<String>? = null
)