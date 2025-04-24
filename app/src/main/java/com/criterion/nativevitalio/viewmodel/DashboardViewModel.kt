package com.critetiontech.ctvitalio.viewmodel

import PillReminderModel
import PillTime
import PrefsManager
import Vital
import VitalsResponse
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.utils.ConfirmationBottomSheet
import com.criterion.nativevitalio.viewmodel.BaseViewModel
import com.critetiontech.ctvitalio.model.DietItemModel
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                    _pillList.postValue(list)
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
            "text" to transcript,
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
                    overrideBaseUrl="http://food.shopright.ai:3478/api/",


                ).dynamicRawPost(
                    url = "echo/",
                    body = requestBody
                )

                if (response.isSuccessful) {
                    val body = response.body()?.string()
                    Log.d("API_RESPONSE", "data is $body")
                    var addedData: String = ""
                    val parsedJson = JSONObject(body)
                    val myVital = parsedJson.getJSONObject("echo").getJSONObject("myvital")
                    val vitalMap = jsonObjectToMap(myVital)
                    if (
                        myVital["vmValueTemperature"].toString() != "0.0" ||
                        myVital["vmValueRespiratoryRate"].toString() != "0" ||
                        myVital["vmValueRbs"].toString() != "0" ||
                        myVital["vmValueHeartRate"].toString() != "0" ||
                        myVital["vmValueSPO2"].toString() != "0" ||
                        myVital["vmValuePulse"].toString() != "0" ||
                        myVital["vmValueBPDias"].toString() != "0" ||
                        myVital["vmValueBPSys"].toString() != "0" ||
                        myVital["weight"].toString() != "0"
                    ) {
                        addedData=  addedData + vitalName(vitalMap) ;

                    }


                    val symptomsList = vitalMap["symptomsList"] as? List<Map<String, Any>> ?: emptyList()

                    if (symptomsList.isNotEmpty()) {
//                        val symptomNames = symptomsList.mapNotNull {
//
//                            it["symptom"]?.toString()?  }
//                        addedData += symptomNames.joinToString(", ") + ", "
                    }

                    val fluidValue = myVital["fluidValue"] as? Map<String, Any> ?: emptyMap()
                    val fluidList = fluidValue.keys.toList()
                    if (fluidList.isNotEmpty()) {

                        val temp = listOf(
                            mapOf("name" to "water", "id" to "97694"),
                            mapOf("name" to "milk", "id" to "76"),
                            mapOf("name" to "green tea", "id" to "114973"),
                            mapOf("name" to "coffee", "id" to "168"),
                            mapOf("name" to "fruit juice", "id" to "66")
                        )

                        for (i in fluidList.indices) {
                            val fluidName = fluidList[i].toString()
                            val fluidAmount = fluidValue[fluidList[i]].toString()
                            addedData += "$fluidName $fluidAmount ml, "
                        }
                    }




                    val foodList = (myVital["foodIntakeList"] as? List<Map<String, Any?>>) ?: emptyList()

                    if (foodList.isNotEmpty()) {
                        for (food in foodList) {
                            val foodName = food["foodName"].toString()
                            addedData += "$foodName, "
                        }
                    }




                    val medicationList = (myVital["myMedication"] as? List<Map<String, Any?>>) ?: emptyList()

                    if (medicationList.isNotEmpty()) {
                        for (medication in medicationList) {
                            val drugName = medication["drugName"].toString()
                            addedData += "$drugName, "
                        }
                    }
                    if (addedData.isNotEmpty()) {
                        // Remove the trailing comma and space
                        addedData = addedData.removeSuffix(", ")

                        val bottomSheet = ConfirmationBottomSheet(
                            message = "Are you sure you want to save $addedData?",
                            onConfirm = {

                                val hasVitals = listOf(
                                    "vmValueTemperature",
                                    "vmValueRespiratoryRate",
                                    "vmValueRbs",
                                    "vmValueHeartRate",
                                    "vmValueSPO2",
                                    "vmValuePulse",
                                    "vmValueBPDias",
                                    "vmValueBPSys",
                                    "weight"
                                ).any { key ->
                                    myVital[key]?.toString() != "0" && myVital[key]?.toString() != "0.0"
                                }


                                if (hasVitals) {
                                    insertPatientVital(
                                        BPSys = data["vmValueBPSys"].toString(),
                                        BPDias = data["vmValueBPDias"].toString(),
                                        rr = data["vmValueRespiratoryRate"].toString(),
                                        spo2 = data["vmValueSPO2"].toString(),
                                        pr = data["vmValuePulse"].toString(),
                                        tmp = data["vmValueTemperature"].toString(),
                                        hr = data["vmValueHeartRate"].toString(),
                                        weight = data["weight"].toString(),
                                        rbs = data["vmValueRbs"].toString(),
                                        positionId = "129"
                                    )
                                }






                                // Place your save logic or API call here
                                Toast.makeText(context, "$addedData saved successfully!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "ConfirmSheet")
                    }


                    // Parse body and update your ViewModel/UI accordingly
                } else {
                    Log.e("VoicePost", "Failed: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("VoicePost", "Exception: ${e.message}")
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




}
