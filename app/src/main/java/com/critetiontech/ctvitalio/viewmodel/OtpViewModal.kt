package com.critetiontech.ctvitalio.viewmodel

import Patient
import PrefsManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.UI.Login
import com.critetiontech.ctvitalio.UI.Home
import com.critetiontech.ctvitalio.UI.SignupActivity
import com.critetiontech.ctvitalio.model.BaseResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.ToastUtils

import com.critetiontech.ctvitalio.viewmodel.BaseViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class OtpViewModal  (application: Application) : BaseViewModel(application){

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    fun getPatientDetailsByUHID(uhid: String,
                                deviceToken: String,otp:String,
                                isRegistered: String="",context:Context,
                                mNo:String) {

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
                    if(isRegistered.toString()=="0"){

                        if(uhid.isEmpty()){
                            Log.d("RESPONSE", "phoneOrUHID5$mNo")
                            val intent =
                                Intent(MyApplication.appContext, SignupActivity::class.java).apply {
                                    putExtra("UHID", uhid)
                                    putExtra("mobileNo", mNo)
                                }
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            MyApplication.appContext.startActivity(intent)
                        }else{
                            Toast.makeText(context, "Enter valid uhid", Toast.LENGTH_SHORT).show()
                        }



                    }else{
                    getPatientDetailsByUHIDs(uhid,context)
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

    private fun getPatientDetailsByUHIDs(uhid: String,context: Context) {
        _loading.value = true

        viewModelScope.launch {
            try {
                var mo = ""
                var uhidVal = ""

                if (uhid.toLowerCase().contains("uhid")) {
                    uhidVal = uhid
                } else {
                    mo = uhid
                }
                val queryParams = mapOf(
                    "mobileNo" to mo,
                    "uhid" to uhidVal,
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