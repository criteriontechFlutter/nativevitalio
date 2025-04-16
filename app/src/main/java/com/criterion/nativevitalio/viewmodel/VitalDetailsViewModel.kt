package com.criterion.nativevitalio.viewmodel

import PrefsManager
import Vital
import VitalsResponse
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.utils.ApiEndPoint
import com.criterion.nativevitalio.networking.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.launch

class VitalDetailsViewModel: ViewModel() {

    private val _vitalList = MutableLiveData<List<Vital>>()
    val vitalList: LiveData<List<Vital>> get() = _vitalList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun getVitals() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "uhID" to PrefsManager().getPatient()?.uhid.orEmpty(),
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicGet(
                        url = ApiEndPoint().getPatientLastVital, // ✅ Update to correct endpoint
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, VitalsResponse::class.java)
                    _vitalList.value = parsed.responseValue
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