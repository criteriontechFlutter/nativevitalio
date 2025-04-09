package com.criterion.nativevitalio.model
data class SymptomResponse(
    val status: Int,
    val message: String,
    val responseValue: List<SymptomDetail>
)

data class SymptomDetail(
    val uhID: String,
    val pmID: Int,
    val pdmID: Int,
    val detailID: Int,
    val details: String,
    val detailsDate: String,
    val isProvisionalDiagnosis: Int,
    var selection: Int = -1
)