package com.criterion.nativevitalio.model
data class AllergyResponse(
    val responseValue: List<AllergyGroup>
)

data class AllergyGroup(
    val parameterName: String,       // "Drug Allergies", "Food Allergies"
    val jsonHistory: String,         // JSON array of AllergyHistoryItem
)

data class AllergyHistoryItem(
    val rowId: Int,
    val remark: String,
    val substance: String,
    val severityLevel: String,
    val isFromPatient: Int,
    val category: String? = null     // ← "Drug Allergies", only for the first item in each group
)
data class AllergyApiResponse<T>(
    val status: Int,
    val message: String,
    val responseValue: T
)