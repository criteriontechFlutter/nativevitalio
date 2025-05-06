package com.criterion.nativevitalio.UI.fragments

import PrefsManager
import Vital
import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.criterion.nativevitalio.R
import com.criterion.nativevitalio.adapter.DashboardAdapter
import com.criterion.nativevitalio.adapter.ToTakeAdapter
import com.criterion.nativevitalio.databinding.FragmentDashboardBinding
import com.criterion.nativevitalio.networking.RetrofitInstance
import com.criterion.nativevitalio.utils.MyApplication
import com.criterion.nativevitalio.utils.showRetrySnackbar
import com.criterion.nativevitalio.viewmodel.DashboardViewModel
import com.criterion.nativevitalio.viewmodel.PillsReminderViewModal
import com.criterion.nativevitalio.viewmodel.WebSocketState
import com.google.android.material.snackbar.Snackbar
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString

class Dashboard  : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var pillsViewModel: PillsReminderViewModal
    private lateinit var adapter: DashboardAdapter
    private lateinit var toTakeAdapter: ToTakeAdapter
    private var voiceDialog: Dialog? = null
    private var snackbar: Snackbar? = null
    private var currentPage = 0
    private val slideDelay: Long = 2100
    private val handler = Handler(Looper.getMainLooper())
    private var sliderRunnable: Runnable? = null

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var webSocket: WebSocket? = null
    private val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    private val PERMISSION_REQUEST_CODE = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]
        pillsViewModel = ViewModelProvider(this)[PillsReminderViewModal::class.java]

        pillsViewModel.getAllPatientMedication()


        viewModel.isConnected.observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) {
                snackbar?.dismiss()
                viewModel.getVitals()
            } else {
                showRetrySnackbar(
                    ""
                ) { viewModel.getVitals() }
            }
        }


        toTakeAdapter = ToTakeAdapter(
            mutableListOf(),
//            onCheckChanged = { pill, isChecked ->
//                Toast.makeText(requireContext(), "${pill.drugName} marked: $isChecked", Toast.LENGTH_SHORT).show()
//            },
            onItemClick = { pill ->
                val bundle = Bundle().apply {
                    putSerializable("PILL_DATA", pill)
                }
                findNavController().navigate(R.id.action_dashboard_to_intakePills, bundle)

            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = toTakeAdapter

        pillsViewModel.currentDatePillList.observe(viewLifecycleOwner) { pills ->
            toTakeAdapter.updateList(pills)
        }

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

        Glide.with(MyApplication.appContext)
            .load(PrefsManager().getPatient()?.profileUrl.toString())
            .placeholder(R.drawable.baseline_person_24)
            .circleCrop()
            .into(binding.profileImage)

        binding.userName.text = PrefsManager().getPatient()!!.patientName
        binding.profileImage.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_drawer4)
        }


        binding.sosIcon.setOnClickListener {
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.emergency_popup)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            val closeBtn = dialog.findViewById<ImageView>(R.id.closeBtn)
            closeBtn.setOnClickListener {
                dialog.dismiss()
            }

        }


        binding.fluidlayout.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_fluidFragment)
        }

        binding.fluidlayout.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_intakePills)
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

        binding.dietChecklist.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_dietChecklist)
        }

        binding.notificationIconWrapper.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_notificationFragment)
        }

        binding.uploadReport.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_uploadReportHistory2)
        }

        binding.fabAdd.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    showVoiceOverlay()
                    connectWebSocket()
                    checkAndStartAudio()
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    hideVoiceOverlay()
                    stopAudioStreaming()
                    disconnectWebSocket()
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
                    true
                }
                R.id.nav_reminders -> {
                    findNavController().navigate(R.id.action_dashboard_to_pillsReminder)
                    true
                }
                else -> false
            }
        }

        viewModel.webSocketStatus.observe(viewLifecycleOwner) { status ->
            val statusText = when (status) {
                WebSocketState.CONNECTING -> "Connecting..."
                WebSocketState.CONNECTED -> "Connected"
                WebSocketState.DISCONNECTED -> "Disconnected"
                WebSocketState.ERROR -> "Connection Error"
            }

            val statusColor = when (status) {
                WebSocketState.CONNECTED -> android.R.color.holo_green_light
                WebSocketState.DISCONNECTED -> android.R.color.darker_gray
                WebSocketState.ERROR -> android.R.color.holo_red_light
                else -> android.R.color.holo_orange_light
            }

            voiceDialog?.findViewById<TextView>(R.id.websocket_status)?.apply {
                text = statusText
                setTextColor(ContextCompat.getColor(requireContext(), statusColor))
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

    private fun connectWebSocket() {
        viewModel.setWebSocketState( WebSocketState.CONNECTING)
        voiceDialog?.findViewById<TextView>(R.id.voice_transcript)?.text = ""

        val request = Request.Builder().url(RetrofitInstance.holdSpeakWsUrl+ PrefsManager().getPatient()?.pid.toString()).build()
        val client = OkHttpClient()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                viewModel.setWebSocketState(WebSocketState.CONNECTED)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                requireActivity().runOnUiThread {
                    voiceDialog?.findViewById<TextView>(R.id.voice_transcript)?.append(" $text")
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                viewModel.setWebSocketState(WebSocketState.DISCONNECTED)
                requireActivity().runOnUiThread {
                    val transcriptText = voiceDialog?.findViewById<TextView>(R.id.voice_transcript)?.text.toString()
                    if (transcriptText.isNotBlank()) {
                        viewModel.postAnalyzedVoiceData(requireContext(), transcriptText)
                    } else {
                        Toast.makeText(requireContext(), "No speech input found", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                viewModel.setWebSocketState(WebSocketState.ERROR)
            }
        })
    }

    private fun disconnectWebSocket() {
        webSocket?.close(1000, "Closing")
        webSocket = null
    }

    private fun checkAndStartAudio() {
        if (ContextCompat.checkSelfPermission(requireContext(), RECORD_AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            startAudioStreaming()
        } else {
            requestPermissions(arrayOf(RECORD_AUDIO_PERMISSION), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startAudioStreaming()
        } else {
            Toast.makeText(requireContext(), "Microphone permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startAudioStreaming() {
        val bufferSize = AudioRecord.getMinBufferSize(
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (ActivityCompat.checkSelfPermission(
                requireContext(),  // ✅ Corrected
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {


            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            16000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        audioRecord?.startRecording()
        isRecording = true

        Thread {
            val buffer = ByteArray(bufferSize)
            while (isRecording && audioRecord != null) {
                val read = audioRecord!!.read(buffer, 0, buffer.size)
                if (read > 0) {
                    webSocket?.send(buffer.toByteString(0, read))
                }
            }
        }.start()
    }

    private fun stopAudioStreaming() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
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
