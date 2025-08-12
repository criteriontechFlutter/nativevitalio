package com.critetiontech.ctvitalio.viewmodel

import PillReminderModel
import PillTime
import PrefsManager
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class PillsReminderViewModal (application: Application) : BaseViewModel(application) {

    private val _pillList = MutableLiveData<List<PillReminderModel>>()
    val pillList: LiveData<List<PillReminderModel>> get() = _pillList
    private val _currentDatePillList = MutableLiveData<List<PillReminderModel>>()
    val currentDatePillList: LiveData<List<PillReminderModel>> get() = _currentDatePillList
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
     fun getAllPatientMedication( ) {
        _loading.value = true

        viewModelScope.launch {
            try {


                val queryParams = mapOf(
                    "UhID" to PrefsManager().getPatient()?.empId.toString()
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
                    _currentDatePillList.postValue(todaysMedications)
                    _pillList.postValue(list)
                    Log.d("RESPONSE", "responseValue: $todaysMedications")

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


    @RequiresApi(Build.VERSION_CODES.O)
    fun convertTo24Hour(time: String): LocalTime {
        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        return LocalTime.parse(time.trim(), formatter)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun insertPatientMedication(
        pmID:String,
        prescriptionID:String,
        durationType:String,
        compareTime: String
    ) {
        _loading.value = true

        viewModelScope.launch {
            try {
                val convertedTime = convertTo24Hour(compareTime)

                val queryParams = mapOf(
                    "UhID" to PrefsManager().getPatient()?.empId.toString(),
                    "pmID"  to  pmID,
                    "intakeDateAndTime"  to    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())+" "+convertedTime,



                    "prescriptionID" to prescriptionID,
                    "userID"  to PrefsManager().getPatient()?.id.toString(),
                "duration"  to  durationType ,
                "compareTime"  to  convertedTime
                )

                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService( )
                    .dynamicRawPost(
                        url = ApiEndPoint().insertPatientMedication,
                        body = queryParams
                    )
                getAllPatientMedication()
                _loading.value = false
                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    Log.d("RESPONSE", "responseValue: $json")

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
