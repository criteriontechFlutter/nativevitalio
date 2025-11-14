package com.example.vitalio_pragya.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AddActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("FitnessApp", Context.MODE_PRIVATE)

    private val _allActivities = listOf(
        "Biking", "Aerobics", "Archery", "Badminton",
        "Baseball", "Basketball", "Biathlon",
        "Handbiking", "Mountain Biking", "Road Biking",
        "Spinning", "Stationary Biking", "Utility Biking",
        "Boxing", "Walking", "Running", "Jogging",
        "Cycling", "Swimming", "Hiking",
        "Jump Rope", "Stair Climbing", "Cricket"
    )

    private val _recentActivities = MutableLiveData<List<String>>()
    val recentActivities: LiveData<List<String>> = _recentActivities

    private val _filteredActivities = MutableLiveData<List<String>>()
    val filteredActivities: LiveData<List<String>> = _filteredActivities

    init {
        loadRecentActivities()
        _filteredActivities.value = _allActivities
    }

    fun filterActivities(query: String) {
        _filteredActivities.value = if (query.isEmpty()) {
            _allActivities
        } else {
            _allActivities.filter { it.contains(query, ignoreCase = true) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun selectActivity(activity: String) {
        val current = _recentActivities.value?.toMutableList() ?: mutableListOf()
        if (!current.contains(activity)) {
            current.add(0, activity)
            if (current.size > 6) current.removeLast()
            _recentActivities.value = current
            saveRecentActivities(current)
        }
    }

    private fun loadRecentActivities() {
        val saved = prefs.getStringSet("recent_activities", emptySet())?.toList() ?: emptyList()
        _recentActivities.value = saved
    }

    private fun saveRecentActivities(list: List<String>) {
        prefs.edit().putStringSet("recent_activities", list.toSet()).apply()
    }
}