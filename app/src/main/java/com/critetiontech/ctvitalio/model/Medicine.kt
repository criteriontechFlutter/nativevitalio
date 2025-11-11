package com.critetiontech.ctvitalio.model

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Medicine(
    val medicineIntakeId: Int?,
    val dosageType: String?,
    val dosageStrength: Double?,
    val instructions: String?,
    val medicineName: String?,
    val unit: String?,
    val scheduledDateTime: String?,
    var isTaken: Int?,
    val takenDateTime: String?,
    val isSkipped: Int?,
    val dayPeriod: String? = null // ✅ added field
)
data class DayWiseMedicines(
    val dayPeriod: String,
    val medicineData: List<Medicine>
)
data class MedicineSchedule(
    val dayPeriod: String?,
    val medicineData: List<Medicine>// Stringified JSON array
) {
    // Helper to parse medicines string into a List<Medicine>
//    fun getMedicinesList(): List<Medicine> {
//        return try {
//            val type = object : TypeToken<List<Medicine>>() {}.type
//            Gson().fromJson(medicineData, type)
//        } catch (e: Exception) {
//            emptyList()
//        }
//    }
}