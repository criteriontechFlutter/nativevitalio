package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.AllergyApiResponse
import com.critetiontech.ctvitalio.model.BPLog
import com.critetiontech.ctvitalio.model.BloodPressureResponse
import com.critetiontech.ctvitalio.model.JoinedChallenge
import com.critetiontech.ctvitalio.model.NewChallengeModel
import com.critetiontech.ctvitalio.model.Summary
import com.critetiontech.ctvitalio.model.WeeklyMapGraph
import com.critetiontech.ctvitalio.model.WeeklyMapTrend
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPointCorporateModule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class BloosPresureHistoryViewModel(application: Application) : BaseViewModel(application) {


    // region LiveData
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _joinedChallenges = MutableLiveData<List<JoinedChallenge>>()
    val joinedChallenges: LiveData<List<JoinedChallenge>> get() = _joinedChallenges

    private val _newChallenges = MutableLiveData<List<NewChallengeModel>>()
    val newChallenges: LiveData<List<NewChallengeModel>> get() = _newChallenges

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    val joinedCount: LiveData<Int> = joinedChallenges.map { it.size }
    val newCount: LiveData<Int> = newChallenges.map { it.size }
    // endregion

    private val gson = Gson()
    private val apiService = RetrofitInstance.createApiService(includeAuthHeader = true)
    private val prefs = PrefsManager()
    private val _summary = MutableLiveData<Summary?>()
    val summary: LiveData<Summary> = _summary as LiveData<Summary>
    private val _bpLogs = MutableLiveData<List<BPLog>?>()
    val bpLogs: LiveData<List<BPLog>> = _bpLogs as LiveData<List<BPLog>>
    private val _weeklyTrend = MutableLiveData<List<WeeklyMapTrend>>()
    val weeklyTrend: LiveData<List<WeeklyMapTrend>> = _weeklyTrend
    private val _weeklyMapGraph = MutableLiveData<List<WeeklyMapGraph>?>()
    val weeklyMapGraph: LiveData<List<WeeklyMapGraph>> = _weeklyMapGraph as LiveData<List<WeeklyMapGraph>>
    fun getBloodPressureDetailsByPid() {

        viewModelScope.launch {
            try {

                val params = mapOf(
                    "pid" to prefs.getPatient()?.id.toString(),
                    "FromDate" to "2026-02-12"
                )

                val response = apiService.dynamicGet(
                    url = ApiEndPointCorporateModule().getBloodPressureDetailsByPid,
                    params = params
                )

                val responseBody = response.body()?.string()

                if (response.isSuccessful && !responseBody.isNullOrEmpty()) {

                    val parsed =
                        gson.fromJson(responseBody, BloodPressureResponse::class.java)

                    parsed.responseValue?.let { value ->

                        // Summary
                        _summary.postValue(value.Summary)

                        // Today Logs
                        val logs = value.TodayLogs?.map {
                            BPLog(
                                systolic = it.systolic.toInt(),
                                diastolic = it.diastolic.toInt(),
                                map = it.map.toInt(),
                                time = it.time,
                                status = it.status
                            )
                        }

                        _bpLogs.postValue(logs)
                        _weeklyTrend.postValue(value.WeeklyMapTrend ?: emptyList())
                        // Weekly Graph
                        _weeklyMapGraph.postValue(value.WeeklyMapGraph)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}