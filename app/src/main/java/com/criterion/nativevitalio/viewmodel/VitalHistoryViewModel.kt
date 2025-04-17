package com.criterion.nativevitalio.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.criterion.nativevitalio.model.BloodPressureReading
import com.criterion.nativevitalio.model.FluidSummaryResponse
import com.criterion.nativevitalio.networking.RetrofitInstance
import com.criterion.nativevitalio.utils.ApiEndPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VitalHistoryViewModel: ViewModel() {

    private val _bpList = MutableLiveData<List<BloodPressureReading>>()
    val bpList: LiveData<List<BloodPressureReading>> = _bpList


    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    fun loadTodayBPList(json: String) {
        val result = mutableListOf<BloodPressureReading>()

        val root = JSONObject(json)
        val graphArray = root
            .getJSONObject("responseValue")
            .getJSONArray("patientGraph")

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        for (i in 0 until graphArray.length()) {
            val entry = graphArray.getJSONObject(i)
            val vitalDateTime = entry.getString("vitalDateTime")
            if (!vitalDateTime.startsWith(today)) continue

            val detailsJson = JSONArray(entry.getString("vitalDetails"))
            var sys: Int? = null
            var dia: Int? = null

            for (j in 0 until detailsJson.length()) {
                val item = detailsJson.getJSONObject(j)
                when (item.getString("vitalName")) {
                    "BP_Sys" -> if (sys == null) sys = item.getDouble("vitalValue").toInt()
                    "BP_Dias" -> dia = item.getDouble("vitalValue").toInt()
                }
            }

            if (sys != null && dia != null) {
                val time = outputTimeFormat.format(inputFormat.parse(vitalDateTime)!!)
                result.add(
                    BloodPressureReading(
                        time = time,
                        sys = sys,
                        dia = dia,
                        bp = "$sys/$dia mmHg"
                    )
                )
            }
        }

        _bpList.postValue(result)
    }



    fun getBloodPressureRangeHistory(uhid: String, fromDate: String, toDate: String) {
        viewModelScope.launch {
            try {
                _loading.value = true
                val queryParams = mapOf(
                    "Uhid" to uhid,
                    "fromDate" to fromDate,
                    "toDate" to toDate
                )

                val response = RetrofitInstance
                    .createApiService7096()
                    .dynamicGet(
                        url = ApiEndPoint().getFluidIntakeDetailsByRange,
                        params = queryParams
                    )
                _loading.value = false
                if (response.isSuccessful) {
                    val responseBodyString = response.body()?.string()
                    val type = object : TypeToken<FluidSummaryResponse>() {}.type
                    val parsed = Gson().fromJson<FluidSummaryResponse>(responseBodyString, type)
                    val allItems = parsed.responseValue
                 //   _intakeListRangeWise.value= allItems
//                    val filteredList = parsed.responseValue.mapNotNull {
//
//                        Log.d("TAG", "fetchManualFluidIntake: "+intakeList.value)
//                        val qty = it.quantity.toFloatOrNull() ?: 0f
//                        if (qty > 0f) {
//                            FluidType(
//                                name = it.foodName.trim(),
//                                amount = qty.toInt(),
//                                color = mapColorForFood(it.foodName) ,
//                                id= it.foodID
//                            )
//                        } else null
//                    }
                    //  _fluidList.value = filteredList

                } else {
                  //  _errorMessage.value = "Error: ${response.code()}"
                }

            } catch (e: Exception) {
                _loading.value = false
               // _errorMessage.value = e.message ?: "Unknown error occurred"
                e.printStackTrace()
            }
        }
    }
}