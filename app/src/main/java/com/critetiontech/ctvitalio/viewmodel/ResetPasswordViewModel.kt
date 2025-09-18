package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import kotlinx.coroutines.launch

class ResetPasswordViewModel (application: Application) : BaseViewModel(application){

    val isRegistered = MutableLiveData<Int>()
    private val _finishEvent = MutableLiveData<Boolean>()
    val finishEvent: LiveData<Boolean> get() = _finishEvent
    private val _showDialog = MutableLiveData<String?>()
    val showDialog: LiveData<String?> get() = _showDialog
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading




    private val _loginSuccess = MutableLiveData<Boolean>()
    val loginSuccess: LiveData<Boolean> get() = _loginSuccess
    fun  resetPassword(context: Context, newPassword: String, confirmNewPassword: String) {
        _loading.value = true
        viewModelScope.launch {

            _loginSuccess.postValue(false)

            try {
                val queryParams = mapOf(
                    // "mobileNo" to mo,

                    "username" to PrefsManager().getPatient()?.empId.toString(),
                "newPassword" to newPassword,
                "confirmNewPassword" to confirmNewPassword,
                    )

                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService()
                    .dynamicRawPost(
                        url = ApiEndPoint().corporateEmployeeForgotPassword,
                        body = queryParams
                    )


                if (response.isSuccessful) {

                    _loginSuccess.postValue(true)
//                    val responseBodyString = response.body()?.string()
//                    Log.d("RESPONSE", "phoneOrUHID3"+responseBodyString.toString())
                } else {

                    _loginSuccess.postValue(false)
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