package com.criterion.nativevitalio.model
data class AllergyResponse(
    val responseValue: List<AllergyGroup>
)

data class AllergyGroup(
    val parameterName: String,       // "Drug Allergies", "Food Allergies"
    val jsonHistory: String,         // JSON array of AllergyHistoryItem
)

data class AllergyHistoryItem(
    val rowId: Int?,
    val substance: String?,           // ✅ make nullable
    val remark: String?,
    val severityLevel: String?,
    val category: String? = null      // For UI grouping
)
data class AllergyApiResponse<T>(
    val status: Int,
    val message: String,
    val responseValue: T
)