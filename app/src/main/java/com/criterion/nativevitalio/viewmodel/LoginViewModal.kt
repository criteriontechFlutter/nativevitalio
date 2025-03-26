package com.criterion.nativevitalio.viewmodel

import Patient
import PrefsManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.networking.RetrofitInstance
import com.criterion.nativevitalio.networking.generateAuthHeaderMap
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import retrofit2.Response
import okhttp3.ResponseBody

class LoginViewModel : ViewModel() {

    private val _movieResults = MutableLiveData<List<Patient>>()
    val movieResults: LiveData<List<Patient>> get() = _movieResults

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
                    .createApiService( )
                    .dynamicGet(
                        url = "api/PatientRegistration/GetPatientDetailsByMobileNo",

                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val responseBodyString = response.body()?.string()
//                    val patient = Gson().fromJson(jsonString, Patient::class.java)
//                    PrefsManager().savePatient( )


                    // Optionally parse it:
                    // val patient = Gson().fromJson(responseBodyString, Patient::class.java)
                    // _userData.value = patient
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