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
import com.critetiontech.ctvitalio.model.Medicine
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class PillsReminderViewModal (application: Application) : BaseViewModel(application) {

    private val _pillList = MutableLiveData<List<Medicine>>()
    val pillList: LiveData<List<Medicine>> get() = _pillList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
     fun getAllPatientMedication( ) {
        _loading.value = true

        viewModelScope.launch {
            try {


                val queryParams = mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString(),
                    "clientId" to 194
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
                    Log.d("RESPONSE", "responseValue: $json")

                    try {
                        val jsonObject = JSONObject(json ?: "")
                        val status = jsonObject.optInt("status")
                        val message = jsonObject.optString("message")

                        if (status == 1) {
                            val responseArray = jsonObject.optJSONArray("responseValue")

                            // Parse JSON array to List<Medicine>
                            if (responseArray != null && responseArray.length() > 0) {
                                val gson = Gson()
                                val type = object : TypeToken<List<Medicine>>() {}.type
                                val medicines: List<Medicine> = gson.fromJson(responseArray.toString(), type)

                                _pillList.postValue(medicines)
                                Log.d("PILLS_LIST", "Parsed medicines: $medicines")
                            } else {
                                _pillList.postValue(emptyList())
                                Log.d("PILLS_LIST", "Empty responseValue array")
                            }
                        } else {
                            _pillList.postValue(emptyList())
                            Log.d("PILLS_LIST", "No record found: $message")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _errorMessage.postValue("Error parsing response")
                    }
                } else {
                    _errorMessage.postValue("Error: ${response.code()}")
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }




//    @RequiresApi(Build.VERSION_CODES.O)
//    fun insertPatientMedication(
//        pmID:String,
//        prescriptionID:String,
//        durationType:String,
//        compareTime: String
//    ) {
//        _loading.value = true
//
//        viewModelScope.launch {
//            try {
//                val convertedTime = convertTo24Hour(compareTime)
//
//                val queryParams = mapOf(
//                    "UhID" to PrefsManager().getPatient()?.empId.toString(),
//                    "pmID"  to  pmID,
//                    "intakeDateAndTime"  to    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())+" "+convertedTime,
//
//
//
//                    "prescriptionID" to prescriptionID,
//                    "userID"  to PrefsManager().getPatient()?.id.toString(),
//                "duration"  to  durationType ,
//                "compareTime"  to  convertedTime
//                )
//
//                // This response is of type Response<ResponseBody>
//                val response = RetrofitInstance
//                    .createApiService( )
//                    .dynamicRawPost(
//                        url = ApiEndPoint().insertPatientMedication,
//                        body = queryParams
//                    )
//                getAllPatientMedication()
//                _loading.value = false
//                if (response.isSuccessful) {
//                    val json = response.body()?.string()
//                    Log.d("RESPONSE", "responseValue: $json")
//
//                } else {
//                    _errorMessage.value = "Error: ${response.code()}"
//                }
//
//            } catch (e: Exception) {
//                _loading.value = false
//                _errorMessage.value = e.message ?: "Unknown error occurred"
//                e.printStackTrace()
//            }
//        }
//    }



}
