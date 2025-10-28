package com.critetiontech.ctvitalio.viewmodel

import Patient
import PrefsManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.UI.ChangePassword
import com.critetiontech.ctvitalio.UI.Home
import com.critetiontech.ctvitalio.UI.Login
import com.critetiontech.ctvitalio.UI.otp
import com.critetiontech.ctvitalio.UI.ui.ConfirmUpdateDialog
import com.critetiontech.ctvitalio.model.BaseResponse
import com.critetiontech.ctvitalio.model.OtpResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class LoginViewModel (application: Application) : BaseViewModel(application){

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


    fun corporateEmployeeLogin(context:Context, username: String, password: String,) {
        _loading.value = true
        viewModelScope.launch {
            _loginSuccess.postValue(false)
            try {
                val queryParams = mapOf(
                    // "mobileNo" to mo,
                    "username" to username,
                   "password" to  password
                )

                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService()
                    .dynamicRawPost(
                        url = ApiEndPoint().corporateEmployeeLogin,
                        body = queryParams
                    )


                if (response.isSuccessful) {
                    _loginSuccess.postValue(true)
                    _loading.value = false
                    val responseBodyString = response.body()?.string()
                    val type = object : TypeToken<BaseResponse<List<Patient>>>() {}.type
                    val parsed = Gson().fromJson<BaseResponse<List<Patient>>>(responseBodyString, type)
                    parsed.let {

                        PrefsManager().savePatient(it.responseValue.first())

                    }

                 } else {
                    _loginSuccess.postValue(false)
                    val errorMsg = parseErrorMessage(response.errorBody())
                    ToastUtils.showFailure(MyApplication.appContext, errorMsg)
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


    fun logoutFromApp(uhid: String, deviceToken: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "UHID" to PrefsManager().currentPatientUHID.toString(),
                    "deviceToken" to PrefsManager().getDeviceToken().toString()
                )

                val response = RetrofitInstance
                    .createApiService7082()
                    .dynamicGet(
                        url = ApiEndPoint().logoutFromApp,
                        params = queryParams
                    )



                if (response.isSuccessful) {
                    _loading.value = false
                    PrefsManager().clearPatient()
                    val intent = Intent(MyApplication.appContext, Login::class.java)
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                    MyApplication.appContext.startActivity(intent)

                    _finishEvent.value = true
                } else {

                    PrefsManager().clearPatient()
                    val intent = Intent(MyApplication.appContext, Login::class.java)
                    intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
                    MyApplication.appContext.startActivity(intent)

                    _loading.value = false
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