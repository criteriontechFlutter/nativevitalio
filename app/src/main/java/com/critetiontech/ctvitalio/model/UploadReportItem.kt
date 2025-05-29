package com.critetiontech.ctvitalio.model

data class UploadReportItem(
    val title: String,
    val iconResId: Int,
    val filePath: String, // Path to the file (image or PDF)
    val category: String // Category (Radiology, Imaging, Lab, etc.)
)