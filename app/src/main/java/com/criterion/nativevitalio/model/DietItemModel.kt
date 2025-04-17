package com.criterion.nativevitalio.model

data class DietItemModel(
    val dietId: Int,
    val foodId: Int,
    val foodQty: Double,
    val foodUnitID: Int,
    val foodName: String,
    val unitName: String,
    val foodEntryDate: String,
    val foodEntryTime: String,
    val foodGivenAt: String,
    val isGiven: Int
)

sealed class DietListItem {
    data class Header(val title: String) : DietListItem()
    data class Item(val diet: DietItemModel) : DietListItem()
}