package com.criterion.nativevitalio.UI.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.criterion.nativevitalio.adapter.WatchAdapter
import com.criterion.nativevitalio.viewmodel.ConnectSmartWatchViewModel
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentConnectSmartWatchBinding
import com.criterion.nativevitalio.model.WatchModel
import java.util.Random


class ConnectSmartWatchFragment : Fragment() {
    private lateinit var binding: FragmentConnectSmartWatchBinding
    private  val viewModel: ConnectSmartWatchViewModel by viewModels()
    private lateinit var watchAdapter: WatchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentConnectSmartWatchBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        watchAdapter = viewModel.watchList.value?.let {
            WatchAdapter(it) { item ->
                viewModel.removeWatch(item)
            }
        }!!

        binding.recyclerWatchList.apply {
            adapter = watchAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.btnAddNew.setOnClickListener {
            viewModel.addWatch(
                WatchModel("New Watch ${Random().nextInt(999)}", "100%", R.drawable.watch)
            )
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


    }

}