package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.MindfulnessApiResponse
import com.critetiontech.ctvitalio.model.MindfulnessExercise
import com.critetiontech.ctvitalio.model.MindfulnessProgress
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.launch

class WellnessViewModel(application: Application) : BaseViewModel(application) {

    private val _groundingExercises = MutableLiveData<List<MindfulnessExercise>>()
    val groundingExercises: LiveData<List<MindfulnessExercise>> = _groundingExercises

    private val _bingoActivities = MutableLiveData<List<MindfulnessExercise>>()
    val bingoActivities: LiveData<List<MindfulnessExercise>> = _bingoActivities

    private val _breathingActivities = MutableLiveData<List<MindfulnessExercise>>()
    val breathingActivities: LiveData<List<MindfulnessExercise>> = _breathingActivities



    private val _eyeMovementActivities = MutableLiveData<List<MindfulnessExercise>>()
    val eyeMovementActivities: LiveData<List<MindfulnessExercise>> = _eyeMovementActivities
    private val _progress = MutableLiveData<MindfulnessProgress>()
    val progress: LiveData<MindfulnessProgress> = _progress

    init {
        fetchMindfulnessData()
    }

    fun fetchMindfulnessData() {
        val pid = PrefsManager().getPatient()?.id?.toString() ?: "152"
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.createApiService().dynamicGet(
                    url = "api/CorporateMindfulness/GetCorporateMindfulness",
                    params = mapOf("pid" to pid)
                )
                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, MindfulnessApiResponse::class.java)
                    if (parsed?.status == 1) {
                        _progress.postValue(parsed.responseValue.progress)
                        for (category in parsed.responseValue.categories) {
                            when (category.categoryId) {
                                1 -> _groundingExercises.postValue(category.exercises)
                                2 -> _bingoActivities.postValue(category.exercises)
                                3 -> _breathingActivities.postValue(category.exercises)
                                4 -> _eyeMovementActivities.postValue(category.exercises)

                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("WellnessViewModel", "fetchMindfulnessData error: ${e.message}", e)
            }
        }
    }



}

data class MindfulnessStepJson(
    val stepNo: Int,
    val title: String,
    val items: List<String>
)