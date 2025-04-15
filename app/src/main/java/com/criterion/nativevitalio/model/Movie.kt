package com.criterion.nativevitalio.model
data class Movies(
    val page: Int,
    val results: List<Result>,
    val total_pages: Int,
    val total_results: Int
)


data class FluidType(val name: String, val amount: Int, val color: Int,val id: Int,)



data class ManualFoodAssignResponse(
    val status: Int,
    val message: String,
    val responseValue: List<ManualFoodItem>
)

data class ManualFoodItem(
    val foodID: Int,
    val foodName: String,
    val quantity: String= 0.0.toString(),
    val givenFoodDate: String
)



data class GlassSize(
    val volume: Int, // e.g. 150
    var isSelected: Boolean = false
)


data class FluidPoint(
    val time: Float,     // in hours (e.g., 5.5 for 5:30 AM)
    val amount: Float,   // in ml
    val colorHex: String // color per fluid type (e.g., #3DA5F5)
)

