package com.critetiontech.ctvitalio.UI.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.critetiontech.ctvitalio.databinding.FragmentVoiceBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit


class VoiceFragment : Fragment() {
    private lateinit var binding: FragmentVoiceBinding


    private var audioRecord: AudioRecord? = null
    private var bufferSize = 0
    private var vadThread: Thread? = null
    private var isSpeaking = false
    private var isRunning = false
    private var webSocket: WebSocket? = null

    private val sampleRate = 16000
    private val rmsThreshold = 2000
    private val token =10

    private val wsUrl = "ws://182.156.200.177:8002/listen?token=$token"

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentVoiceBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebSocket()
        startVAD()
    }



    private fun setupWebSocket() {
        val request = Request.Builder().url(wsUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("Socket", "Connected to $wsUrl")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("Socket", "Message: $text")
                requireActivity().runOnUiThread {
                    binding.txtResult.text = text
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("Socket", "Error: ${t.message}")
            }
        })
    }

    private fun startVAD() {
        bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
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
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        isRunning = true
        audioRecord?.startRecording()

        vadThread = Thread {
            val buffer = ShortArray(bufferSize)

            while (isRunning) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    val rms = calculateRMS(buffer, read)
                    requireActivity().runOnUiThread {
                        binding.audioWaveView.setWaveHeight(rms.toInt())
                    }
                    val currentlySpeaking = rms > rmsThreshold

                    if (currentlySpeaking != isSpeaking) {
                        isSpeaking = currentlySpeaking
                        if (!isSpeaking) {
                            webSocket?.send("{\"event\":\"end\"}")
                        }
                    }

                    if (isSpeaking) {
                        val byteBuffer = ByteArray(read * 2)
                        for (i in 0 until read) {
                            byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
                            byteBuffer[i * 2 + 1] = (buffer[i].toInt() shr 8).toByte()
                        }
                        webSocket?.send(ByteString.of(*byteBuffer))
                    }
                }
            }
        }
        vadThread?.start()
    }

    private fun stopVAD() {
        isRunning = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
            vadThread?.interrupt()
            webSocket?.close(1000, "com.critetiontech.ctvitalio.UI.fragments.User left")
        } catch (e: Exception) {
            Log.e("VAD", "Stop error: ${e.message}")
        }
    }

    private fun calculateRMS(buffer: ShortArray, readSize: Int): Double {
        var sum = 0.0
        for (i in 0 until readSize) {
            sum += buffer[i] * buffer[i]
        }
        return Math.sqrt(sum / readSize)
    }

}