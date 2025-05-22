package com.critetiontech.ctvitalio.UI.CTChatBot

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.ChatBotPageAdapter
import com.critetiontech.ctvitalio.adapter.ChatItem
import com.critetiontech.ctvitalio.databinding.FragmentChatBotPageBinding

class ChatBotPage : Fragment() {
    private lateinit var binding: FragmentChatBotPageBinding
    private lateinit var adapter: ChatBotPageAdapter
    private val chatItems = mutableListOf<ChatItem>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatBotPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ChatBotPageAdapter { onChatEvent(it) }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        chatItems.add(ChatItem.MainButtons)
        adapter.submitList(chatItems.toList())
    }

    private fun onChatEvent(event: ChatItem) {
        chatItems.add(event)
        adapter.submitList(chatItems.toList())
        binding.recyclerView.scrollToPosition(chatItems.size - 1)
    }
}