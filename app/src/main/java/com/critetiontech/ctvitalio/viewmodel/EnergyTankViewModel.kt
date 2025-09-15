package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPointCorporateModule
import kotlinx.coroutines.launch

class EnergyTankViewModel(application: Application) : BaseViewModel(application){

    val selectedMoodId = MutableLiveData<String>()
    fun onMoodClicked(id:String) {
        selectedMoodId.value =id
    }
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    fun insertEnergyTankMaster(context: Context,energyPercentage:String) {
        viewModelScope.launch {
            _loading.value = true
            try {

                val queryParams = mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString() ,
                    "id" to PrefsManager().getPatient()?.id.toString() ,
                    "energyPercentage" to energyPercentage ,
                    "statusLabel" to "test2",
                    "userId" to "194",
                    "clientId" to "194",
                )


                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicRawPost(
                        url =  ApiEndPointCorporateModule().insertEnergyTankMaster,
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
}