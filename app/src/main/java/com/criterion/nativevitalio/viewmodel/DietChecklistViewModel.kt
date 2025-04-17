package com.criterion.nativevitalio.viewmodel

import PrefsManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.model.DietItemModel
import com.criterion.nativevitalio.networking.RetrofitInstance
import com.criterion.nativevitalio.utils.ApiEndPoint
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DietChecklistViewModel: ViewModel() {

    private val _dietList = MutableLiveData<List<DietItemModel>>()
    val dietList: LiveData<List<DietItemModel>> get() = _dietList

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun getFoodIntake() {
        _loading.value = true

        viewModelScope.launch {
            try {
                val queryParams = mapOf(
                    "Uhid" to PrefsManager().getPatient()?.uhID.toString(),
                    "entryType" to "D",
                    "fromDate" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                )

                val response = RetrofitInstance
                    .createApiService7096(includeAuthHeader=true)
                    .dynamicGet(
                        url = ApiEndPoint().getFoodIntake,
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val rootObj = Gson().fromJson(json, JsonObject::class.java)
                    val listJson = rootObj.getAsJsonArray("foodIntakeList")

                    val type = object : TypeToken<List<DietItemModel>>() {}.type
                    val parsedList: List<DietItemModel> = Gson().fromJson(listJson, type)

                    _dietList.postValue(parsedList)
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


    FoodIntake/IntakeByDietID
}