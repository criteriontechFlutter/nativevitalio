package com.criterion.nativevitalio.viewmodel

import PrefsManager
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.model.EmergencyContact
import com.criterion.nativevitalio.model.EmergencyContactResponse
import com.criterion.nativevitalio.networking.RetrofitInstance
import com.criterion.nativevitalio.utils.ApiEndPoint
import com.google.gson.Gson
import kotlinx.coroutines.launch

class EmergencyContactViewModel (application: Application) : BaseViewModel(application)  {


    private val _emergencyContactList = MutableLiveData<List<EmergencyContact>>()
    val emergencyContactList: LiveData<List<EmergencyContact>> get() = _emergencyContactList

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    init {
        getEmergencyContacts()
    }




    fun getEmergencyContacts() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val uhid = PrefsManager().getPatient()?.pid.orEmpty()

                val queryParams = mapOf(
                    "pid" to uhid,
                    "clientId" to PrefsManager().getPatient()?.clientId.toString()
                )

                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true)
                    .dynamicGet(
                        url = ApiEndPoint().getEmergencyContact,
                        params = queryParams
                    )



                if (response.isSuccessful) {
                    _loading.value = false
                    val json = response.body()?.string()
                    val parsed = Gson().fromJson(json, EmergencyContactResponse::class.java)
                    _emergencyContactList.value = parsed.responseValue
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



    fun saveEmergencyContact(name: String, phone: String, relation: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val uhid = PrefsManager().getPatient()?.uhID.orEmpty()

                val requestBody = mapOf(
                    "id" to 0,
                    "pid" to PrefsManager().getPatient()?.pid.toString(), // Replace with actual variable or hardcoded value e.g., 233
                    "contactName" to name.toString(),
                    "contactNumber" to phone.toString(),
                    "relationship" to relation.toString(),
                    "userId" to PrefsManager().getPatient()?.userId.toString(),
                    "clientId" to PrefsManager().getPatient()?.clientId.toString()
                )


                val response = RetrofitInstance
                    .createApiService7082()
                    .dynamicRawPost(
                        url = ApiEndPoint().getEmergencyContact,
                        body = requestBody
                    )


                if (response.isSuccessful) {
                    _loading.value = false
                    getEmergencyContacts()

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