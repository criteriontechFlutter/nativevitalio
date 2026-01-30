package com.critetiontech.ctvitalio.model

data class AllergyTypeItem(
    val subCategoryId: Int,
    val subCategoryName: String,
    val categoryId: Int,
    val categoryName: String,
    val historyParameterAssignId: Int,  // ✅ Make sure this field exists
    val parameterId: Int,
    val parameterName: String,
    val apiUrl: String,
    // Add other properties if needed...
)
data class UnitConfig(
    val step: Double,
    val min: Double = 0.0,
    val max: Double? = null,
    val allowDecimal: Boolean = false
)
