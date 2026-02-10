package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.BPLog
import com.critetiontech.ctvitalio.model.BloodPressureResponse
import com.critetiontech.ctvitalio.model.GlucoseLog
import com.critetiontech.ctvitalio.model.GlucoseResponse
import com.critetiontech.ctvitalio.model.GlucoseSummary
import com.critetiontech.ctvitalio.model.JoinedChallenge
import com.critetiontech.ctvitalio.model.MonthlyGraph
import com.critetiontech.ctvitalio.model.NewChallengeModel
import com.critetiontech.ctvitalio.model.Summary
import com.critetiontech.ctvitalio.model.TrendGraph
import com.critetiontech.ctvitalio.model.WeeklyMapGraph
import com.critetiontech.ctvitalio.model.WeeklyMapTrend
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPointCorporateModule
import com.google.gson.Gson
import kotlinx.coroutines.launch

class GlucoseHistoryViewModel(application: Application) : BaseViewModel(application) {

    private val gson = Gson()
    private val apiService = RetrofitInstance.createApiService()
    private val prefs = PrefsManager()

    // Summary
    private val _summary = MutableLiveData<GlucoseSummary>()
    val summary: LiveData<GlucoseSummary> = _summary

    // Today Logs
    private val _logs = MutableLiveData<List<GlucoseLog>>()
    val logs: LiveData<List<GlucoseLog>> = _logs

    // Weekly Avg Graph
    private val _weeklyGraph = MutableLiveData<List<WeeklyMapGraph>>()
    val weeklyGraph: LiveData<List<WeeklyMapGraph>> = _weeklyGraph

    // Monthly Graph
    private val _monthlyGraph = MutableLiveData<List<MonthlyGraph>>()
    val monthlyGraph: LiveData<List<MonthlyGraph>> = _monthlyGraph

    // Trend Graph
    private val _trendGraph = MutableLiveData<List<TrendGraph>>()
    val trendGraph: LiveData<List<TrendGraph>> = _trendGraph


    fun getGlucoseDetailsByPid() {

        viewModelScope.launch {

            try {
                val params = mapOf(
                    "pid" to prefs.getPatient()?.id.toString(),
                    "FromDate" to "2026-02-05"
                )

                val response = apiService.dynamicGet(
                    url = ApiEndPointCorporateModule().getGlucoseDetailsByPid,
                    params = params
                )

                val body = response.body()?.string()

                if (response.isSuccessful && !body.isNullOrEmpty()) {

                    val parsed = gson.fromJson(body, GlucoseResponse::class.java)

                    val value = parsed.responseValue ?: return@launch

                    // Assign data
                    _summary.postValue(value.Summary)
                    _logs.postValue(value.TodayLogs)
                    _weeklyGraph.postValue(value.GlucoseAvgGraph)
                    _monthlyGraph.postValue(value.GlucoseMonthlyGraph)
                    _trendGraph.postValue(value.GlucoseTrendGraph)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}