package com.critetiontech.ctvitalio.viewmodel

import PillReminderModel
import PillTime
import PrefsManager
import Vital
import VitalsResponse
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.utils.ConfirmationBottomSheet
import com.criterion.nativevitalio.viewmodel.BaseViewModel
import com.critetiontech.ctvitalio.model.DietItemModel
import com.critetiontech.ctvitalio.model.SymptomDetail
import com.critetiontech.ctvitalio.model.SymptomResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.MyApplication
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HoldSpeakSymptomDetail(
    val pdmID: Int,
    val details: String
)
enum class WebSocketState {
    CONNECTING, CONNECTED, DISCONNECTED, ERROR
}
class DashboardViewModel(application: Application) : BaseViewModel(application) {

    private val _vitalList = MutableLiveData<List<Vital>>()
    val vitalList: LiveData<List<Vital>> get() = _vitalList

    private val _dietList = MutableLiveData<List<DietItemModel>>()
    val dietList: LiveData<List<DietItemModel>> get() = _dietList
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _pillList = MutableLiveData<List<PillReminderModel>>()
    val pillList: LiveData<List<PillReminderModel>> get() = _pillList


    private val _webSocketStatus = MutableLiveData<WebSocketState>()
    val webSocketStatus: LiveData<WebSocketState> get() = _webSocketStatus

    fun setWebSocketState(state: WebSocketState) {
        _webSocketStatus.postValue(state)
    }
    fun getVitals() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val uhid = PrefsManager().getPatient()?.uhID.orEmpty()

                val queryParams = mapOf(
                    "uhID" to uhid
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicGet(
                        url = ApiEndPoint().getPatientLastVital,
                        params = queryParams
                    )



                if (response.isSuccessful) {
                    _loading.value = false
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, VitalsResponse::class.java)
                    _vitalList.value = parsed.responseValue
                } else {
                    _loading.value = false
                    _errorMessage.value = "Error Code: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _loading.value = false
                _errorMessage.value = e.message ?: "Unexpected error"
                e.printStackTrace()
            }
        }
    }


    fun getAllPatientMedication( ) {
        _loading.value = true

        viewModelScope.launch {
            try {


                val queryParams = mapOf(
                    "UhID" to PrefsManager().getPatient()?.uhID.toString()
                )
                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService( )
                    .dynamicGet(
                        url = ApiEndPoint().getAllPatientMedication,
                        params = queryParams
                    )
                _loading.value = false
                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val list = parseMedicationNameAndDateList(json)
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                        Date()
                    )

// Filter the list to only include today's medications
                    val todaysMedications = list.filter { it.date == currentDate }
                    _pillList.postValue(todaysMedications)
                    Log.d("RESPONSE", "responseValue: $_pillList")

                } else {
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }
    fun getFoodIntake(date:  String?) {
        _loading.value = true
        val finalDate = if (date.isNullOrBlank()) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        } else {
            date
        }

        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "Uhid" to PrefsManager().getPatient()?.uhID.toString(),
                    "entryType" to "D",
                    "fromDate" to finalDate,
                )

                val response = RetrofitInstance
                    .createApiService7096(includeAuthHeader=true)
                    .dynamicGet(
                        url = ApiEndPoint().getFoodIntake,
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val rootObj = Gson().fromJson(json, JsonObject::class.java)
                    val listJson = rootObj.getAsJsonArray("foodIntakeList")

                    val type = object : TypeToken<List<DietItemModel>>() {}.type
                    val parsedList: List<DietItemModel> = Gson().fromJson(listJson, type)

                    _dietList.postValue(parsedList)
                } else {
                    _dietList.postValue(emptyList())
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _dietList.postValue(emptyList())
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }
    private fun parseMedicationNameAndDateList(json: String?): List<PillReminderModel> {
        val result = mutableListOf<PillReminderModel>()
        if (json == null) return result

        val root = JSONObject(json)
        val medArray = root.getJSONObject("responseValue").getJSONArray("medicationNameAndDate")

        for (i in 0 until medArray.length()) {
            val obj = medArray.getJSONObject(i)
            val jsonTime = JSONArray(obj.getString("jsonTime"))

            val times = mutableListOf<PillTime>()
            for (j in 0 until jsonTime.length()) {
                val timeObj = jsonTime.getJSONObject(j)
                times.add(
                    PillTime(
                        time = timeObj.optString("time"),
                        durationType = timeObj.optString("durationType"),
                        icon = timeObj.optString("icon"),
                        intakeTime = timeObj.optString("intakeTime")
                    )
                )
            }

            result.add(
                PillReminderModel(
                    prescriptionRowID = obj.optInt("prescriptionRowID"),
                    pmId = obj.optInt("pmId"),
                    date = obj.optString("date"),
                    drugName = obj.optString("drugName"),
                    dosageForm = obj.optString("dosageForm"),
                    frequency = obj.optString("frequency"),
                    doseFrequency = obj.optString("doseFrequency"),
                    remark = obj.optString("remark"),
                    medicineId = obj.optInt("medicineId"),
                    drugId = obj.optInt("drugId"),
                    jsonTime = times,
                    translation = obj.optString("translation")
                )
            )
        }

        return result
    }





    fun postAnalyzedVoiceData(context: Context, transcript: String) {
        val patient = PrefsManager().getPatient() ?: return
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        getAllPatientMedication()
        val medicationList = listOf(
            mapOf(
                "drugName" to "Paracetamol",
                "medicationNameAndDate" to listOf(
                    mapOf("date" to currentDate, "time" to "08:00", "status" to "taken")
                )
            )
        )

        val foodList = listOf(
            mapOf("foodName" to "Rice", "dietId" to "1"),
            mapOf("foodName" to "Soup", "dietId" to "2")
        )

        val data = mapOf(
            "text" to "fever pulse rate 74 water 100 ml urine 100 ml output 74 ml ",
//            "text" to transcript,
            "userid" to patient.id.toString(),
            "uhid" to patient.uhID.toString(),
            "date" to currentDate,
            "time" to currentTime,
            "clientID" to 1,
            "medication" to listOf(
                mapOf(
                    "drugName" to emptyList<Any>(),
                    "medicationNameAndDate" to emptyList<Any>()
                )
            ),
            "foodIntakeList" to emptyList<Any>()
        )

        val requestBody = mapOf("text" to data)
        val json = JSONObject(requestBody).toString()

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.createApiService(
                    overrideBaseUrl=RetrofitInstance.shopright


                ).dynamicRawPost(
                    url = "echo/",
                    body = requestBody
                )

                if (response.isSuccessful) {
                    val body = response.body()?.string()
                    Log.d("API_RESPONSE", "data is $body")

                    val parsedJson = JSONObject(body)
                    val myVital = parsedJson.getJSONObject("echo").getJSONObject("myvital")

                    var addedData = ""

                    // Check vitals and construct summary
                    val vitalKeys = listOf(
                        "vmValueTemperature", "vmValueRespiratoryRate", "vmValueRbs",
                        "vmValueHeartRate", "vmValueSPO2", "vmValuePulse",
                        "vmValueBPDias", "vmValueBPSys", "weight"
                    )

                    val hasVitals = vitalKeys.any { key ->
                        val value = myVital.optString(key, "0")
                        value != "0" && value != "0.0"
                    }

                    if (hasVitals) {
                        vitalKeys.forEach { key ->
                            val value = myVital.optString(key, "0")
                            if (value != "0" && value != "0.0") {
                                val name = key.removePrefix("vmValue").replace("BPSys", "BP Systolic")
                                    .replace("BPDias", "BP Diastolic")
                                    .replace("Rbs", "RBS")
                                    .replace("Pulse", "Pulse Rate")
                                    .replace("HeartRate", "Heart Rate")
                                    .replace("SPO2", "SpO2")
                                    .replace("RespiratoryRate", "Respiratory Rate")
                                    .replace("Temperature", "Temperature")
                                    .replace("weight", "Weight")
                                addedData += "$name $value, "
                            }
                        }
                    }

                    // Symptoms
                    val symptomsList = myVital.optJSONArray("symptomsList")
                    symptomsList?.let {
                        for (i in 0 until it.length()) {
                            val symptomObj = it.getJSONObject(i)
                            val symptomName = symptomObj.optString("symptom")
                            if (symptomName.isNotBlank()) {
                                addedData += "$symptomName, "
                            }
                        }
                    }

                    // Fluids
                    val fluidValue = myVital.optJSONObject("fluidValue")
                    fluidValue?.let {
                        val keys = fluidValue.keys()
                        while (keys.hasNext()) {
                            val key = keys.next()
                            val value = fluidValue.optDouble(key, 0.0)
                            if (value > 0.0) {
                                addedData += "$key $value ml, "
                            }
                        }
                    }

                    // Food Intake
                    val foodList = myVital.optJSONArray("foodIntakeList")
                    foodList?.let {
                        for (i in 0 until it.length()) {
                            val foodObj = it.getJSONObject(i)
                            val foodName = foodObj.optString("foodName")
                            if (foodName.isNotBlank()) {
                                addedData += "$foodName, "
                            }
                        }
                    }

                    // Medications
                    val medicationList = myVital.optJSONArray("myMedication")
                    medicationList?.let {
                        for (i in 0 until it.length()) {
                            val medObj = it.getJSONObject(i)
                            val drugName = medObj.optString("drugName")
                            if (drugName.isNotBlank()) {
                                addedData += "$drugName, "
                            }
                        }
                    }

                    if (addedData.isNotEmpty()) {
                        addedData = addedData.removeSuffix(", ")

                        val bottomSheet = ConfirmationBottomSheet(
                            message = "Are you sure you want to save $addedData?",
                            onConfirm = {
                                // Insert vitals
                                if (hasVitals) {
                                    insertPatientVital(
                                        BPSys = myVital.optString("vmValueBPSys", "0"),
                                        BPDias = myVital.optString("vmValueBPDias", "0"),
                                        rr = myVital.optString("vmValueRespiratoryRate", "0"),
                                        spo2 = myVital.optString("vmValueSPO2", "0"),
                                        pr = myVital.optString("vmValuePulse", "0"),
                                        tmp = myVital.optString("vmValueTemperature", "0"),
                                        hr = myVital.optString("vmValueHeartRate", "0"),
                                        weight = myVital.optString("weight", "0"),
                                        rbs = myVital.optString("vmValueRbs", "0"),
                                        positionId = "129"
                                    )
                                }

                                // Insert symptoms
                                symptomsList?.let {
                                    val symptomDetails = mutableListOf<HoldSpeakSymptomDetail>()
                                    for (i in 0 until it.length()) {
                                        val symp = it.getJSONObject(i)
                                        val id = symp.optInt("id", -1)
                                        val name = symp.optString("symptom")
                                        if (id != -1 && name.isNotBlank()) {
                                            symptomDetails.add(HoldSpeakSymptomDetail(id, name))
                                        }
                                    }
                                    if (symptomDetails.isNotEmpty()) insertSymptoms(symptomDetails)
                                }

                                // Insert fluid intake
                                fluidValue?.let {
                                    val fluidMap = mapOf(
                                        "water" to "97694",
                                        "milk" to "76",
                                        "green tea" to "114973",
                                        "coffee" to "168",
                                        "fruit juice" to "66"
                                    )
                                    for (key in fluidMap.keys) {
                                        val value = fluidValue.optDouble(key, 0.0)
                                        if (value > 0.0) {
                                            fluidIntake(context, fluidMap[key]!!, value.toString())
                                        }
                                    }
                                }

                                Toast.makeText(context, "$addedData saved successfully!", Toast.LENGTH_SHORT).show()
                            }
                        )
                        bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "ConfirmSheet")
                    }

                }  else {
                    Log.e("VoicePost", "Failed: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("VoicePost", "Exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }


    private val _patientSymptomList = MutableLiveData<List<SymptomDetail>>()
    val  patientSymptomList: LiveData<List<SymptomDetail>> get() = _patientSymptomList
    fun getSymptoms() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "uhID" to PrefsManager().getPatient()?.uhID.toString(),
                    "clientID" to PrefsManager().getPatient()?.clientId.toString(),
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .queryDynamicRawPost(
                        url = ApiEndPoint().getSymptoms,
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, SymptomResponse::class.java)
                    _patientSymptomList.value = parsed.responseValue
                } else {
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }
    fun jsonObjectToMap(json: JSONObject): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = json.opt(key)
        }
        return map
    }
    fun toCamelCase(input: String): String {
        return input.replaceFirstChar { it.uppercaseChar() }
    }

    fun vitalName(myVital: Map<String, Any?>): String {
        var vitalData = ""

        if (myVital["vmValueTemperature"].toString() != "0.0") {
            vitalData += "Temperature ${myVital["vmValueTemperature"]}, "
        }
        if (myVital["vmValueRespiratoryRate"].toString() != "0") {
            vitalData += "Respiratory Rate ${myVital["vmValueRespiratoryRate"]}, "
        }
        if (myVital["vmValueRbs"].toString() != "0") {
            vitalData += "RBS ${myVital["vmValueRbs"]}, "
        }
        if (myVital["vmValueHeartRate"].toString() != "0") {
            vitalData += "Heart Rate ${myVital["vmValueHeartRate"]}, "
        }
        if (myVital["vmValueSPO2"].toString() != "0") {
            vitalData += "SPO2 ${myVital["vmValueSPO2"]}, "
        }
        if (myVital["vmValuePulse"].toString() != "0") {
            vitalData += "Pulse Rate ${myVital["vmValuePulse"]}, "
        }
        if (myVital["vmValueBPDias"].toString() != "0") {
            vitalData += "BP Dias ${myVital["vmValueBPDias"]}, "
        }
        if (myVital["vmValueBPSys"].toString() != "0") {
            vitalData += "BP Sys ${myVital["vmValueBPSys"]}, "
        }
        if (myVital["weight"].toString() != "0") {
            vitalData += "Weight ${myVital["weight"]}, "
        }

        return vitalData
    }


    fun insertPatientVital(
        BPSys: String?= "0",
        BPDias: String?= "0",
        rr: String? = "0",
        spo2: String? = "0",
        pr: String? ="0",
        tmp: String? = "0",
        hr: String? ="0",
        weight: String? ="0",
        rbs: String? = "0",
        positionId:  String? = "0",
    ) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "userId" to 0,
                    "vmValueBPSys" to BPSys.toString(),
                    "vmValueBPDias" to BPDias.toString(),
                    "vmValueRespiratoryRate" to rr.toString(),
                    "vmValueSPO2" to spo2.toString(),
                    "vmValuePulse" to pr.toString(),
                    "vmValueTemperature" to tmp.toString(),
                    "vmValueHeartRate" to hr.toString(),
                    "weight" to weight.toString(),
                    "vmValueRbs" to rbs.toString(),
                    "vitalTime" to SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                    "vitalDate" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    "uhid" to PrefsManager().getPatient()?.uhID.toString(),
                    "currentDate" to  SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                    "clientId" to PrefsManager().getPatient()?.clientId.toString(),
                    "isFromPatient" to true,
                    "positionId" to positionId.toString()
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicRawPost(
                        url = ApiEndPoint().insertPatientVital,
                        body = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {

                } else {
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }

    fun insertSymptoms(selectedSymptoms: List<HoldSpeakSymptomDetail>) {
        _loading.value = true
        getSymptoms()
        viewModelScope.launch {
            try {
                val dtDataTable = mutableListOf<Map<String, String>>()

                // Get the current timestamp once
                val now = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"))
                } else {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                }

                // Populate data table from selected symptoms
                selectedSymptoms.forEach { symptom ->
                    dtDataTable.add(
                        mapOf(
                            "detailID" to symptom.pdmID.toString(),
                            "detailsDate" to now,
                            "details" to symptom.details,
                            "isFromPatient" to "1"
                        )
                    )
                }
                patientSymptomList.value?.forEach { symptom ->
                    dtDataTable.add(
                        mapOf(
                            "detailID" to symptom.detailID.toString(),
                            "detailsDate" to symptom.detailsDate ,
                            "details" to symptom.details,
                            "isFromPatient" to "1"
                        )
                    )
                }

                val queryParams = mapOf(
                    "uhID" to (PrefsManager().getPatient()?.uhID ?: ""),
                    "userID" to "0",
                    "doctorId" to "0",
                    "jsonSymtoms" to Gson().toJson(dtDataTable),
                    "clientID" to (PrefsManager().getPatient()?.clientId ?: "")
                )

                val response = RetrofitInstance
                    .createApiService()
                    .queryDynamicRawPost(
                        url = ApiEndPoint().insertSymtoms,
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val context = MyApplication.appContext
                    Toast.makeText(context, "Symptoms saved successfully!", Toast.LENGTH_SHORT).show()



                } else {
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }

    fun fluidIntake(context: Context, foodId: String, givenFoodQuantity: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val currentDateTime: String = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(Date())

                val user = PrefsManager().getPatient()
                val body = mapOf(
                    "givenQuanitityInGram" to "0",
                    "uhid" to user?.uhID.orEmpty(),
                    "foodId" to foodId,
                    "pmId" to "0",
                    "givenFoodQuantity" to givenFoodQuantity,
                    "givenFoodDate" to currentDateTime, // e.g. "2025-04-23 12:15"
                    "givenFoodUnitID" to "27",
                    "recommendedUserID" to "0",
                    "jsonData" to "",
                    "fromDate" to currentDateTime,
                    "isGiven" to "0",
                    "entryType" to "N",
                    "isFrom" to "0",
                    "dietID" to "0",
                    "userID" to user?.userId.toString()
                )

                val response = RetrofitInstance
                    .createApiService7096()
                    .dynamicRawPost(
                        url = ApiEndPoint().insertFoodIntake,
                        body = body
                    )

                _loading.value = false

                if (response.isSuccessful) {

                } else {
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }
}
