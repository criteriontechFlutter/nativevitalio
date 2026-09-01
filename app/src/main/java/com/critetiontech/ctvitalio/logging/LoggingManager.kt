package com.critetiontech.ctvitalio.logging

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.critetiontech.ctvitalio.utils.MyApplication
import com.critetiontech.ctvitalio.utils.PrefsManager
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * LoggingManager.kt
 *
 * Queues analytics/event logs in memory and flushes them via [LogUploadWorker]
 * using WorkManager.
 *
 * Performance fix: previously flushed on EVERY logEvent() call. Now batches
 * events in [logQueue] and only triggers upload on [flushLogs] — which is
 * called automatically when the app moves to background (MyApplication).
 *
 * Log level fix: was using Log.e (error) for normal events; now uses Log.d.
 */
object LoggingManager {

    private val logQueue: MutableList<JSONObject> = mutableListOf()
    private val sessionId: String = UUID.randomUUID().toString()
    private val appVersion: String by lazy { getAppVersion(MyApplication.appContext) }

    /**
     * Queues an event log entry. Does NOT flush immediately.
     * Call [flushLogs] explicitly to send queued events to the server.
     */
    fun logEvent(event: String, screen: String, extras: Map<String, Any?> = emptyMap()) {
        Log.d("LoggingManager", "Queuing event: $event on $screen")
        val json = JSONObject().apply {
            put("event", event)
            put("screen", screen)
            put("timestamp", System.currentTimeMillis())
            put("sessionId", sessionId)
            put("extra", JSONObject(extras.mapValues { it.value ?: "" }))
        }
        logQueue.add(json)
    }

    /**
     * Dispatches all queued logs to the server via WorkManager.
     * The queue is cleared before dispatching to prevent double-sending.
     */
    fun flushLogs(context: Context) {
        if (logQueue.isEmpty()) return

        val logsToSend = ArrayList(logQueue)
        logQueue.clear()

        val prefs = PrefsManager()
        val root = JSONObject().apply {
            put("deviceToken", prefs.getDeviceToken().orEmpty())
            put("clientId",   prefs.getPatient()?.clientId?.toString().orEmpty())
            put("pid",        prefs.getPatient()?.id?.toString().orEmpty())
            put("appVersion", appVersion)
            put("logs",       JSONArray(logsToSend))
        }

        val workRequest = OneTimeWorkRequestBuilder<LogUploadWorker>()
            .setInputData(workDataOf("logs" to root.toString()))
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private fun getAppVersion(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "Version: ${pInfo.versionName}, Code: ${pInfo.versionCode}"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }
}
