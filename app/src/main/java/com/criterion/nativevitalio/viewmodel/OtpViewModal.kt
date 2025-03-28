package com.criterion.nativevitalio.viewmodel

import Patient
import PrefsManager
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.UI.Home
import com.criterion.nativevitalio.UI.Login
import com.criterion.nativevitalio.Utils.ApiEndPoint
import com.criterion.nativevitalio.Utils.MyApplication
import com.criterion.nativevitalio.model.BaseResponse
import com.criterion.nativevitalio.networking.RetrofitInstance
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class OtpViewModal  :ViewModel(){

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    fun getPatientDetailsByUHID(uhid: String,deviceToken: String,otp:String) {
        _loading.value = true

        viewModelScope.launch {
            try {

//                UHID=${uhid}&otp=${otpC.value.text.toString()}&deviceToken=${token}&ifLoggedOutFromAllDevices=0",

                val queryParams = mapOf(
                    "otp" to otp,
                    "UHID" to uhid,
                    "deviceToken" to deviceToken,
                    "ifLoggedOutFromAllDevices" to  "0"

                )

                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService( )
                    .dynamicGet(
                        url = ApiEndPoint().verifyLogInOTPForSHFCApp,
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val responseBodyString = response.body()?.string()
                    getPatientDetailsByUHID(uhid)




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


                    val type = object : TypeToken<BaseResponse<List<Patient>>>() {}.type
                    val parsed = Gson().fromJson<BaseResponse<List<Patient>>>(responseBodyString, type)

                    Log.d("RESPONSE", "responseValue: ${Gson().toJson(parsed.responseValue)}")
                    val firstPatient = parsed.responseValue.firstOrNull()


                    firstPatient?.let {
                        PrefsManager( ).savePatient(it)
                        Login.storedUHID=it.uhid
                        val intent = Intent(MyApplication.appContext, com.criterion.nativevitalio.UI.Home::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        MyApplication.appContext.startActivity(intent)

                        Log.d("RESPONSE", "Full Patients: ${PrefsManager().getPatient()?.uhid.toString()}")
                    }


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