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