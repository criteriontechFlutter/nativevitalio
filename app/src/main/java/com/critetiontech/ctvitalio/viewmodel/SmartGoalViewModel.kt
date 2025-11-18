package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.GoalCategoryResponse
import com.critetiontech.ctvitalio.model.GoalItem
import com.critetiontech.ctvitalio.model.SmartGoalResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class SmartGoalViewModel (application: Application) : BaseViewModel(application) {


    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    private val _addedGoalItemList = MutableLiveData<List<GoalCategoryResponse>>()
    val vitalList: LiveData<List<GoalCategoryResponse>> get() = _addedGoalItemList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun getAddedSmartGoal() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val queryParams = mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString(),
                    "clientId" to PrefsManager().getPatient()?.clientId.toString(),
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicGet(
                        url = ApiEndPoint().getAddedSmartGoalApi,
                        params = queryParams
                    )



                if (response.isSuccessful) {
                    _loading.value = false
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, SmartGoalResponse::class.java)

                    _loading.value = false
                    _addedGoalItemList.value = parsed.responseValue


                } else {
                    _addedGoalItemList.value = emptyList()
                    _loading.value = false
                    _errorMessage.value = "Error Code: ${response.code()}"
                }

            } catch (e: Exception) {
                _addedGoalItemList.value = emptyList()
                _loading.value = false
                _loading.value = false
                _errorMessage.value = e.message ?: "Unexpected error"
                e.printStackTrace()
            }
        }
    }

}