import org.json.JSONArray


fun parsePillReminderList(rawList: List<Map<String, Any>>): List<PillReminderModel> {
    return rawList.map { item ->
        val jsonTimeStr = item["jsonTime"] as? String ?: "[]"
        val pillTimeArray = JSONArray(jsonTimeStr)

        val times = mutableListOf<PillTime>()
        for (i in 0 until pillTimeArray.length()) {
            val timeObj = pillTimeArray.getJSONObject(i)
            times.add(
                PillTime(
                    time = timeObj.optString("time", ""),
                    durationType = timeObj.optString("durationType", ""),
                    icon = timeObj.optString("icon", ""),
                    intakeTime = timeObj.optString("intakeTime", "")
                )
            )
        }

        PillReminderModel(
            prescriptionRowID = (item["prescriptionRowID"] as? Number)?.toInt() ?: 0,
            pmId = (item["pmId"] as? Number)?.toInt() ?: 0,
            date = item["date"].toString(),
            drugName = item["drugName"].toString(),
            dosageForm = item["dosageForm"].toString(),
            frequency = item["frequency"].toString(),
            doseFrequency = item["doseFrequency"].toString(),
            remark = item["remark"].toString(),
            medicineId = (item["medicineId"] as? Number)?.toInt() ?: 0,
            drugId = (item["drugId"] as? Number)?.toInt() ?: 0,
            jsonTime = times,
            translation = item["translation"].toString()
        )
    }
}
data class PillTime(
    val time: String,
    val durationType: String,
    var icon: String,
    val intakeTime: String
)

data class PillReminderModel(
    val prescriptionRowID: Int,
    val pmId: Int,
    val date: String,
    val drugName: String,
    val dosageForm: String,
    val frequency: String,
    val doseFrequency: String,
    val remark: String,
    val medicineId: Int,
    val drugId: Int,
    val jsonTime: List<PillTime>,
    val translation: String
)
