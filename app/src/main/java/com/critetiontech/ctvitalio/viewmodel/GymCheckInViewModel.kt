package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.MyApplication
import kotlinx.coroutines.launch
import org.json.JSONObject

class GymCheckInViewModel (application: Application) : AndroidViewModel(application) {








    val qrResult = MutableLiveData<String>()
    val gymCheckInResponse = MutableLiveData<String>()

    fun onQrScanned(code: String, gymLatitude: Double?, gymLongitude: Double?) {
        qrResult.value = code
        fetchGymCheckIn(code,gymLatitude,gymLongitude)
    }

    private val _apiStatus = MutableLiveData<Boolean>()
    val apiStatus: LiveData<Boolean> = _apiStatus



    fun fetchGymCheckIn(code: String, gymLatitude: Double?, gymLongitude: Double?) {

        viewModelScope.launch {

            try {

                val response = RetrofitInstance
                    .createApiService()
                    .dynamicRawPost(
                        url = "api/GymAttendance/CheckIn",

                        body = mapOf(
                            "qrCode" to code,
                            "longitude" to gymLatitude.toString(),
                            "latitude" to gymLongitude.toString(),
                            "deviceId" to "1",
                            "empId" to  PrefsManager().getPatient()?.id.toString()
                        )
                    )

                if (response.code()==200) {

                    val json = response.body()?.string()

                    val jsonObj = JSONObject(json)

                    val arr = jsonObj.getJSONArray("responseValue")

                    Log.d("TAG", "fetchGymCheckIn: "+arr.toString());



                } else {

                    val json = response.body()?.string()
                    val jsonObj = JSONObject(json)
                    val arr = jsonObj.getJSONArray("message")
                    Toast.makeText(MyApplication.appContext,jsonObj.getJSONArray("message").toString(),
                        Toast.LENGTH_LONG).show()
                    Log.e("GYM_API", "FAILED → ${response.errorBody()?.string()}")
                    _apiStatus.postValue(false)

                }

            } catch (e: Exception) {

                Log.e("GYM_API", e.localizedMessage.toString())
                _apiStatus.postValue(false)

            }
        }
    }
}