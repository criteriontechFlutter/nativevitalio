package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.critetiontech.ctvitalio.model.BotMessage
import com.critetiontech.ctvitalio.model.ChatMessage
import com.critetiontech.ctvitalio.model.ChatResponse
import com.critetiontech.ctvitalio.model.ChatResponseRepo
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.critetiontech.ctvitalio.utils.ToastUtils
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
    private val sharedPreferences = application.getSharedPreferences("ChatPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading
    val _errorMessage = MutableLiveData<String>()
    private val _chatMessages = MutableLiveData<MutableList<BotMessage>>(mutableListOf())
    val chatMessages: LiveData<MutableList<BotMessage>> = _chatMessages

    init {
        val savedJson = sharedPreferences.getString("chat_history", null)
        if (savedJson != null) {
            val type = object : TypeToken<MutableList<BotMessage>>() {}.type
            _chatMessages.value = gson.fromJson(savedJson, type)
        } else {
            _chatMessages.value = mutableListOf(
                BotMessage("Hi, I'm your health assistant!\nनमस्ते! मैं आपकी हेल्थ असिस्टेंट हूँ। आप मुझसे vitals, symptoms, fluids, medicines, या थीम के बारे में पूछ सकते हैं।", false, listOf("Vitals", "Symptoms", "Fluids", "Medicines", "Theme"))
            )
        }
    }


    private fun getCurrentTime(): String {
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return format.format(Date())
    }


    fun sendMessage(input: String) {
        if (input.lowercase().contains("reset") || input.lowercase().contains("start over")) {
            _chatMessages.value = mutableListOf(
                BotMessage("Conversation reset. How can I assist you now?", false, listOf("Vitals", "Symptoms", "Fluids", "Medicines", "Theme"))
            )
            saveChatHistory()
            return
        }

        val currentList = _chatMessages.value ?: mutableListOf()
        currentList.add(BotMessage(input, true))
        _chatMessages.value = currentList.toMutableList()

        Handler(Looper.getMainLooper()).postDelayed({
            val botReply = ChatResponseRepo.getResponse(input)
            currentList.add(botReply)
            _chatMessages.value = currentList.toMutableList()
            saveChatHistory()
        }, 800)
    }

    fun saveChatHistory() {
        val json = gson.toJson(_chatMessages.value)
        sharedPreferences.edit().putString("chat_history", json).apply()
    }

    fun clearChatHistory() {
        sharedPreferences.edit().remove("chat_history").apply()
        _chatMessages.value = mutableListOf(
            BotMessage("Chat reset. I'm here to help. What do you want to do?", false, listOf("Vitals", "Symptoms", "Fluids", "Medicines", "Theme"))
        )
    }

    fun recognizeVoiceInput(context: Context, onResult: (String) -> Unit) {
        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val intent = RecognizerIntent.getVoiceDetailsIntent(context).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
        }

        speechRecognizer.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onResults(results: android.os.Bundle?) {
                val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!data.isNullOrEmpty()) {
                    onResult(data[0])
                }
                speechRecognizer.destroy()
            }
            override fun onError(error: Int) {
                Toast.makeText(context, "Voice recognition failed", Toast.LENGTH_SHORT).show()
                speechRecognizer.destroy()
            }
            override fun onReadyForSpeech(params: android.os.Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: android.os.Bundle?) {}
            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })

        speechRecognizer.startListening(intent)
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
                parts += partFromField("SendFrom",  PrefsManager().getPatient()!!.id.toString())
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
