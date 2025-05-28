package com.critetiontech.ctvitalio.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.critetiontech.ctvitalio.adapter.ChatItem

class ChatBotPageViewModel : ViewModel() {

    private val _chatItems = MutableLiveData<List<ChatItem>>(emptyList())
    val chatItems: LiveData<List<ChatItem>> get() = _chatItems

    private var hasMainButtonsBeenAdded = false

    fun submitMessage(message: ChatItem) {
        val updated = _chatItems.value!!.toMutableList()
        updated.add(message)
        _chatItems.value = updated
    }
    fun keepOnlyMainButtons() {
        val currentList = _chatItems.value.orEmpty()
        _chatItems.value = currentList.filter { it is ChatItem.MainButtons }
        hasMainButtonsBeenAdded = true
    }
    fun addMainButtonsIfNeeded() {
        if (!hasMainButtonsBeenAdded) {
            val updated = _chatItems.value!!.toMutableList()
            updated.add(ChatItem.MainButtons)
            _chatItems.value = updated
            hasMainButtonsBeenAdded = true
        }
    }

    fun resetChat() {
        _chatItems.value = emptyList()
        hasMainButtonsBeenAdded = false
    }
}