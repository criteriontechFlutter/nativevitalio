package com.criterion.nativevitalio.UI.fragments

import Vital
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.databinding.FragmentDashboardBinding
import com.criterion.nativevitalio.viewmodel.DashboardViewModel
import com.criterion.nativevitalio.viewmodel.VitalDetailsViewModel
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.widget.ImageView
import com.criterion.nativevitalio.adapter.DashboardAdapter
import com.criterion.nativevitalio.adapter.VitalDetailsAdapter

class Dashboard  : Fragment() {

    private lateinit var binding: FragmentDashboardBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var adapter: DashboardAdapter
    private var voiceDialog: Dialog? = null

    private var currentPage = 0
    private val slideDelay: Long = 2100
    private val handler = Handler(Looper.getMainLooper())
    private var sliderRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        viewModel.getVitals()

        viewModel.vitalList.observe(viewLifecycleOwner) { vitalList ->

            val bpSys = vitalList.find { it.vitalName.equals("BP_Sys", ignoreCase = true) }
            val bpDia = vitalList.find { it.vitalName.equals("BP_Dias", ignoreCase = true) }

            val filtered = vitalList.filterNot {
                it.vitalName.equals("BP_Sys", ignoreCase = true) ||
                        it.vitalName.equals("BP_Dias", ignoreCase = true)
            }.toMutableList()

            if (bpSys != null && bpDia != null) {
                val bpVital = Vital().apply {
                    vitalName = "Blood Pressure"
                    vitalValue = 0.0
                    unit = "${bpSys.vitalValue.toInt()}/${bpDia.vitalValue.toInt()} ${bpSys.unit}"
                    vitalDateTime = bpSys.vitalDateTime
                }
                filtered.add(bpVital)
            }

            adapter = DashboardAdapter(requireContext(), filtered) { vitalType ->
                val bundle = Bundle().apply {
                    putString("vitalType", vitalType)
                }
                findNavController().navigate(R.id.action_dashboard_to_connection, bundle)
            }
            binding.vitalsSlider.adapter = adapter
            binding.vitalsIndicator.setupWithViewPager(binding.vitalsSlider)

            sliderRunnable = object : Runnable {
                override fun run() {
                    if (adapter.count > 0) {
                        currentPage = (currentPage + 1) % adapter.count
                        binding.vitalsSlider.setCurrentItem(currentPage, true)
                        handler.postDelayed(this, slideDelay)
                    }
                }
            }
            handler.removeCallbacksAndMessages(null)
            currentPage = 0
            binding.vitalsSlider.setCurrentItem(currentPage, false)
            handler.postDelayed(sliderRunnable!!, slideDelay)
        }

        binding.profileSection.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_drawer4)
        }

        binding.fluidlayout.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_fluidFragment)
        }

        binding.pillsReminder.setOnClickListener {
            findNavController().navigate(R.id.pillsReminder)
        }

        binding.symptomsTracker.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_symptomsFragment)
        }

        binding.vitalDetails.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_vitalDetail)
        }

        binding.fabAdd.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    showVoiceOverlay()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    hideVoiceOverlay()
                    true
                }
                else -> false
            }
        }


        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_chat -> {
                    findNavController().navigate(R.id.action_dashboard_to_chatFragment)
                    true
                }
                R.id.nav_home -> {
                    Toast.makeText(requireContext(), "Vitals clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_reminders -> {
                    Toast.makeText(requireContext(), "Settings clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }



    }

    private fun showVoiceOverlay() {
        if (voiceDialog == null) {
            voiceDialog = Dialog(requireContext(), android.R.style.Theme_DeviceDefault_NoActionBar)
            voiceDialog?.setContentView(R.layout.dialog_voice_input)
            voiceDialog?.setCancelable(false)
            voiceDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
        voiceDialog?.show()
    }

    private fun hideVoiceOverlay() {
        voiceDialog?.dismiss()
    }

    override fun onResume() {
        super.onResume()
        handler.removeCallbacksAndMessages(null)
        currentPage = 0
        binding.vitalsSlider.setCurrentItem(currentPage, false)
        sliderRunnable?.let { handler.postDelayed(it, slideDelay) }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }
}
