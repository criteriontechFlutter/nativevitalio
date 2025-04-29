package com.criterion.nativevitalio.UI.fragments

import ChatAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.criterion.nativevitalio.databinding.FragmentChatBinding
import com.critetiontech.ctvitalio.viewmodel.ChatViewModel


class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: ChatAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        viewModel.getMessages()

        // Observe chat messages
        activity?.let {
            viewModel.messages.observe(it) { messageList ->
                adapter = ChatAdapter(messageList.reversed())
                binding.chatRecycler.layoutManager = LinearLayoutManager(context)
                binding.chatRecycler.adapter = adapter
                binding.chatRecycler.scrollToPosition(messageList.size - 1)
            }
        }



        binding.voiceBtn.setOnClickListener {

                viewModel.sentMessages(requireContext(),"",binding.messageBox.text.toString())
                binding.messageBox.clearFocus()



        }

        binding.chatToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun sendMessage() {
        val text = binding.messageBox.text.toString().trim()
        if (text.isNotEmpty()) {
            viewModel.sendMessage(text)
            binding.messageBox.text?.clear()
        }


    }



}