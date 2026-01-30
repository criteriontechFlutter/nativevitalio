package com.critetiontech.ctvitalio.model

import PrefsManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MovementIndexViewModel : ViewModel() {


    private val _wellnessMetric   = MutableLiveData<MovementIndexResponseValue>()
    val wellnessMetrics: LiveData<MovementIndexResponseValue> get() = _wellnessMetric



    val progress = MutableLiveData<String>()
    val steps = MutableLiveData<String>()
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

init {
    getWellnessData(getCurrentDate())
}
    fun getCurrentDate(pattern: String = "yyyy-MM-dd"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date())
    }
    fun getWellnessData(formatted: String) {
        _loading.value = true

        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString(),
                    "clientId" to PrefsManager().getPatient()?.clientId.toString(),
                    "date" to  formatted
                )

                val response = RetrofitInstance
                    .createApiService()
                    .dynamicGet(
                        url = "api/UltrahumanVitals/GetWellnessDataByPid",
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val json = response.body()?.string()

                    val parsed = Gson().fromJson(json, WellnessResponse::class.java)

                    // Store API response
                    _wellnessMetric .postValue(parsed.responseValue)


                } else {
                 }

            } catch (e: Exception) {
                _loading.value = false
                 _errorMessage.value = e.message ?: "Unknown error"
            }
        }
    }



}