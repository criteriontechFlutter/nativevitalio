package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class MedicationViewModel(application: Application) : AndroidViewModel(application) {





    fun getEmployeeMedicineIntakeByDate( ) {
        viewModelScope.launch {
            try {
                val queryParams =  mapOf(
                    "pid" to PrefsManager().getPatient()?.id.toString(),
                    "givenDate" to convertDateFormat("2025-11-10"),
                    "clientId" to PrefsManager().getPatient()?.clientId.toString()
                )
                val response = RetrofitInstance
                    .createApiService(includeAuthHeader = true,  )
                    .dynamicGet(
                        url = "api/EmployeeMedicineIntake/GetEmployeeMedicineIntakeByDate",
                        params = queryParams
                    )

                if (response.isSuccessful) {


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