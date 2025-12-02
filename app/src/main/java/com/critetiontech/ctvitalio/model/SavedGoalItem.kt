package com.critetiontech.ctvitalio.model

data class GoalCategoryResponse(
    val id: Int,
    val categoryId: Int,
    val categoryName: String,
    val goals: List<GoalItem>
)
data class SmartGoalResponse(
    val status: Int,
    val message: String,
    val responseValue: List<GoalCategoryResponse>
)

data class GoalItem(
    val goalId: Int,
    val goalName: String,
    val description: String,
    val targetValue: String,
    val isActive: Int,
    var isPinned: Int
)
