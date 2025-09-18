package com.critetiontech.ctvitalio.logging

import PrefsManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.critetiontech.ctvitalio.utils.MyApplication
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID


object LoggingManager {

    private val logQueue: MutableList<JSONObject> = mutableListOf()
    private var sessionId: String = UUID.randomUUID().toString()

    private  val APP_VERSION = getAppVersion(MyApplication.appContext)

    fun logEvent(event: String, screen: String, extras: Map<String, Any?> = emptyMap()) {
        Log.e("LogUploadWorkerFlush", "logevent:")
        val json = JSONObject()
        json.put("event", event)
        json.put("screen", screen)
        json.put("timestamp", System.currentTimeMillis())
        json.put("sessionId", sessionId)
        json.put("extra", JSONObject(extras))

        logQueue.add(json)

//        if (logQueue.size > 50) {
            flushLogs(MyApplication.appContext)
//        }
    }

    fun getAppVersion(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionName = pInfo.versionName
            val versionCode = pInfo.versionCode // use pInfo.versionCode for older SDKs
            "Version: $versionName, Code: $versionCode"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            "Unknown"
        }
    }

    fun flushLogs(context: Context) {
        if (logQueue.isEmpty()) return

        val logsToSend = ArrayList(logQueue)
        logQueue.clear()

        val root = JSONObject().apply {
            put("deviceToken", PrefsManager().getDeviceToken().toString())
            put("appVersion", APP_VERSION)
            put("logs", JSONArray(logsToSend))
        }

        val data = workDataOf("logs" to root.toString())

        val oneTime = OneTimeWorkRequestBuilder<LogUploadWorker>()
            .setInputData(data)
            .build()
        WorkManager.getInstance(context).enqueue(oneTime)
    }

}
