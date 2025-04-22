package com.critetiontech.ctvitalio.viewmodel

import Patient
import PrefsManager
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.viewmodel.BaseViewModel
import com.critetiontech.ctvitalio.UI.Login
import com.critetiontech.ctvitalio.model.BaseResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.MyApplication
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class OtpViewModal  (application: Application) : BaseViewModel(application){

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    fun getPatientDetailsByUHID(uhid: String,deviceToken: String,otp:String) {

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
                    getPatientDetailsByUHID(uhid)

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

    private fun getPatientDetailsByUHID(uhid: String) {
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
                        Login.storedUHID=it.uhID
                        val intent = Intent(MyApplication.appContext, com.critetiontech.ctvitalio.UI.Home::class.java)
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        MyApplication.appContext.startActivity(intent)
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
}