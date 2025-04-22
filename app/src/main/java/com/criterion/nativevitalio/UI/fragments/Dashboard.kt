package com.critetiontech.ctvitalio.UI.fragments

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
import com.bumptech.glide.Glide
import com.criterion.nativevitalio.utils.showRetrySnackbar
import com.critetiontech.ctvitalio.R
import com.critetiontech.ctvitalio.adapter.DashboardAdapter
import com.critetiontech.ctvitalio.databinding.FragmentDashboardBinding
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.viewmodel.DashboardViewModel
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
    private lateinit var adapter: DashboardAdapter
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

//        binding.notificationIconWrapper.setOnClickListener {
//            findNavController().navigate(R.id.action_dashboard_to_notificationFragment)
//        }

        binding.uploadReport.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_uploadReport)
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

    private fun connectWebSocket() {
        val url = "ws://182.156.200.177:8002/listen?token=1"
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                requireActivity().runOnUiThread {
                    voiceDialog?.findViewById<TextView>(R.id.voice_transcript)?.let {
                        it.text = "${it.text} $text"
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                voiceDialog?.findViewById<TextView>(R.id.voice_transcript)?.let { transcriptView ->
                    val transcriptText = transcriptView.text.toString()
                    if (transcriptText.isNotBlank()) {
                        viewModel.postAnalyzedVoiceData(requireContext(), transcriptText)
                    } else {
                        Toast.makeText(requireContext(), "No speech input found", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("❌ WebSocket Error: ${t.message}")
            }

            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("✅ WebSocket Connected")
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
