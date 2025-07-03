package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.adapter.ChatBotAdapter
import com.critetiontech.ctvitalio.databinding.FragmentChatbotBinding
import com.critetiontech.ctvitalio.viewmodel.ChatViewModel


class ChatbotFragment : Fragment() {

    private lateinit var binding: FragmentChatbotBinding
    private  val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatBotAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        adapter = ChatBotAdapter { selectedOption ->
            viewModel.sendMessage(selectedOption)
            if (selectedOption.contains("vital", true)) {
               //findNavController().navigate(R.id.action_chatbotFragment_to_vitalsFragment)
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.chatMessages.observe(viewLifecycleOwner) {
            adapter.submitList(it.toList())
            binding.recyclerView.scrollToPosition(it.size - 1)
        }

        binding.sendButton.setOnClickListener {
            val text = binding.messageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendMessage(text)
                binding.messageInput.text.clear()
            }
        }
    }
}