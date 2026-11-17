package com.critetiontech.vitalioCorporate.model

data class BotMessage(
    val message: String,
    val isUser: Boolean,
    val options: List<String>? = null
)