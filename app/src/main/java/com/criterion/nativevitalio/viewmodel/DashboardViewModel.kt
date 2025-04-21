package com.criterion.nativevitalio.viewmodel

import PillReminderModel
import PillTime
import PrefsManager
import Vital
import VitalsResponse
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.model.DietItemModel
import com.criterion.nativevitalio.networking.RetrofitInstance
import com.criterion.nativevitalio.utils.ApiEndPoint
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel : ViewModel() {

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

                _loading.value = false

                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, VitalsResponse::class.java)
                    _vitalList.value = parsed.responseValue
                } else {
                    _errorMessage.value = "Error Code: ${response.code()}"
                }

            } catch (e: Exception) {
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
//            "medication" to medicationList,
            "foodIntakeList" to foodList
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
                    Log.d("VoicePost", "Response: $body")
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
}
