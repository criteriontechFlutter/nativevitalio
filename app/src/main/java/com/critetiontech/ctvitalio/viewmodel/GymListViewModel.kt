package com.critetiontech.ctvitalio.viewmodel

import android.app.Application


import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.Gym
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import kotlinx.coroutines.launch
import org.json.JSONObject

class GymViewModel(application: Application) : AndroidViewModel(application) {

    private val _gymList = MutableLiveData<List<Gym>>()
    val gymList: LiveData<List<Gym>> = _gymList

    private val _apiStatus = MutableLiveData<Boolean>()
    val apiStatus: LiveData<Boolean> = _apiStatus


    fun getAllGymMasters() {

        viewModelScope.launch {

            try {

                val response = RetrofitInstance
                    .createApiService()
                    .dynamicGet(
                        url = "api/GymMasters/GetAllGymMasters",
                        params = emptyMap()
                    )

                if (response.isSuccessful) {

                    val json = response.body()?.string()

                    val jsonObj = JSONObject(json)

                    val arr = jsonObj.getJSONArray("responseValue")

                    val list = mutableListOf<Gym>()

                    for (i in 0 until arr.length()) {

                        val obj = arr.getJSONObject(i)

                        val gym = Gym(
                            gymName = obj.getString("gymName"),
                            description = obj.getString("description"),
                            contactNumber = obj.getString("contactNumber"),
                            email = obj.getString("email"),
                            address = obj.getString("address"),
                            city = obj.getString("city"),
                            state = obj.getString("state"),
                            country = obj.getString("country"),
                            latitude = obj.getDouble("latitude"),
                            longitude = obj.getDouble("longitude"),
                            imageURL = obj.getString("imageURL")
                                .replace("\\", "/")
                        )

                        list.add(gym)
                    }

                    _gymList.postValue(list)
                    _apiStatus.postValue(true)

                } else {

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