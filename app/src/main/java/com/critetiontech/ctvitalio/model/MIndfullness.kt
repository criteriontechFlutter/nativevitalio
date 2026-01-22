package com.critetiontech.ctvitalio.model

data class BreathingProtocol(
    val id: Int,
    val title: String,
    val description: String,
    val duration: String,
    val benefits: List<String>,
    val icon: Int,
    val hrIncrease: Boolean
)

data class EyeExercise(
    val id: Int,
    val title: String,
    val description: String,
    val duration: String,
    val reps: String,
    val benefits: List<String>,
    val icon: Int
)