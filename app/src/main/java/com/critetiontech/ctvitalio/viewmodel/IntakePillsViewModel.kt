package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class IntakePillsViewModel :ViewModel() {
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

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
                    "intakeDateAndTime"  to    java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())+" "+convertedTime,



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

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertTo24Hour(input: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        val outputFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)

        val time = LocalTime.parse(input.trim(), inputFormatter)
        return time.format(outputFormatter)
    }
}