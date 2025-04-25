package com.criterion.nativevitalio.utils

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.critetiontech.ctvitalio.utils.MyApplication
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

class MicVADSocketManager(
    private val token: String,
    private val onServerResponse: (String) -> Unit,
    private val sampleRate: Int = 16000,
    private val rmsThreshold: Int = 2000,
) {
    private val wsUrl = "ws://182.156.200.177:8002/listen?token=$token"

    private var isRunning = false
    private var isSpeaking = false
    private var audioRecord: AudioRecord? = null
    private var vadThread: Thread? = null
    private var webSocket: WebSocket? = null

    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()

    fun start() {
        connectWebSocket()

        if (ActivityCompat.checkSelfPermission(
                MyApplication.appContext,
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
                    val isCurrentlySpeaking = rms > rmsThreshold

                    if (isCurrentlySpeaking != isSpeaking) {
                        isSpeaking = isCurrentlySpeaking
                        if (!isSpeaking) {
                            // User stopped speaking – notify server to process
                            webSocket?.send("{\"event\": \"end\"}")
                        }
                        Log.d("MicVAD", if (isSpeaking) "Speaking..." else "Silent...")
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

    fun stop() {
        isRunning = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
            vadThread?.interrupt()
            webSocket?.close(1000, "Client closed")
        } catch (e: Exception) {
            Log.e("MicVADSocketManager", "Stop error: ${e.message}")
        }
        audioRecord = null
        vadThread = null
        isSpeaking = false
    }

    private fun connectWebSocket() {
        val request = Request.Builder().url(wsUrl).build()
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected to $wsUrl")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Server Response: $text")
                onServerResponse(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error: ${t.message}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Closing: $reason")
            }
        })
    }

    private fun calculateRMS(buffer: ShortArray, readSize: Int): Double {
        var sum = 0.0
        for (i in 0 until readSize) {
            sum += buffer[i] * buffer[i]
        }
        return Math.sqrt(sum / readSize)
    }
}
