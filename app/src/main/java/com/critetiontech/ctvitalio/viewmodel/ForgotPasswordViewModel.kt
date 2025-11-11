package com.critetiontech.ctvitalio.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class ForgotPasswordViewModel(application: Application) : BaseViewModel(application){

    val isRegistered = MutableLiveData<Int>()
    private val _finishEvent = MutableLiveData<Boolean>()
    val finishEvent: LiveData<Boolean> get() = _finishEvent
    private val _showDialog = MutableLiveData<String?>()
    val showDialog: LiveData<String?> get() = _showDialog
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading




    fun forgotPassword(context: Context, username: String, ) {
        _loading.value = true
        viewModelScope.launch {


            try {
                val queryParams = mapOf(
                    // "mobileNo" to mo,

                    "emailId" to username,
                )

                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService()
                    .queryDynamicRawPost(
                        url = ApiEndPoint().employeeResetPassowrdLink,
                        params = queryParams
                    )


                if (response.isSuccessful) {
                 

                    val errorMsg = parseErrorMessage(response.body())
                    ToastUtils.showFailure(MyApplication.appContext, errorMsg)

                } else {


                    val errorMsg = parseErrorMessage(response.errorBody())
                    ToastUtils.showFailure(MyApplication.appContext, errorMsg)
                    _loading.value = false
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {

                Toast.makeText(context,  e.message, Toast.LENGTH_LONG).show()
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
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

}