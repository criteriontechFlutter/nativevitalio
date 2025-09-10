package com.critetiontech.ctvitalio.logging

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class LogUploadWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val payload = inputData.getString("logs") ?: return Result.success()
        Log.d("workerMessage", "🚀 Payload: $payload")

        return try {
            val client = OkHttpClient()
            val mediaType = "application/json".toMediaType()
            val body = payload.toRequestBody(mediaType)

            val request = Request.Builder()
                .url("http://182.156.200.177:5082/api/ActivityLogs/InsertActivityLogs")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string()
                Log.d("workerMessage", "🔄 Response code: ${response.code}")
                Log.d("workerMessage", "📩 Response body: $respBody")

                return if (response.isSuccessful) {
                    Log.d("LogUploadWorker", "✅ Logs uploaded successfully")
                    Result.success()
                } else {
                    Log.e("LogUploadWorker", "❌ Upload failed: ${response.code}, body=$respBody")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e("LogUploadWorker", "⚠️ Exception: ${e.message}", e)
            return Result.retry()
        }
    }
}
