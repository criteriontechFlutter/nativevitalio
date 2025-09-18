package com.critetiontech.ctvitalio.viewmodel

import Mood
import MoodResponse
import MoodsResponse
import PrefsManager
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.ApiEndPointCorporateModule
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoodViewModel(application: Application) : BaseViewModel(application){

    val selectedMoodId = MutableLiveData<String>()
    fun onMoodClicked(id:String) {
         selectedMoodId.value =id
    }
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    fun insertMood(context: Context ) {
        viewModelScope.launch {
            _loading.value = true
            try {

                val queryParams = mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString() ,
                    "moodId" to selectedMoodId.value ,
                    "userId" to "99",
                    "clientId" to "194",
                )



                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicRawPost(
                        url =  ApiEndPointCorporateModule().insertMood,
                        body = queryParams
                    )


                if (response.isSuccessful) {
                    _loading.value = false
                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()


                } else {

                    _loading.value = false
                    _errorMessage.value = "Error Code: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _loading.value = false
                _errorMessage.value = e.message ?: "Unexpected error"
                e.printStackTrace()
            }
        }
    }
    private val _moods = MutableLiveData<List<Mood>>()
    val moodsLiveData: LiveData<List<Mood>> get() = _moods
    fun getMoodByPid( ) {
        viewModelScope.launch {
            _loading.value = true
            try {



                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicGet(
                        url =  ApiEndPointCorporateModule().getAllMoods,
                        params = emptyMap()
                    )


                if (response.isSuccessful) {
                    _loading.value = false
                    val json = response.body()?.string()
                    val moodsResponse = Gson().fromJson(json, MoodsResponse::class.java)

                    Log.d("RESPONSE", "responseValue: $moodsResponse")

                    _moods.value = moodsResponse.responseValue
                } else {

                    _loading.value = false
                    _errorMessage.value = "Error Code: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _loading.value = false
                _errorMessage.value = e.message ?: "Unexpected error"
                e.printStackTrace()
            }
        }
    }
}