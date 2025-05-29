package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.UploadedReportItem
import com.critetiontech.ctvitalio.model.UploadedReportResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.google.gson.Gson
import kotlinx.coroutines.launch

class UploadReportHistoryViewModel : ViewModel() {


    // LiveData for selected report counts
    val selectedRadiology = MutableLiveData<Int>(0)
    val selectedImaging = MutableLiveData<Int>(0)
    val selectedLab = MutableLiveData<Int>(0)

    // Example method to update the count for Radiology reports


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
                    if (parsed.responseValue.isNotEmpty()) {

                        updateCategoryCounts(category ,parsed.responseValue)

                       }
                } else {
                    _reportList.value = emptyList()
//                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _reportList.value = emptyList()
                _loading.value = false
//                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }
    private fun updateCategoryCounts(reports: String , reportData: List<UploadedReportItem>) {
            // Filter the reportData based on the category and update the counts
            when (reports) {
                "Radiology" -> {
                    val radiologyReports = reportData.filter { it.category == "Radiology" }
                    selectedRadiology.value = radiologyReports.size
                }
                "Imaging" -> {
                    val imagingReports = reportData.filter { it.category == "Imaging" }
                    selectedImaging.value = imagingReports.size
                }
                "Lab" -> {
                    val labReports = reportData.filter { it.category == "Lab" }
                    selectedLab.value = labReports.size
                }
            }
        }

}
