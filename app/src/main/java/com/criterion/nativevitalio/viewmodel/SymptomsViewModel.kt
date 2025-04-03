package com.criterion.nativevitalio.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.Utils.ApiEndPoint
import com.criterion.nativevitalio.networking.RetrofitInstance
import kotlinx.coroutines.launch

class SymptomsViewModel : ViewModel() {


    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    fun getAllPatientMedication( ) {
        _loading.value = true
//        http://52.172.134.222:205/api/v1.0/Patient/getProblemsWithIcon
        viewModelScope.launch {
            try {


                val queryParams = mapOf(
                    "problemName" to "" ,
                    "languageId" to "1"
                )
                // This response is of type Response<ResponseBody>
                val response = RetrofitInstance
                    .createApiService( )
                    .dynamicRawPost(
                        url = ApiEndPoint().getProblemsWithIcon,
                        body = queryParams
                    )
                _loading.value = false
                if (response.isSuccessful) {
                    val json = response.body()?.string()


// Filter the list to only include today's medications


                } else {
                    _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
                _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }


}