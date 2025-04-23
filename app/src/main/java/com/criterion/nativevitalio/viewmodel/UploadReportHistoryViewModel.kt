package com.criterion.nativevitalio.viewmodel

import PrefsManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.model.UploadedReportItem
import com.criterion.nativevitalio.model.UploadedReportResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.google.gson.Gson
import kotlinx.coroutines.launch

class UploadReportHistoryViewModel : ViewModel() {

    private val _reportList = MutableLiveData<List<UploadedReportItem>>()
    val reportList: LiveData<List<UploadedReportItem>> get() = _reportList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun getReportsByCategory(category: String) {
        _reportList.value = emptyList()
        _loading.value = true
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "uhId" to PrefsManager().getPatient()?.uhID.orEmpty(),
                    "category" to category,
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true,
                        )
                    .dynamicGet(
                        url = ApiEndPoint().getPatientMediaData,
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, UploadedReportResponse::class.java)
                    _reportList.value = parsed.responseValue
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
