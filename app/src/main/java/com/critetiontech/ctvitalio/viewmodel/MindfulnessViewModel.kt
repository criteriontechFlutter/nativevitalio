package com.critetiontech.ctvitalio.viewmodel

import com.critetiontech.ctvitalio.utils.PrefsManager
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
