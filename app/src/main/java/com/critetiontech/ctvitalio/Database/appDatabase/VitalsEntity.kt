package com.critetiontech.ctvitalio.Database.appDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vitals_table")
data class VitalsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    val lastVitalJson: String,
    val insightsJson: String,
    val sleepMetricJson: String?
)
