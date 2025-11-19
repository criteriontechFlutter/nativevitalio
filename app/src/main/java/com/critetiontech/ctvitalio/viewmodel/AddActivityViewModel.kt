package com.example.vitalio_pragya.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.EmployeeActivity
import com.critetiontech.ctvitalio.model.EmployeeActivityResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPointCorporateModule
import com.google.gson.Gson
import kotlinx.coroutines.launch

class AddActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("FitnessApp", Context.MODE_PRIVATE)

    private val _activities = MutableLiveData<List<EmployeeActivity>>()
    val activities: LiveData<List<EmployeeActivity>> get() = _activities
//    private val _allActivities = listOf(
//        "Biking", "Aerobics", "Archery", "Badminton",
//        "Baseball", "Basketball", "Biathlon",
//        "Handbiking", "Mountain Biking", "Road Biking",
//        "Spinning", "Stationary Biking", "Utility Biking",
//        "Boxing", "Walking", "Running", "Jogging",
//        "Cycling", "Swimming", "Hiking",
//        "Jump Rope", "Stair Climbing", "Cricket"
//    )

    private val _recentActivities = MutableLiveData<List<EmployeeActivity>>()
    val recentActivities: LiveData<List<EmployeeActivity>> = _recentActivities

    private val _filteredActivities = MutableLiveData<List<EmployeeActivity>>()
    val filteredActivities: LiveData<List<EmployeeActivity>> = _filteredActivities

    init {
        loadRecentActivities()
        _filteredActivities.value = filteredActivities.value
    }

    fun filterActivities(query: String) {
        _filteredActivities.value = if (query.isEmpty()) {
            filteredActivities.value
        } else {
            filteredActivities.value.filter { it.activityName.contains(query, ignoreCase = true) }
        }


    }

//    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
//    fun selectActivity(activity: String) {
//        val current = _recentActivities.value?.toMutableList() ?: mutableListOf()
//        if (!current.contains(activity.activityName)) {
//            current.add(0, activity)
//            if (current.size > 6) current.removeLast()
//            _recentActivities.value = current
//            saveRecentActivities(current)
//        }
//    }

    private fun loadRecentActivities() {
        val saved = prefs.getStringSet("recent_activities", emptySet())?.toList() ?: emptyList()
//        _recentActivities.value = savedEmployeeActivity
    }

    private fun saveRecentActivities(list: List<String>) {
        prefs.edit().putStringSet("recent_activities", list.toSet()).apply()
    }
 fun getAllEmployeeActivity() {
        viewModelScope.launch {
            try {
                val queryParams = mapOf("clientId" to "194")

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicGet(
                        url = ApiEndPointCorporateModule().getAllEmployeeActivity,
                        params = queryParams
                    )

                if (response.isSuccessful) {
                    val json = response.body()?.string()

                    Log.d("RESPONSE", json ?: "null")

                    val data = Gson().fromJson(json, EmployeeActivityResponse::class.java)

                    _activities.value = data.responseValue

                } else {
                }

            } catch (e: Exception) {
            }
        }
    }



    fun insertEmployeeActivity() {
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "pid" to 89,
                    "activityId" to 1,
                    "startTime" to "12:30 PM",
                    "endTime" to "1:30 PM",
                    "userId" to 99,
                    "clientId" to 194
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicRawPost(
                        url = ApiEndPointCorporateModule().insertEmployeeActivity,
                        body = queryParams
                    )

                if (response.isSuccessful) {
                    val json = response.body()?.string()

                    Log.d("RESPONSE", json ?: "null")

                    val data = Gson().fromJson(json, EmployeeActivityResponse::class.java)

                    _activities.value = data.responseValue

                } else {
                }

            } catch (e: Exception) {
            }
        }
    }
}