package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.R
 import com.critetiontech.ctvitalio.model.WatchModel
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class ConnectSmartWatchViewModel (application: Application) : BaseViewModel(application){


//    fun removeWatch(item: WatchModel) {
//        _watchList.value = _watchList.value?.filter { it != item }
//    }
//
//    fun addWatch(item: WatchModel) {
//        _watchList.value = _watchList.value?.toMutableList()?.apply { add(item) }
//    }
  fun   insertWatchDetails(watchdata: JSONObject)   {
            viewModelScope.launch {
        // This response is of type Response<ResponseBody>

                try {
        val queryParams = mapOf(
                /* "id": 0, */
                "token" to watchdata.getString("token")   ,
                "brand" to   watchdata.getString("brand")   ,
                "model" to   watchdata.getString("model")    ,
                "userId" to  PrefsManager().getPatient()?.userId.toString(),
                "clientId" to PrefsManager().getPatient()?.clientId.toString(),
        );

        val response = RetrofitInstance
            .createApiService( )
            .dynamicRawPost(
                url = ApiEndPoint().insertWatch,
                body = queryParams
            )

        if (response.isSuccessful) {
            saveUser(   watchdata.getString("token").toString(),)
            getWatchDetails()
        } else {

        }  } catch (e: Exception) {
                    Log.e("Firestore", "Failed to add user: ${e.message}")
                }
            }

    }
fun saveUser(

    token: String,

    ) {
    val users = FirebaseFirestore.getInstance().collection("watch_data")

    try {
//        val userData = PrefsManager().getPatient()?.toJson()
        val patient = PrefsManager().getPatient()

        patient?.isHoldToSpeak = 0 // Set it explicitly

        val userData = patient?.toJson()

        if (userData != null) {
            users.document(token).set(userData)
        } // using Kotlin coroutines
        Log.d("Firestore", "User added successfully!")

        // replace with your own function
    } catch (e: Exception) {
        Log.e("Firestore", "Failed to add user: ${e.message}")
    }
}


    private val _watchList = MutableLiveData<List<WatchModel>>()
    val watchList: LiveData<List<WatchModel>> = _watchList
    fun getWatchDetails() {
        viewModelScope.launch {
            val params = mapOf(
                "clientId" to PrefsManager().getPatient()?.clientId.toString(), // Fixed: clientId
                "userId" to PrefsManager().getPatient()?.userId.toString(),
            )

            try {
                val response = RetrofitInstance
                    .createApiService()
                    .dynamicGet(
                        url = ApiEndPoint().getWatchDetails,
                        params = params
                    )


                if (response.isSuccessful) {
                    val dataStr = response.body()?.string()
                    val data = JSONObject(dataStr ?: "{}")

                    Log.d("WatchDetails", "data: $data")

                    if (data.optInt("status") == 0) {
                        _watchList.value = emptyList()
                    } else {
                        val jsonArray = data.optJSONArray("responseValue") ?: JSONArray()
                        val watchList: List<WatchModel> = Gson().fromJson(
                            data.optJSONArray("responseValue").toString(),
                            object : TypeToken<List<WatchModel>>() {}.type
                        )
                        _watchList.value = watchList
                    }

                    val token = data.optJSONArray("responseValue")
                        ?.optJSONObject(0)
                        ?.optString("token")

                    if (!token.isNullOrEmpty()) {
                        saveUser(token)
                    }
                } else {
                    _watchList.value = emptyList()
                    Log.e("WatchDetails", "API failed: ${response.code()}")
                }
            } catch (e: Exception) {
                _watchList.value = emptyList()
                e.printStackTrace()
            }
        }
    }
    @SuppressLint("SuspiciousIndentation")
    fun  deleteWatchDetails(context: Context, id:String, token: String)   {


      viewModelScope.launch {
          try {
        val params= mapOf(
//            "Id" to id
            "token" to token

        );


      val response = RetrofitInstance
          .createApiService()
          .dynamicDelete(
              url = ApiEndPoint().deleteWatchDetails,
              params = params
          )

          Log.e("WatchDetails", "deleteWatchDetails: ${response.isSuccessful}")


          if (response.isSuccessful) {
              deleteDocument(context ,token.toString())
              getWatchDetails()
          }
          else{

          }  } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error deleting document: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
      }
    }



    fun deleteDocument(context: Context, token: String) {
        try {
            FirebaseFirestore.getInstance()
                .collection("watch_data")
                .document(token)
                .delete()


            Toast.makeText(context, "Watch removed successfully.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error deleting document: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun Any.toJson(): Map<String, Any> {
        val json = Gson().toJson(this)
        return Gson().fromJson(json, object : TypeToken<Map<String, Any>>() {}.type)
    }
}