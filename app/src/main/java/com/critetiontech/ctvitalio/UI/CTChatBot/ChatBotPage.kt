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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.ChatBotPageAdapter
import com.critetiontech.ctvitalio.adapter.ChatItem
import com.critetiontech.ctvitalio.databinding.FragmentChatBotPageBinding
import com.critetiontech.ctvitalio.viewmodel.ChatBotPageViewModel
import com.critetiontech.ctvitalio.viewmodel.PillsReminderViewModal

class ChatBotPage : Fragment() {
    private lateinit var binding: FragmentChatBotPageBinding
    private lateinit var adapter: ChatBotPageAdapter
    private lateinit var viewModel: ChatBotPageViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBotPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[ChatBotPageViewModel::class.java]

        adapter = ChatBotPageAdapter(
            onEvent = { onChatEvent(it) },
            onNavigate = { destinationId -> findNavController().navigate(destinationId) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Observe chat list
        viewModel.chatItems.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            binding.recyclerView.scrollToPosition(it.size - 1)
        })

        // Add main buttons if not already added
        viewModel.addMainButtonsIfNeeded()

        binding.actionButton.setOnClickListener {
            viewModel.keepOnlyMainButtons()
        }
    }

    private fun onChatEvent(event: ChatItem) {
        viewModel.submitMessage(event)
    }
}
