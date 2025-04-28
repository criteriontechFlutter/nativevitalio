package com.criterion.nativevitalio.model

data class ProblemWithIcon(
    val problemId: Int,
    val problemName: String,
    val isVisible: Int,
    val displayIcon: String,
    val translation: String?
)