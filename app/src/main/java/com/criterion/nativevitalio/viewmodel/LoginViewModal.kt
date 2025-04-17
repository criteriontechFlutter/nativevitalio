package com.criterion.nativevitalio.viewmodel

import Patient
import PrefsManager
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.UI.Login
import com.criterion.nativevitalio.UI.otp
import com.criterion.nativevitalio.model.BaseResponse
import com.criterion.nativevitalio.networking.RetrofitInstance
import com.criterion.nativevitalio.utils.ApiEndPoint
import com.criterion.nativevitalio.utils.MyApplication
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {


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


}