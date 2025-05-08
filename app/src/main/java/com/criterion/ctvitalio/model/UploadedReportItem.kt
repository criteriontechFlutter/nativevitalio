package com.critetiontech.ctvitalio.model

data class UploadedReportItem(
    val id: Int,
    val pmId: Int,
    val url: String,
    val category: String,
    val fileType: String,
    val fileName: String,
    val filePath: String?,
    val subCategory: String,
    val remark: String,
    val dateTime: String,
    val createdDate: String,
    val status: Int,
    val userId: Int,
    val clientID: Int,
    val uhid: String
)

// Response wrapper class
data class UploadedReportResponse(
    val status: Int,
    val message: String,
    val responseValue: List<UploadedReportItem>
)