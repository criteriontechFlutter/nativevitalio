package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.AllergyApiResponse
import com.critetiontech.ctvitalio.model.DashboardActiveChallenges
import com.critetiontech.ctvitalio.model.ResponseValueModel
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPointCorporateModule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class NewChallengesDetailsViewModel (application: Application) : BaseViewModel(application){


    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _joinedChallenges = MutableLiveData<List<DashboardActiveChallenges>>()
    val joinedChallenges: LiveData<List<DashboardActiveChallenges>> get() = _joinedChallenges



    private val gson = Gson()
    private val apiService = RetrofitInstance.createApiService()
    private val prefs = PrefsManager()


    fun getJoinedChallenge() {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val params = mapOf(
                    "pid" to prefs.getPatient()?.id.toString(),
                    "clientId" to prefs.getPatient()?.clientId.toString()
                )

                val response = apiService.dynamicGet(
                    url = ApiEndPointCorporateModule().getJoinedChallenge,
                    params = params
                )

                val responseBody = response.body()?.string()
                Log.d("ChallengesVM", "JoinedChallenge Response: $responseBody")

                if (response.isSuccessful && !responseBody.isNullOrEmpty()) {

                    val type = object : TypeToken<AllergyApiResponse<ResponseValueModel>>() {}.type

                    val parsed = gson.fromJson<AllergyApiResponse<ResponseValueModel>>(responseBody, type)

                    _joinedChallenges.postValue(
                        parsed.responseValue.joinedChallenges
                    )
                } else {
                    _joinedChallenges.postValue(emptyList())
                }
            } catch (e: Exception) { 
                _joinedChallenges.postValue(emptyList())
            } finally {
                _loading.postValue(false)
            }
        }
    }


}