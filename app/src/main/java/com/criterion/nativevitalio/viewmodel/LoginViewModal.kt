package com.criterion.nativevitalio.viewmodel

import Patient
import PrefsManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.Utils.ApiEndPoint
import com.criterion.nativevitalio.model.BaseResponse
import com.criterion.nativevitalio.networking.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _movieResults = MutableLiveData<List<Patient>>()
    val movieResults: LiveData<List<Patient>> get() = _movieResults

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun getPatientDetailsByUHID(uhid: String) {
        _loading.value = true

        viewModelScope.launch {
            try {


                val queryParams = mapOf(
                    "mobileNo" to "",
                    "uhid" to uhid
                )

                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService( )
                    .dynamicGet(
                        url = ApiEndPoint().getPatientDetailsByMobileNo,
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val responseBodyString = response.body()?.string()
//                    val patient = Gson().fromJson(jsonString, Patient::class.java)
//                    PrefsManager().savePatient( )

                    val type = object : TypeToken<BaseResponse<List<Patient>>>() {}.type
                    val parsed = Gson().fromJson<BaseResponse<List<Patient>>>(responseBodyString, type)

                    Log.d("RESPONSE", "responseValue: ${Gson().toJson(parsed.responseValue)}")
                    val firstPatient = parsed.responseValue.firstOrNull()

                    if (firstPatient != null) {
                        Log.d("RESPONSE", "Full Patient: ${Gson().toJson(firstPatient)}")
                    } else {
                        Log.e("RESPONSE", "No patient data found.")
                    }
                    firstPatient?.let {
                        PrefsManager( ).savePatient(it)

                        Log.d("RESPONSE", "Full Patients: ${PrefsManager().getPatient()?.uhid.toString()}")
                    }
                    SentLogInOTPForSHFCApp(PrefsManager().getPatient()?.uhid.toString())
                    // Optionally parse it:
                    // val patient = Gson().fromJson(responseBodyString, Patient::class.java)
                    // _userData.value = patient
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
//    https://api.medvantage.tech:7082/api/LogInForSHFCApp/SentLogInOTPForSHFCApp?UHID=uhid01256&ifLoggedOutFromAllDevices=0

    fun SentLogInOTPForSHFCApp(uhid: String) {
        _loading.value = true

        viewModelScope.launch {
            try {


                val queryParams = mapOf(
                    "ifLoggedOutFromAllDevices" to "0",
                    "UHID" to uhid
                )

                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService( )
                    .dynamicGet(
                        url = "api/LogInForSHFCApp/SentLogInOTPForSHFCApp",
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val responseBodyString = response.body()?.string()
//                    val patient = Gson().fromJson(jsonString, Patient::class.java)
//                    PrefsManager().savePatient( )

                    Log.d("RESPONSE", "responseValue: ${responseBodyString }")
                    // Optionally parse it:
                    // val patient = Gson().fromJson(responseBodyString, Patient::class.java)
                    // _userData.value = patient
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