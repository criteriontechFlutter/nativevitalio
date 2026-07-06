package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.InsertMindfulnessRequest
import com.critetiontech.ctvitalio.model.InsertMindfulnessResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.repository.MindfulnessRepository
import kotlinx.coroutines.launch

class MindfulnessViewModel : ViewModel() {

    private val repository = MindfulnessRepository()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _apiResult = MutableLiveData<Result<InsertMindfulnessResponse>>()
    val apiResult: LiveData<Result<InsertMindfulnessResponse>> = _apiResult

    fun insertMindfulnesss(
        exerciseId: Int,
        duration: Int,
        totalSteps: Int,
        mindfulnessJson: String
    ) {
        if (_isLoading.value == true) return // Prevent duplicate calls

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val patient = PrefsManager().getPatient()
                val pid = patient?.id ?: 0
                val userId = patient?.userId?.toIntOrNull() ?: 0
                val clientId = patient?.clientId ?: 0

                val request = InsertMindfulnessRequest(
                    pid = pid,
                    exerciseId = exerciseId,
                    duration = duration,
                    totalSteps = totalSteps,
                    mindfulnessJson = mindfulnessJson,
                    userId = userId,
                    clientId = clientId
                )

                val response = repository.insertMindfulness(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        _apiResult.value = Result.success(body)
                    } else {
                        _apiResult.value = Result.failure(Exception("Empty response body"))
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "API error: ${response.code()}"
                    _apiResult.value = Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e("MindfulnessViewModel", "Error inserting mindfulness API", e)
                _apiResult.value = Result.failure(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun insertMindfulness(
        exerciseId: Int,
        duration: Int,
        totalSteps: Int,
        mindfulnessJson: String) {
        viewModelScope.launch {
            try {

                val patient = PrefsManager().getPatient()
                val pid = patient?.id ?: 0
                val userId = patient?.userId?.toIntOrNull() ?: 0
                val clientId = patient?.clientId ?: 0

                val body = mutableMapOf<String, Any>(
                    "pid" to pid.toString(),
                    "exerciseId" to exerciseId.toString(),
                    "duration" to duration.toString(),
//                    "totalSteps" to 0,
                    "mindfulnessJson" to mindfulnessJson,
                    "userId" to userId,
                    "clientId" to clientId.toString()
                )
                val response = RetrofitInstance.createApiService().dynamicRawPost(
                    url = "api/CorporateMindfulness/InsertCorporateMindfulness",
                    body =body
                )
                if (response.isSuccessful) {


                }
            } catch (e: Exception) {
                Log.e("WellnessViewModel", "fetchMindfulnessData error: ${e.message}", e)
            }
        }
    }
}
