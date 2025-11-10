package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.critetiontech.ctvitalio.model.AllergyApiResponse
import com.critetiontech.ctvitalio.model.JoinedChallenge
import com.critetiontech.ctvitalio.model.NewChallengeModel
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPointCorporateModule
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import okhttp3.ResponseBody

class ChallengesViewModel(application: Application) : BaseViewModel(application) {

    // region LiveData
    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> get() = _loading

    private val _joinedChallenges = MutableLiveData<List<JoinedChallenge>>()
    val joinedChallenges: LiveData<List<JoinedChallenge>> get() = _joinedChallenges

    private val _newChallenges = MutableLiveData<List<NewChallengeModel>>()
    val newChallenges: LiveData<List<NewChallengeModel>> get() = _newChallenges

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    val joinedCount: LiveData<Int> = joinedChallenges.map { it.size }
    val newCount: LiveData<Int> = newChallenges.map { it.size }
    // endregion

    private val gson = Gson()
    private val apiService = RetrofitInstance.createApiService(includeAuthHeader = true)
    private val prefs = PrefsManager()

    /**
     * Fetch all joined challenges for the current user.
     */
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
                    val type = object : TypeToken<AllergyApiResponse<List<JoinedChallenge>>>() {}.type
                    val parsed = gson.fromJson<AllergyApiResponse<List<JoinedChallenge>>>(responseBody, type)
                    _joinedChallenges.postValue(parsed.responseValue ?: emptyList())
                } else {
                    handleError(response.code(), response.errorBody())
                    _joinedChallenges.postValue(emptyList())
                }
            } catch (e: Exception) {
                handleException(e)
                _joinedChallenges.postValue(emptyList())
            } finally {
                _loading.postValue(false)
            }
        }
    }

    /**
     * Fetch all new challenges available to the current user.
     */
    fun getNewChallenge() {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val params = mapOf(
                    "pid" to prefs.getPatient()?.id.toString(),
                    "clientId" to prefs.getPatient()?.clientId.toString()
                )

                val response = apiService.dynamicGet(
                    url = ApiEndPointCorporateModule().getNewChallenge,
                    params = params
                )

                val responseBody = response.body()?.string()
                Log.d("ChallengesVM", "NewChallenge Response: $responseBody")

                if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                    val type = object : TypeToken<AllergyApiResponse<List<NewChallengeModel>>>() {}.type
                    val parsed = gson.fromJson<AllergyApiResponse<List<NewChallengeModel>>>(responseBody, type)
                    _newChallenges.postValue(parsed.responseValue ?: emptyList())
                } else {
                    handleError(response.code(), response.errorBody())
                    _newChallenges.postValue(emptyList())
                }
            } catch (e: Exception) {
                handleException(e)
                _newChallenges.postValue(emptyList())
            } finally {
                _loading.postValue(false)
            }
        }
    }

    /**
     * Join a challenge.
     */
    fun joinChallenge(challengeId: String) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "challengeId" to challengeId,
                    "pid" to prefs.getPatient()?.id.toString(),
                    "clientId" to prefs.getPatient()?.clientId.toString(),
                    "userId" to prefs.getPatient()?.id.toString()
                )

                val response = apiService.dynamicRawPost(
                    url = ApiEndPointCorporateModule().insertChallengeparticipants,
                    body = body
                )

                val responseBody = response.body()?.string()
                Log.d("ChallengesVM", "JoinChallenge Response: $responseBody")

                if (response.isSuccessful) {
                    ToastUtils.showSuccess(MyApplication.appContext, "Challenge joined successfully!")
                    getNewChallenge()
                } else {
                    handleError(response.code(), response.errorBody())
                }
            } catch (e: Exception) {
                handleException(e)
            } finally {
                _loading.postValue(false)
            }
        }
    }

    /**
     * Leave a challenge.
     */
    fun leaveChallenge(challengeId: String) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val params = mapOf(
                    "pid" to prefs.getPatient()?.id.toString(),
                    "challengeId" to challengeId
                )

                val response = apiService.queryDynamicPut(
                    url = ApiEndPointCorporateModule().leaveChallengeparticipants,
                    params = params
                )

                val responseBody = response.body()?.string()
                Log.d("ChallengesVM", "LeaveChallenge Response: $responseBody")

                if (response.isSuccessful) {
                    ToastUtils.showSuccess(MyApplication.appContext, "Challenge left successfully!")
                    getJoinedChallenge()
                } else {
                    handleError(response.code(), response.errorBody())
                }
            } catch (e: Exception) {
                handleException(e)
            } finally {
                _loading.postValue(false)
            }
        }
    }

    // region Helper Methods

    private fun handleError(code: Int, errorBody: ResponseBody?) {
        val message = parseErrorMessage(errorBody)
        _errorMessage.postValue("Error $code: $message")
        ToastUtils.showFailure(MyApplication.appContext, message)
    }

    private fun handleException(e: Exception) {
        Log.e("ChallengesVM", "Exception: ${e.message}", e)
        _errorMessage.postValue(e.message ?: "Unknown error")
        ToastUtils.showFailure(MyApplication.appContext, e.message ?: "Something went wrong")
    }

    private fun parseErrorMessage(errorBody: ResponseBody?): String {
        return try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val map: Map<String, Any> = gson.fromJson(errorBody?.charStream(), type)
            map["message"]?.toString() ?: "Unknown error"
        } catch (e: Exception) {
            "Unable to parse error message"
        }
    }
    // endregion
}
