package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.adapter.DailyTipAdapter
import com.critetiontech.ctvitalio.adapter.NotificationAdapter
import com.critetiontech.ctvitalio.adapter.VitalAdapter
import com.critetiontech.ctvitalio.databinding.FragmentNotificationBinding
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel


class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var notificationAdapter: NotificationAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        viewModel.getVitals()
        // 1. Set up adapter and layoutManager ONCE before observing
        notificationAdapter = NotificationAdapter(emptyList())
        binding.notificationRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.notificationRecycler.adapter = notificationAdapter

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }



        // 2. Just update the adapter's data inside the observer
        viewModel.notificationList.observe(viewLifecycleOwner) { list ->
            notificationAdapter.updateList(list)
        }
    }}