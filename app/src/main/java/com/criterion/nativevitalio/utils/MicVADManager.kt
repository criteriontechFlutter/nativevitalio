//package com.criterion.nativevitalio.utils
//
//import PrefsManager
//import android.Manifest
//import android.content.pm.PackageManager
//import android.media.AudioFormat
//import android.media.AudioRecord
//import android.media.MediaRecorder
//import android.util.Log
//import androidx.core.app.ActivityCompat
//import com.criterion.nativevitalio.networking.RetrofitInstance
//import com.criterion.nativevitalio.utils.MyApplication
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.Response
//import okhttp3.WebSocket
//import okhttp3.WebSocketListener
//import okio.ByteString
//import okio.ByteString.Companion.toByteString
//import java.util.UUID
//import java.util.concurrent.TimeUnit
//
//class MicVADSocketManager(
//    private val onServerResponse: (String) -> Unit,
//    private val sampleRate: Int = 16000,
//    private val rmsThreshold: Int = 1000,
//) {
//
//    // 🔁 Token is generated dynamically (you can replace with API-generated one)
//    private val token: String = UUID.randomUUID().toString()
//    private val wsUrl = "ws://182.156.200.177:8002/listen?token=$token"
//
//    private var isRunning = false
//    private var isSpeaking = false
//    private var audioRecord: AudioRecord? = null
//    private var vadThread: Thread? = null
//    private var webSocket: WebSocket? = null
//
//    private val bufferSize = AudioRecord.getMinBufferSize(
//        sampleRate,
//        AudioFormat.CHANNEL_IN_MONO,
//        AudioFormat.ENCODING_PCM_16BIT
//    )
//
//    private val okHttpClient = OkHttpClient.Builder()
//        .connectTimeout(5, TimeUnit.SECONDS)
//        .build()
//
//    fun start() {
//        connectWebSocket()
//
//        if (ActivityCompat.checkSelfPermission(
//                MyApplication.appContext,
//                Manifest.permission.RECORD_AUDIO
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return
//        }
//        audioRecord = AudioRecord(
//            MediaRecorder.AudioSource.MIC,
//            sampleRate,
//            AudioFormat.CHANNEL_IN_MONO,
//            AudioFormat.ENCODING_PCM_16BIT,
//            bufferSize
//        )
//
//        isRunning = true
//        audioRecord?.startRecording()
//
//        vadThread = Thread {
//            val buffer = ByteArray(bufferSize)
//
//            while (isRunning) {
//                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
//                if (read > 0) {
//                    val rms = calculateRMS(buffer, read)
//                    val isCurrentlySpeaking = rms > rmsThreshold
//
//                    if (isCurrentlySpeaking != isSpeaking) {
//                        isSpeaking = isCurrentlySpeaking
//                        Log.d("MicVAD", if (isSpeaking) "🎙️ Speaking..." else "🤫 Silence...")
//
//                        if (!isSpeaking) {
//                            webSocket?.send("")
//                        }
//                    }
//
//                    if (isSpeaking) {
//                        val byteBuffer = ByteArray(read * 2)
//                        for (i in 0 until read) {
//                            byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
//                            byteBuffer[i * 2 + 1] = (buffer[i].toInt() shr 8).toByte()
//                        }
//                        webSocket?.send(ByteString.of(*byteBuffer))
//
//
//
//                                val read = audioRecord!!.read(buffer, 0, buffer.size)
//                                if (read > 0) {
//                                    webSocket?.send(buffer.toByteString(0, read))
//                                }
//
//                    }
//                }
//            }
//        }
//        vadThread?.start()
//    }
//
//    fun stop() {
//        isRunning = false
//        try {
//            audioRecord?.stop()
//            audioRecord?.release()
//            vadThread?.interrupt()
//            webSocket?.close(1000, "Client closed")
//        } catch (e: Exception) {
//            Log.e("MicVADSocketManager", "Stop error: ${e.message}")
//        }
//        audioRecord = null
//        vadThread = null
//        isSpeaking = false
//    }
//
//    private fun connectWebSocket() {
////        val request = Request.Builder().url(wsUrl).build()
////        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
////
////            override fun onOpen(webSocket: WebSocket, response: Response) {
////                Log.d("WebSocket", "✅ Connected to $wsUrl")
////            }
////
////            override fun onMessage(webSocket: WebSocket, text: String) {
////                Log.d("WebSocket", "📩 Server Response: $text")
////                onServerResponse(text)
////            }
////
////            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
////                Log.e("WebSocket", "❌ Error: ${t.message}")
////            }
////
////            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
////                Log.d("WebSocket", "🔌 Closing: $reason")
////            }
////        })
//
//
//        val request = Request.Builder().url(RetrofitInstance.holdSpeakWsUrl+ PrefsManager().getPatient()?.pid.toString()).build()
//        val client = OkHttpClient()
//
//        webSocket = client.newWebSocket(request, object : WebSocketListener() {
//            override fun onOpen(webSocket: WebSocket, response: Response) {
//                Log.d("WebSocket", "✅ Connected to $wsUrl")
//            }
//
//            override fun onMessage(webSocket: WebSocket, text: String) {
//                Log.d("WebSocket", "📩 Server Response: $text")
//                onServerResponse(text)
//            }
//
//            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
//                Log.d("WebSocket", "🔌 Closing: $reason")
//            }
//
//            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
//                Log.e("WebSocket", "❌ Error: ${t.message}")
//            }
//        })
//
//
//    }
//
//    private fun calculateRMS(buffer: ShortArray, readSize: Int): Double {
//        var sum = 0.0
//        for (i in 0 until readSize) {
//            sum += buffer[i] * buffer[i]
//        }
//        return Math.sqrt(sum / readSize)
//    }
//}
