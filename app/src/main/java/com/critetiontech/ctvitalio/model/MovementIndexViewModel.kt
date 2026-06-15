package com.critetiontech.ctvitalio.model

import PrefsManager
import SleepValue
import VitalsResponse
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.networking.RetrofitInstance
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

    private val _sleepTime = MutableLiveData<String>()
    val sleepTime: LiveData<String> get() = _sleepTime

    private val _wakeTime = MutableLiveData<String>()
    val wakeTime: LiveData<String> get() = _wakeTime

    private val _sleepCyclesCount = MutableLiveData<String>()
    val sleepCyclesCount: LiveData<String> get() = _sleepCyclesCount

    private val _totalSleepText = MutableLiveData<String>()
    val totalSleepText: LiveData<String> get() = _totalSleepText

init {
    getWellnessData(getCurrentDate())
    fetchSleepSummary()
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

    fun fetchSleepSummary() {
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString(),
                    "clientId" to 194
                )
                val response = RetrofitInstance
                    .createApiService()
                    .dynamicGet(
                        url = "api/UltrahumanVitals/GetUltrahumanVitalsByPid",
                        params = queryParams
                    )
                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, VitalsResponse::class.java)
                    val lastVital = parsed.responseValue.lastVital

                    _totalSleepText.postValue(
                        lastVital.firstOrNull { it.vitalName == "TotalSleep" }?.vmValueText ?: "--"
                    )
                    _sleepCyclesCount.postValue(
                        lastVital.firstOrNull { it.vitalName == "SleepCycles" }?.vmValueText ?: "--"
                    )

                    val sleepMetricJson = parsed.responseValue.sleepmetrics
                        ?.firstOrNull { it.vitalID == 243 }
                        ?.vitalValue

                    sleepMetricJson?.let { vitalJson ->
                        val sleepValue: SleepValue = if (vitalJson.trim().startsWith("{")) {
                            Gson().fromJson(vitalJson, SleepValue::class.java)
                        } else {
                            val unescaped = Gson().fromJson(vitalJson, String::class.java)
                            Gson().fromJson(unescaped, SleepValue::class.java)
                        }
                        _sleepTime.postValue(formatBedtime(sleepValue.BedtimeStart))
                        _wakeTime.postValue(formatBedtime(sleepValue.BedtimeEnd))
                    }
                }
            } catch (_: Exception) { }
        }
    }

    private fun formatBedtime(isoTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            outputFormat.format(inputFormat.parse(isoTime)!!)
        } catch (_: Exception) {
            "--"
        }
    }
}