package com.critetiontech.ctvitalio.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.BloodPressureReading
import com.critetiontech.ctvitalio.model.VitalResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VitalHistoryViewModel: ViewModel() {

    private val _bpList = MutableLiveData<List<BloodPressureReading>>()
    val bpList: LiveData<List<BloodPressureReading>> = _bpList


    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading




    fun getBloodPressureRangeHistory(uhid: String,fromDate :String,toDate:String,vitalsID:String) {
        viewModelScope.launch {
            try {
                _loading.value = true

                val queryParams = mapOf(
                    "userId" to 0,
                    "UHID" to uhid,
                    "vitalIdSearchNew" to vitalsID,
                    "vitalDate" to fromDate,
                    "currentDate" to toDate,


                )

                val response = RetrofitInstance
                    .createApiService7082()
                    .dynamicGet(
                        url = ApiEndPoint().getBpRangeHistory,
                        params = queryParams
                    )

                _loading.value = false

                if (response.isSuccessful) {
                    val responseBodyString = response.body()?.string()
                    val type = object : TypeToken<VitalResponse>() {}.type
                    val parsed = Gson().fromJson<VitalResponse>(responseBodyString, type)
                    val patientGraph = parsed.responseValue.patientGraph

                    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val outputTimeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

                    val result = mutableListOf<BloodPressureReading>()

                    patientGraph.forEach { graph ->
                        if (!graph.vitalDateTime.startsWith(today)) return@forEach

                        val detailsArray = JSONArray(graph.vitalDetails)
                        var sys: Int? = null
                        var dia: Int? = null

                        for (i in 0 until detailsArray.length()) {
                            val item = detailsArray.getJSONObject(i)
                            when (item.getString("vitalName")) {
                                "BP_Sys" -> if (sys == null) sys = item.getDouble("vitalValue").toInt()
                                "BP_Dias" -> dia = item.getDouble("vitalValue").toInt()
                            }
                        }

                        if (sys != null && dia != null) {
                            val time = outputTimeFormat.format(inputFormat.parse(graph.vitalDateTime)!!)
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

                } else {
                    // Handle API error
                }

            } catch (e: Exception) {
                _loading.value = false
                e.printStackTrace()
            }
        }
    }

}