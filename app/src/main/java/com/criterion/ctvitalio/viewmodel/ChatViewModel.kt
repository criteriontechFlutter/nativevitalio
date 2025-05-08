package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.ChatMessage
import com.critetiontech.ctvitalio.model.ChatResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.ToastUtils
import com.critetiontech.ctvitalio.viewmodel.BaseViewModel

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatViewModel (application: Application) : BaseViewModel(application) {

    private val _messages = MutableLiveData<MutableList<ChatMessage>>()
    val messages: LiveData<MutableList<ChatMessage>> get() = _messages

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    val _errorMessage = MutableLiveData<String>()


    init {
        // Load initial messages
//        _messages.value = mutableListOf(
//            ChatMessage(
//                id = ,
//                chatDate = ,
//                chatTime = ,
//                fileLength = ,
//                fileName = , fileUrl = , groupId = , isOnline = , isPatient = , message = , messageDateTime = , messageDay = , sendFrom = ,
//                sendFromName = , sendTo = , sendToName = ,
//            ),
//
//        )
    }

    fun sendMessage(text: String) {

    }

    private fun getCurrentTime(): String {
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return format.format(Date())
    }



    fun sentMessages(
        requireContext: Context,
        filePath: String? = null,
        messages: String,

        ) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val patient = PrefsManager().getPatient() ?: return@launch
                val parts = mutableListOf<MultipartBody.Part>()
                fun partFromField(key: String, value: String): MultipartBody.Part {
                    Log.d("sentMessage", "Field: $key = $value")
                    return MultipartBody.Part.createFormData(key, value)
                }

                parts += partFromField("MessageType", "1")
                parts += partFromField("SendTo", PrefsManager().getPatient()!!.doctorID)
                parts += partFromField("Message", messages)
              //  parts += partFromField("SenderTypeID", genderId)
                //parts += partFromField("RecieverTypeId", patient.bloodGroupId)
//                parts += partFromField("SendToName","%.2f".format(height.toDoubleOrNull() ?: 0.0))
//                parts += partFromField("ThumbnailImage", "%.2f".format(weight.toDoubleOrNull() ?: 0.0))
//                parts += partFromField("IsActive", dob)
                parts += partFromField("SendFrom",  PrefsManager().getPatient()!!.id)
              //  parts += partFromField("ChatFile", patient.ageUnitId)
                parts += partFromField("IsGroupChating", false.toString())
                parts += partFromField("IsContact",  false.toString())
                parts += partFromField("GroupId", "0")
                parts += partFromField("IsPatient", "true")


                // Add all text fields

//                parts += partFromField("ProfileURL", patient.profileUrl.replace("https://api.medvantage.tech:7082/", ""))

                // Add file if present
                filePath?.takeIf { it.isNotEmpty() }?.let {
                    val file = File(it)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val filePart = MultipartBody.Part.createFormData("ChatFile", file.name, requestFile)
                    parts += filePart
                    Log.d("UpdateProfile", "File attached: ${file.name}")
                }

                // Print final parts for debug
                parts.forEach { part ->
                    val headers = part.headers?.toString() ?: "No Headers"
                    val bodyString = try {
                        val buffer = okio.Buffer()
                        part.body.writeTo(buffer)
                        buffer.readUtf8()
                    } catch (e: Exception) {
                        "Binary or file content"
                    }
                    val dispositionHeader = part.headers?.get("Content-Disposition")
                    val nameRegex = Regex("name=\"(.*?)\"")
                    val fieldName = nameRegex.find(dispositionHeader ?: "")?.groupValues?.getOrNull(1) ?: "unknown"

                    Log.d("UpdateProfile", "Field: $fieldName = $bodyString")
                }
                // API Call
                val response = RetrofitInstance
                    .createApiService5100(
                        includeAuthHeader=true)
                    .dynamicMultipartPost(
                        url = ApiEndPoint().sentMessageChat,
                        parts = parts
                    )

                if (response.isSuccessful) {
                    getMessages()
                    ToastUtils.showSuccessPopup(requireContext,"Message sent successfully!")

                } else {
                    Log.e("UpdateProfile", "Update failed. Code: ${response.code()}")
                    _errorMessage.postValue("Error: ${response.code()}")
                }

            } catch (e: Exception) {
                Log.e("UpdateProfile", "Exception: ${e.message}", e)
                _errorMessage.postValue(e.message ?: "Unknown error")
            } finally {
                _loading.postValue(false)
            }
        }
    }




    fun getMessages() {
        viewModelScope.launch {
            try {
                _loading.value = true
                val queryParams = mapOf("sendFrom" to PrefsManager().getPatient()!!.id, "sendTo" to PrefsManager().getPatient()!!.doctorID,"isPatient" to true,"groupId" to 0)

                val response = RetrofitInstance
                    .createApiService5100()
                    .dynamicGet(
                        url = ApiEndPoint().getUserChat,
                        params = queryParams
                    )

                if (response.isSuccessful) {
                    _loading.value = false
                    val responseBodyString = response.body()?.string()
                    val type = object : TypeToken<ChatResponse>() {}.type
                    val parsed = Gson().fromJson<ChatResponse>(responseBodyString, type)
                    val allItems = parsed.responseValue
                    _messages.value= allItems!!

                } else {
                    _loading.value = false
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
