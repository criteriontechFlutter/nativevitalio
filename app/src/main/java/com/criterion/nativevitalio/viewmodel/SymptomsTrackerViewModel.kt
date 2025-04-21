package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.SymptomDetail
import com.critetiontech.ctvitalio.model.SymptomResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.MyApplication
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SymptomsTrackerViewModel : ViewModel() {

    private val _symptomList = MutableLiveData<List<SymptomDetail>>()
    val symptomList: LiveData<List<SymptomDetail>> get() = _symptomList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun getSymptoms() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "uhID" to PrefsManager().getPatient()?.uhID.toString(),
                    "clientID" to PrefsManager().getPatient()?.clientId.toString(),
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .queryDynamicRawPost(
                        url = ApiEndPoint().getSymptoms,
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, SymptomResponse::class.java)
                    _symptomList.value = parsed.responseValue
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


    fun insertSymptoms(selectedSymptoms: List<SymptomDetail>) {
        _loading.value = true

        viewModelScope.launch {
            try {
                val dtDataTable = mutableListOf<Map<String, String>>()

                // Get the current timestamp once
                val now = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"))
                } else {
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                }

                // Populate data table from selected symptoms
                selectedSymptoms.forEach { symptom ->
                    dtDataTable.add(
                        mapOf(
                            "detailID" to symptom.pdmID.toString(),
                            "detailsDate" to now,
                            "details" to symptom.details,
                            "isFromPatient" to "1"
                        )
                    )
                }

                val queryParams = mapOf(
                    "uhID" to (PrefsManager().getPatient()?.uhID ?: ""),
                    "userID" to "0",
                    "doctorId" to "0",
                    "jsonSymtoms" to Gson().toJson(dtDataTable),
                    "clientID" to (PrefsManager().getPatient()?.clientId ?: "")
                )

                val response = RetrofitInstance
                    .createApiService()
                    .queryDynamicRawPost(
                        url = ApiEndPoint().insertSymtoms,
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val context = MyApplication.appContext
                    Toast.makeText(context, "Symptoms saved successfully!", Toast.LENGTH_SHORT).show()

                    getSymptoms() // reload updated list

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
