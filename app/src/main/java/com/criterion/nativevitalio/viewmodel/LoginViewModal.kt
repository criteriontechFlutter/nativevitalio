package com.critetiontech.ctvitalio.viewmodel

import Patient
import PrefsManager
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.utils.ToastUtils
import com.critetiontech.ctvitalio.UI.Login
import com.critetiontech.ctvitalio.UI.otp
import com.critetiontech.ctvitalio.model.BaseResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.MyApplication
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class LoginViewModel : ViewModel() {

    private val _finishEvent = MutableLiveData<Boolean>()
    val finishEvent: LiveData<Boolean> get() = _finishEvent
    private val _showDialog = MutableLiveData<String?>()
    val showDialog: LiveData<String?> get() = _showDialog
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
                    .createApiService()
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
                        Login.storedUHID = it.uhID
                        sentLogInOTPForSHFCApp(it.uhID)
                        Log.d("RESPONSE", "Full Patients: ${PrefsManager().getPatient()?.uhID.toString()}"
                        )
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
                        url = ApiEndPoint().sentLogInOTPForSHFCApp,
                        params = queryParams
                    )

                _loading.value = false
                if (response.isSuccessful) {
                    val responseBodyString = response.body()?.string()
                    val intent = Intent(MyApplication.appContext, otp::class.java).apply {
                        putExtra("UHID", uhid)
                    }
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MyApplication.appContext.startActivity(intent)
                    Log.d("RESPONSE", "responseValue: $responseBodyString")
                } else {
                    _showDialog.postValue("Logout Confirmation")
                    _errorMessage.value = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }


    fun logoutFromApp(uhid: String, deviceToken: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "UHID" to uhid,
                    "deviceToken" to PrefsManager().getDeviceToken().toString()
                )

                val response = RetrofitInstance
                    .createApiService7082()
                    .dynamicGet(
                        url = ApiEndPoint().logoutFromApp,
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    PrefsManager().clearPatient()
                    val intent = Intent(MyApplication.appContext, Login::class.java)
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                    MyApplication.appContext.startActivity(intent)

                    _finishEvent.value = true
                } else {
                    val errorMsg = parseErrorMessage(response.errorBody())
                    ToastUtils.showFailure(MyApplication.appContext, errorMsg)
                    _errorMessage.value = "Logout failed: $errorMsg"
                }
            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                ToastUtils.showFailure(MyApplication.appContext, _errorMessage.value ?: "")
                e.printStackTrace()
            }
        }
    }


    fun parseErrorMessage(errorBody: ResponseBody?): String {
        return try {
            val gson = Gson()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val errorMap: Map<String, Any> = gson.fromJson(errorBody?.charStream(), type)
            errorMap["message"]?.toString() ?: "Something went wrong"
        } catch (e: Exception) {
            "Unable to parse error"
        }
    }


    fun triggerFinishActivity() {
        _finishEvent.value = true
    }




}