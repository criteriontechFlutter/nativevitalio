package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.GoalCategoryResponse
import com.critetiontech.ctvitalio.model.GoalItem
import com.critetiontech.ctvitalio.model.SmartGoalResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.MyApplication
import com.google.gson.Gson
import kotlinx.coroutines.launch

class SmartGoalViewModel (application: Application) : BaseViewModel(application) {


    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    private val _addedGoalItemList = MutableLiveData<List<GoalCategoryResponse>>()
    val vitalList: LiveData<List<GoalCategoryResponse>> get() = _addedGoalItemList

    private val _allGoalList = MutableLiveData<List<GoalCategoryResponse>>()
    val allGoalList: LiveData<List<GoalCategoryResponse>> get() = _allGoalList

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



    fun getAllGoalList() {
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
                        url = ApiEndPoint().getAllGoalListApi,
                        params = queryParams
                    )



                if (response.isSuccessful) {
                    _loading.value = false
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, SmartGoalResponse::class.java)

                    _loading.value = false
                    _allGoalList.value = parsed.responseValue


                } else {
                    _allGoalList.value = emptyList()
                    _loading.value = false
                    _errorMessage.value = "Error Code: ${response.code()}"
                }

            } catch (e: Exception) {
                _allGoalList.value = emptyList()
                _loading.value = false
                _loading.value = false
                _errorMessage.value = e.message ?: "Unexpected error"
                e.printStackTrace()
            }
        }
    }


    fun updatePinStatus(id: Int, isPinned: Int) {
        Log.d("TAG", "updatePinStatus: "+isPinned.toString())
        viewModelScope.launch {
            _loading.value = true
            try {
                val queryParams = mapOf(
                    "id" to id.toString(),
                    "isPinned" to if (isPinned == 1) 0.toString() else 1.toString()
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .queryDynamicPut(
                        url = ApiEndPoint().updateGoalPinStatus,
                        params = queryParams,
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    getAddedSmartGoal()
                    val json = response.body()?.string()
                    Toast.makeText(MyApplication.appContext,"Pin status updated successfully", Toast.LENGTH_LONG).show()

                } else {
                    _errorMessage.value = "Error Code: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unexpected error"
                e.printStackTrace()
            }
        }
    }


    fun removeGoal(goal: GoalItem) {

        viewModelScope.launch {
            _loading.value = true
            try {
                val queryParams = mapOf(
                    "id" to goal.id.toString(),
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicDelete(
                        url = ApiEndPoint().deleteEmployeeGoal,
                        params = queryParams,
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    getAddedSmartGoal()
                    val json = response.body()?.string()
                    Toast.makeText(MyApplication.appContext,"Goal Deleted successfully!", Toast.LENGTH_LONG).show()

                } else {
                    _errorMessage.value = "Error Code: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unexpected error"
                e.printStackTrace()
            }
        }
    }





}