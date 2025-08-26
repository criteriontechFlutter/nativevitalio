package com.critetiontech.ctvitalio.model
data class SetGoalModel(
    val icon: Int,
    val name: String,
    val vmId: Int,
    val unit: String,
    val description: String,
    val normalValue: Int,
    var selectedGoal : Int,
    var isSelected: Boolean
)