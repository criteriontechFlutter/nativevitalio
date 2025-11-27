package com.critetiontech.ctvitalio.viewmodel

import AllMedicine
import LoggedMedicine
 import MedicineIntakeResponse
 import PrefsManager
import VitalsResponse
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicationViewModel(application: Application) : AndroidViewModel(application) {



    private val _employeeMedicineIntakeLiveData = MutableLiveData<List<LoggedMedicine>>()
    val employeeMedicineIntakeLiveData: LiveData<List<LoggedMedicine>> = _employeeMedicineIntakeLiveData

    private val _allMedicineListLiveData = MutableLiveData<List<AllMedicine>>()
    val allMedicineListLiveData: LiveData<List<AllMedicine>> = _allMedicineListLiveData
    fun getEmployeeMedicineIntakeByDate(givenDate: String ) {
        viewModelScope.launch {
            try {
                val queryParams =  mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString(),
                    "givenDate" to givenDate,
                    "clientId" to PrefsManager().getPatient()?.clientId.toString()
                )
                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true,  )
                    .dynamicGet(
                        url = "api/EmployeeMedicineIntake/GetEmployeeMedicineIntakeByDate",
                        params = queryParams
                    )

                if (response.isSuccessful) {
                    Log.e("RAW_RESPONSE", response.body().toString())  // memory reference (expected)

                    if(response.body() != null){

                        val json = response.body()?.string()        // << FIX: Extract real JSON
                        val data = Gson().fromJson(json, MedicineIntakeResponse::class.java)

                        Log.e("PARSED_DATA", data.toString())       // <-- now prints actual JSON model

                        _employeeMedicineIntakeLiveData.postValue(data.responseValue.loggedMedicines)
                        _allMedicineListLiveData.postValue(data.responseValue.allMedicines)
                    }

                }else {
                }

            } catch (e: Exception) {
                Log.d("ITEM", e.toString())
            }
        }
    }
    fun convertDateFormat(date: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = inputFormat.parse(date)
        return outputFormat.format(parsedDate!!)
    }
}