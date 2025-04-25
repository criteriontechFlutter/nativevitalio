package com.criterion.nativevitalio.utils

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat
import com.critetiontech.ctvitalio.utils.MyApplication

class MicVADManager(
    private val sampleRate: Int = 16000,
    private val rmsThreshold: Int = 2000,
    private val onVoiceDetected: (Boolean) -> Unit
) {
    private var audioRecord: AudioRecord? = null
    private var isRunning = false
    private var vadThread: Thread? = null
    private var isCurrentlySpeaking = false

    fun start() {
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

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

            while (isRunning && audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    val rms = calculateRMS(buffer, read)
                    val isSpeakingNow = rms > rmsThreshold

                    if (isSpeakingNow != isCurrentlySpeaking) {
                        isCurrentlySpeaking = isSpeakingNow
                        onVoiceDetected(isSpeakingNow)
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
        } catch (e: Exception) {
            Log.e("MicVADManager", "Error stopping VAD: ${e.message}")
        }
        audioRecord = null
        vadThread = null
        isCurrentlySpeaking = false
    }

    private fun calculateRMS(buffer: ShortArray, readSize: Int): Double {
        var sum = 0.0
        for (i in 0 until readSize) {
            sum += buffer[i] * buffer[i]
        }
        return Math.sqrt(sum / readSize)
    }
}
