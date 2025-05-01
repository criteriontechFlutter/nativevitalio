package com.criterion.nativevitalio.viewmodel

import Patient
import PrefsManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.UI.Home
import com.criterion.nativevitalio.UI.Login
import com.criterion.nativevitalio.model.BaseResponse
import com.criterion.nativevitalio.networking.RetrofitInstance
import com.criterion.nativevitalio.utils.ApiEndPoint
import com.criterion.nativevitalio.utils.MyApplication
import com.criterion.nativevitalio.utils.ToastUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class OtpViewModal  (application: Application) : BaseViewModel(application){

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    fun getPatientDetailsByUHID(uhid: String,deviceToken: String,otp:String,context:Context) {

        _loading.value = true
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "otp" to otp,
                    "UHID" to uhid,
                    "deviceToken" to  PrefsManager().getDeviceToken().toString(),
                    "ifLoggedOutFromAllDevices" to  "0"
                )
                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService( )
                    .dynamicGet(
                        url = ApiEndPoint().verifyLogInOTPForVitalioApp,
                        params = queryParams
                    )



                if (response.isSuccessful) {
                    _loading.value = false
                    val responseBodyString = response.body()?.string()
                    getPatientDetailsByUHID(uhid,context)

                } else {
                    _loading.value = false
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {

                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }

    private fun getPatientDetailsByUHID(uhid: String,context: Context) {
        _loading.value = true

        viewModelScope.launch {
            try {

                val queryParams = mapOf(
                    "mobileNo" to "",
                    "uhid" to uhid,
                    "ClientId" to 194
                )

                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService( )
                    .dynamicGet(
                        url = ApiEndPoint().getPatientDetailsByMobileNo,
                        params = queryParams
                    )

                if (response.isSuccessful) {

                    _loading.value = false

                    val responseBodyString = response.body()?.string()

                    val type = object : TypeToken<BaseResponse<List<Patient>>>() {}.type
                    val parsed = Gson().fromJson<BaseResponse<List<Patient>>>(responseBodyString, type)
                    Log.d("RESPONSE", "responseValue: ${Gson().toJson(parsed.responseValue)}")
                    val firstPatient = parsed.responseValue.firstOrNull()


                    firstPatient?.let {
                        PrefsManager( ).savePatient(it)
                        Login.storedUHID=it
                        val intent = Intent(context, Home::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        Log.d("RESPONSE", "Full Patients: ${PrefsManager().getPatient()?.uhID.toString()}")
                    }


                } else {

                    _loading.value = false

                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }


    fun sentLogInOTPForSHFCApp(uhid: String, ifLoggedOutFromAllDevices: String = "0") {
        _loading.value = true
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "ifLoggedOutFromAllDevices" to ifLoggedOutFromAllDevices,
                    "UHID" to uhid
                )
                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService()
                    .dynamicGet(
                        url = ApiEndPoint().sentLogInOTPForVitalioApp,
                        params = queryParams
                    )


                if (response.isSuccessful) {
                    _loading.value = false
                   ToastUtils.showSuccess(MyApplication.appContext,"Otp Resent Successfully!")
                    Log.d("RESPONSE", "responseValue: ")
                } else {
                    _loading.value = false
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