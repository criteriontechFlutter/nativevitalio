package com.critetiontech.ctvitalio.viewmodel

import AllMedicine
import LoggedMedicine
import MedicineIntakeResponse
import PrefsManager
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPointCorporateModule
import com.google.gson.Gson
import kotlinx.coroutines.launch

class BinkingViewModel(application: Application) : AndroidViewModel(application) {


    private val _activityInsertSuccess = MutableLiveData<Boolean>()
    val activityInsertSuccess: LiveData<Boolean> = _activityInsertSuccess
    fun insertEmployeeActivity(activityId: Int, startTime: String, endTime: String) {
        viewModelScope.launch {
            try {
                val params = mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString(),
                    "activityId" to activityId,
                    "startTime" to startTime,
                    "endTime" to endTime,
                    "userId" to PrefsManager().getPatient()?.id.toString(),
                    "clientId" to PrefsManager().getPatient()?.clientId.toString()
                )

                val response = RetrofitInstance.createApiService(includeAuthHeader = true)
                    .dynamicRawPost(
                        url = ApiEndPointCorporateModule().insertEmployeeActivity,
                        body = params
                    )

                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    Log.d("INSERT_ACTIVITY", "SUCCESS → $json")

                    _activityInsertSuccess.postValue(true) // Live update event success

                } else {
                    Log.e("INSERT_ACTIVITY", "FAILED → ${response.errorBody()?.string()}")
                    _activityInsertSuccess.postValue(false)
                }

            } catch (e: Exception) {
                Log.e("INSERT_ACTIVITY", e.localizedMessage.toString())
                _activityInsertSuccess.postValue(false)
            }
        }
    }
}