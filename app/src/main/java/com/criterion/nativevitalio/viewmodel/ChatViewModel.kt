package com.critetiontech.ctvitalio.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.criterion.nativevitalio.viewmodel.BaseViewModel
import com.critetiontech.ctvitalio.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatViewModel (application: Application) : BaseViewModel(application) {

    private val _messages = MutableLiveData<MutableList<ChatMessage>>()
    val messages: LiveData<MutableList<ChatMessage>> get() = _messages

    init {
        // Load initial messages
        _messages.value = mutableListOf(
            ChatMessage("Good morning! How are you feeling today?", "12:07 PM", false),
            ChatMessage("Good morning, Doctor. I’m feeling fine, just here for my check-up.", "12:07 PM", true)
        )
    }

    fun sendMessage(text: String) {
        val newMessage = ChatMessage(text, getCurrentTime(), true)
        _messages.value?.apply {
            add(newMessage)
            _messages.value = this
        }
    }

    private fun getCurrentTime(): String {
        val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return format.format(Date())
    }
}
