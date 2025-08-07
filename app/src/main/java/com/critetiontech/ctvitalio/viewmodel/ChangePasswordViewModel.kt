package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.UI.ResetPassword
import com.critetiontech.ctvitalio.model.OtpResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.MyApplication
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class ChangePasswordViewModel(application: Application) : BaseViewModel(application){

    val isRegistered = MutableLiveData<Int>()
    private val _finishEvent = MutableLiveData<Boolean>()
    val finishEvent: LiveData<Boolean> get() = _finishEvent
    private val _showDialog = MutableLiveData<String?>()
    val showDialog: LiveData<String?> get() = _showDialog
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading




    fun changePassword(context: Context, confirmNewPassword: String,newPassword:String,oldPassword:String  ) {
        _loading.value = true
        viewModelScope.launch {


            try {
                val queryParams = mapOf(

                    "pid" to PrefsManager().getPatient()?.id.toString(),
                "clientID" to PrefsManager().getPatient()?.clientId.toString(),
                "oldPassword" to oldPassword.toString(),
                "newPassword" to newPassword.toString(),
                "confirmNewPassword" to confirmNewPassword.toString(),
                )

                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService()
                    .dynamicRawPost(
                        url = ApiEndPoint().corporateEmployeeChangePassword,
                        body = queryParams
                    )


                if (response.isSuccessful) {
                    val responseBodyString = response.body()?.string()
                    Log.d("RESPONSE", "phoneOrUHID3: $responseBodyString")

                    // Parse the JSON response

                        val jsonObject = JSONObject(responseBodyString)
                        val message = jsonObject.optString("message", "Success")

                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()



                } else {

                    val responseBodyString = response.errorBody()?.string()
                    Log.d("RESPONSE", "phoneOrUHID3: $responseBodyString")

                    // Parse the JSON response

                        val jsonObject = JSONObject(responseBodyString)
                        val message = jsonObject.optString("message", "Success")

                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()


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