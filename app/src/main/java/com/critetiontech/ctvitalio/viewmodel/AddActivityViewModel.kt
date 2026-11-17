package com.example.vitalio_pragya.viewmodel

import com.critetiontech.vitalioCorporate.utils.PrefsManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.vitalioCorporate.model.ActivityModel
import com.critetiontech.vitalioCorporate.model.ActivityResponse
import com.critetiontech.vitalioCorporate.model.EmployeeActivity
import com.critetiontech.vitalioCorporate.model.EmployeeActivityResponse
import com.critetiontech.vitalioCorporate.networking.RetrofitInstance
import com.critetiontech.vitalioCorporate.utils.ApiEndPointCorporateModule
import com.google.gson.Gson
import kotlinx.coroutines.launch

class AddActivityViewModel(application: Application) : AndroidViewModel(application) {

    // 🔹 Activity Master (General List)
    private val _activityMasterList = MutableLiveData<List<ActivityModel>>()
    val activityMasterList: LiveData<List<ActivityModel>> = _activityMasterList

    // 🔹 Employee Activity (User Based)
    private val _employeeActivityList = MutableLiveData<List<EmployeeActivity>>()
    val employeeActivityList: LiveData<List<EmployeeActivity>> = _employeeActivityList


    // --------------------------------------------------
    // 🔥 API 1 → Get Activity Master Data (General List)
    // --------------------------------------------------
    fun getAllActivityMaster() {
        viewModelScope.launch {
            try {
                val response =  RetrofitInstance.createApiService().dynamicGet(
                        url = ApiEndPointCorporateModule().getAllActivityMaster,
                        params = emptyMap()
                    )

                if (response.isSuccessful) {
                    val data = Gson().fromJson(response.body()?.string(), ActivityResponse::class.java)
                    _activityMasterList.postValue(data.responseValue)   // store main list
                }

            } catch (e: Exception) {
                Log.e("MASTER API ERROR", e.message.toString())
            }
        }
    }


    // --------------------------------------------------
    // 🔥 API 2 → Get Activities For Employee
    // --------------------------------------------------
    fun getAllEmployeeActivity(clientId: String = "194") {
        viewModelScope.launch {
            try {
                val response =
                    RetrofitInstance.createApiService().dynamicGet(
                        url = ApiEndPointCorporateModule().getAllEmployeeActivity,
                        params = mapOf(
                            "clientId" to clientId,
                            "pid" to (PrefsManager().getPatient()?.id ?: "").toString()
                        )
                    )

                if (response.isSuccessful) {
                    val data =
                        Gson().fromJson(response.body()?.string(), EmployeeActivityResponse::class.java)
                    _employeeActivityList.postValue(data.responseValue) // store list
                }

            } catch (e: Exception) {
                Log.e("EMPLOYEE API ERROR", e.message.toString())
            }
        }
    }
}