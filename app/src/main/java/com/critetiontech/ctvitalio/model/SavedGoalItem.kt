package com.critetiontech.ctvitalio.model

data class GoalCategoryResponse(
    val id: Int,
    val categoryId: Int,
    val categoryName: String,
    val goals: List<GoalItem>
)


data class GoalItem(
    val id: Int,
    val goalId: Int,
    val vmId: Int,
    val goalName: String,
    val description: String,
    val targetValue: String,
    val vmValue: String,
    val unit: String,
    val isActive: Int,
    var isPinned: Int
)


data class SmartGoalResponse(
    val status: Int,
    val message: String,
    val responseValue: List<GoalCategoryResponse>
)


data class SmartGoalResponseForAdded (

   var status        : Int?           = null,
   var message       : String?        = null,
    var responseValue : ResponseValue? = ResponseValue()

)


data class ResponseValue (

   var goalSummary   : String?                  = null,
   var employeeGoals : ArrayList<EmployeeGoals> = arrayListOf()

)

data class EmployeeGoals (

   var categoryId   : Int?             = null,
    var categoryName : String?          = null,
    var goals        : ArrayList<GoalItem> = arrayListOf()

)