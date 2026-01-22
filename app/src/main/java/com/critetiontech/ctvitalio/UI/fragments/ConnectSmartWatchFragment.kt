package com.critetiontech.ctvitalio.UI.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.critetiontech.ctvitalio.adapter.WatchAdapter
import com.critetiontech.ctvitalio.viewmodel.ConnectSmartWatchViewModel
import com.critetiontech.ctvitalio.databinding.FragmentConnectSmartWatchBinding
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONObject


class ConnectSmartWatchFragment : Fragment() {
    private lateinit var binding: FragmentConnectSmartWatchBinding
    private  val viewModel: ConnectSmartWatchViewModel by viewModels()
    private lateinit var watchAdapter: WatchAdapter
     // QR scanner launcher
    private val qrScanner = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {

            val json = JSONObject(result.contents)
            val token = json.getString("token")
            Log.d("Firestore", "com.critetiontech.ctvitalio.UI.fragments.User added result!${json}")

                viewModel.insertWatchDetails(watchdata = json)


            Toast.makeText(requireContext(), ": ${result.contents}", Toast.LENGTH_LONG).show()
            // You can pass result.contents to ViewModel or handle as needed
        } else {
            Log.d("requireContext", "requireContext added result! ")
            Toast.makeText(requireContext(), "Scan cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentConnectSmartWatchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and Adapter
        watchAdapter = WatchAdapter { item ->
            viewModel.deleteWatchDetails(requireContext() ,item.id.toString(),item.token.toString())

            
        }

        binding.recyclerWatchList.apply {
            adapter = watchAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Observe LiveData from ViewModel
        viewModel.watchList.observe(viewLifecycleOwner) { list ->
            list?.let {
                watchAdapter.setData(it)
            }
        }

        viewModel.getWatchDetails() // Triggers data load

        binding.btnAddNew.setOnClickListener {
            val options = ScanOptions().apply {
                setPrompt("Scan a QR Code")
                setBeepEnabled(true)
                setOrientationLocked(false)
                setCameraId(0)
            }
            qrScanner.launch(options)
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }




}