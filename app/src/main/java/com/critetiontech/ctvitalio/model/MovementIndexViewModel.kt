package com.critetiontech.ctvitalio.model

import PrefsManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ArcProgressView
import com.critetiontech.ctvitalio.utils.CircularProgressView
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MovementIndexViewModel : ViewModel() {

    // Data class
    data class MovementData(
        val progress: Int = 55,
        val calories: Int = 450,
        val steps: Int = 2150,
        val hourlyData: List<Int> = listOf(40, 25, 50, 15, 35, 20, 45, 30),
        val achievementText: String = "You achieved 1/2 movement goals"
    )

    private val _wellnessMetricList = MutableLiveData<List<WellnessItem>>()
    val wellnessMetrics: LiveData<List<WellnessItem>> get() = _wellnessMetricList


    // LiveData
    val movementData = MutableLiveData<MovementData>()
    val progress = MutableLiveData<String>()
    val calories = MutableLiveData<String>()
    val steps = MutableLiveData<String>()
    val achievementText = MutableLiveData<String>()

    private var updateJob: kotlinx.coroutines.Job? = null
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    /**
     * Load initial data
     */
    fun loadInitialData() {
        fetchMovementData()
        startPeriodicUpdates()
    }


    fun getWellnessData() {
        _loading.value = true

        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString(),
                    "clientId" to PrefsManager().getPatient()?.clientId.toString()
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicGet(
                        url = "api/UltrahumanVitals/GetWellnessDataByPid",
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val json = response.body()?.string()

                    val parsed = Gson().fromJson(json, WellnessResponse::class.java)

                    // Store API response
                    _wellnessMetricList.value = parsed.responseValue

                } else {
                    _wellnessMetricList.value = emptyList()
                }

            } catch (e: Exception) {
                _loading.value = false
                _wellnessMetricList.value = emptyList()
                _errorMessage.value = e.message ?: "Unknown error"
            }
        }
    }
    fun getLatestVital(vitalList: List<WellnessItem>, vitalName: String): Reading? {
        val vital = vitalList.firstOrNull {
            it.vitalName.equals(vitalName, ignoreCase = true)
        } ?: return null

        val readings = vital.decodedReadings()
        if (readings.isEmpty()) return null

        return readings.maxByOrNull { it.vitalDateTime }
    }


    /**
     * Fetch movement data from API or local source
     */
    fun fetchMovementData() {
        viewModelScope.launch {
            _loading.value = true
            try {
                // Simulate API delay
                delay(300)

                val data = MovementData(
                    progress = 55,
                    calories = 450,
                    steps = 2150,
                    hourlyData = listOf(40, 25, 50, 15, 35, 20, 45, 30),
                    achievementText = "You achieved 1/2 movement goals"
                )

                updateMovementData(data)
                _loading.value = false
            } catch (e: Exception) {
                _loading.value = false
                e.printStackTrace()
            }
        }
    }

    /**
     * Update movement data
     */
    private fun updateMovementData(data: MovementData) {
        movementData.value = data
        updateProgressValue(data.progress)
        updateMetrics(data.calories, data.steps)
        updateAchievementStatus(data.achievementText)
    }

    /**
     * Update progress value with animator
     */
    private fun updateProgressValue(newValue: Int) {
        val currentValue = progress.value?.toIntOrNull() ?: 0
        animateValueChange(currentValue, newValue) { value ->
            progress.value = value.toString()
        }
    }

    /**
     * Update circular progress view
     */
    fun updateCircularProgress(circularView: ArcProgressView) {
        val newValue = movementData.value?.progress?.toFloat() ?: 55f
      //  circularView.animateTo(newValue, 800)
    }

    /**
     * Update metrics (calories and steps)
     */
    private fun updateMetrics(caloriesValue: Int, stepsValue: Int) {
        calories.value = "$caloriesValue kcal"
        steps.value = String.format("%,d", stepsValue)
    }

    /**
     * Update achievement status
     */
    private fun updateAchievementStatus(newStatus: String) {
        achievementText.value = newStatus
    }

    /**
     * Animate value change
     */
    private fun animateValueChange(
        startValue: Int,
        endValue: Int,
        onUpdate: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val duration = 500L
            val steps = 30
            val stepDuration = duration / steps
            val increment = (endValue - startValue) / steps

            for (i in 0..steps) {
                delay(stepDuration)
                val currentValue = startValue + (increment * i)
                onUpdate(currentValue)
            }
        }
    }

    /**
     * Start periodic data updates
     */
    private fun startPeriodicUpdates() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (true) {
                delay(5000) // Update every 5 seconds

                val randomProgress = (20..100).random()
                val randomCalories = (200..800).random()
                val randomSteps = (1000..5000).random()
                val randomHourly = List(8) { (10..60).random() }

                val newData = MovementData(
                    progress = randomProgress,
                    calories = randomCalories,
                    steps = randomSteps,
                    hourlyData = randomHourly,
                    achievementText = "You achieved ${(1..2).random()}/2 movement goals"
                )

                updateMovementData(newData)
            }
        }
    }

    /**
     * Refresh movement data
     */
    fun refreshMovementData() {
        fetchMovementData()
    }

    /**
     * Stop periodic updates
     */
    fun stopPeriodicUpdates() {
        updateJob?.cancel()
    }

    /**
     * Clear ViewModel resources
     */
    override fun onCleared() {
        super.onCleared()
        stopPeriodicUpdates()
    }
}