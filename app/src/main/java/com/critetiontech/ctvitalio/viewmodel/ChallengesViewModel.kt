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
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
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
//            try {
                val queryParams = mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString(),
                    "clientId" to "194",
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicGet(
                        url = ApiEndPointCorporateModule().getNewChallenge,
                        params = queryParams
                    )

            Log.d("RESPONSE", "responseresponseresponseresponse: ${response.body()}")
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

//            } catch (e: Exception) {
//                _newChallengeList.value = emptyList()
//                _loading.value = false
//                _errorMessage.value = e.message ?: "Unknown error occurred"
//                e.printStackTrace()
//            }
        }
    }


    fun insertChallengeparticipants(  challengesId: String) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                 "challengeId" to challengesId.toString(),
                "pid" to PrefsManager().getPatient()?.id.toString(),
                "clientId" to "194",
                "userId" to PrefsManager().getPatient()?.id.toString(),

                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicRawPost(
                        url =  ApiEndPointCorporateModule().insertChallengeparticipants,
                        body = queryParams
                    )

                if (response.isSuccessful) {
                    val errorMsg = parseErrorMessage(response.body())
                    ToastUtils.showFailure(MyApplication.appContext, errorMsg)
                    _loading.value = false

                    _loading.postValue(false)
//
                    getNewChallenge()
                } else {

                    val errorMsg = parseErrorMessage(response.errorBody())
                    ToastUtils.showFailure(MyApplication.appContext, errorMsg)

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

    fun parseErrorMessage(errorBody: ResponseBody?): String {
        return try {
            val gson = Gson()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val errorMap: Map<String, Any> = gson.fromJson(errorBody?.charStream(), type)
            errorMap["message"]?.toString() ?: "Something went wrong"
        } catch (e: Exception) {
            "Unable to parse error"
        }
    }

    fun leaveChallenge(  challengesId: String) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString(),
                    "challengeId" to "4".toString(),

                     )

                val response = RetrofitInstance
                    .createApiService( )
                    .queryDynamicPut(
                        url =  ApiEndPointCorporateModule().leaveChallengeparticipants,
                        params = queryParams
                    )

                if (response.isSuccessful) {

                    Log.d("RESPONSE", "response: ${response.body()}")
                    val errorMsg = parseErrorMessage(response.body())
                    ToastUtils.showFailure(MyApplication.appContext, errorMsg)
                    _loading.value = false

                    _loading.postValue(false)
//
                    getNewChallenge()
                } else {
                    Log.d("RESPONSE", "response: ${response.body()}")

                    val errorMsg = parseErrorMessage(response.errorBody())
                    ToastUtils.showFailure(MyApplication.appContext, errorMsg)

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