package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.AllergyApiResponse
import com.critetiontech.ctvitalio.model.JoinedChallenge
import com.critetiontech.ctvitalio.model.NewChallengeModel
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPointCorporateModule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONObject

class ChallengesViewModel(application: Application) : BaseViewModel(application) {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading


    private val _joinedChallengeList = MutableLiveData<List<JoinedChallenge>>()
    val joinedChallengeList: LiveData<List<JoinedChallenge>> get() = _joinedChallengeList

    private val _newChallengeList = MutableLiveData<List<NewChallengeModel>>()
    val newChallengeList: LiveData<List<NewChallengeModel>> get() = _newChallengeList


    val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    val joinedCount: LiveData<Int> = joinedChallengeList.map { it.size }
    val newCount: LiveData<Int> = newChallengeList.map { it.size }

    /**
     * Fetches the list of challenges that the current patient has joined.
     */
    fun getJoinedChallenge() {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString(),
                    "clientId" to PrefsManager().getPatient()?.clientId.toString()
                )
                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicGet(
                        url = ApiEndPointCorporateModule().getJoinedChallenge,
                        params = queryParams
                    )
                if (response.isSuccessful) {
                    _loading.value = false
                    val json = response.body()?.string()
                    if (json.isNullOrEmpty()) {
                        _errorMessage.postValue("Empty response")
                        _loading.postValue(false)
                        return@launch
                    }

                    val type =
                        object : TypeToken<AllergyApiResponse<List<JoinedChallenge>>>() {}.type
                    val parsed =
                        Gson().fromJson<AllergyApiResponse<List<JoinedChallenge>>>(json, type)
                    _joinedChallengeList.value = parsed.responseValue
                } else {
                    _joinedChallengeList.value = emptyList()
                    _loading.value = false
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _joinedChallengeList.value = emptyList()
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }


    /**
     * Fetches a list of new challenges available to the patient.
     */
    fun getNewChallenge() {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "pid" to PrefsManager().getPatient()?.pid.orEmpty(),
                    "clientId" to PrefsManager().getPatient()?.clientId.toString()
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicGet(
                        url = ApiEndPointCorporateModule().getNewChallenge,
                        params = queryParams
                    )

                if (response.isSuccessful) {
                    _loading.value = false
                    val json = response.body()?.string()
                    if (json.isNullOrEmpty()) {
                        _errorMessage.postValue("Empty response")
                        _loading.postValue(false)
                        return@launch
                    }

                    val type = object : TypeToken<AllergyApiResponse<List<NewChallengeModel>>>() {}.type
                    val parsed =
                        Gson().fromJson<AllergyApiResponse<List<NewChallengeModel>>>(json, type)
                    _newChallengeList.value = parsed.responseValue
                } else {
                    _newChallengeList.value = emptyList()
                    _loading.value = false
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _newChallengeList.value = emptyList()
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }


    fun insertChallengeparticipants(  challengesId: String) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                 "challengeId" to challengesId.toString(),
                "pid" to PrefsManager().getPatient()?.id.toString(),
                "clientId" to PrefsManager().getPatient()?.clientId.toString(),
                "userId" to PrefsManager().getPatient()?.id.toString(),

                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicRawPost(
                        url = "api/Challengeparticipants/InsertChallengeparticipants",
                        body = queryParams
                    )

                if (response.isSuccessful) {
                    _loading.value = false
                    val responseBodyString = response.body()?.string()
                    Log.d("RESPONSE", "phoneOrUHID3: $responseBodyString")

                    // Parse the JSON response

                    val jsonObject = JSONObject(responseBodyString)
                    val message = jsonObject.optString("message", "Success")
                    _loading.postValue(false)
//                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    getNewChallenge()
                } else {
                    val responseBodyString = response.errorBody()?.string()
                    Log.d("RESPONSE", "phoneOrUHID3: $responseBodyString")

                    // Parse the JSON response

                    val jsonObject = JSONObject(responseBodyString)
                    val message = jsonObject.optString("message", "Success")

//                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    _loading.value = false
                    _loading.postValue(false)
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {

                _loading.value = false
                _loading.postValue(false)
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }

}