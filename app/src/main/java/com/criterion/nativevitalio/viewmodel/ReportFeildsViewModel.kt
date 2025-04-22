package com.criterion.nativevitalio.viewmodel

import PrefsManager
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

class ReportFeildsViewModel  : ViewModel(){
    suspend fun insertPatientMediaData(
        context: Context,

        testName: String,
        category: String,
        remark: String,
        dateTime: String,
        imagePath: String
    ): String? = withContext(Dispatchers.IO) {

        val url = "https://api.medvantage.tech:7082/api/PatientMediaData/InsertPatientMediaData" +
                "?uhId=${PrefsManager().getPatient()?.uhID.toString()}&subCategory=${Uri.encode(testName)}&remark=${Uri.encode(remark)}" +
                "&category=${Uri.encode(category)}&dateTime=${Uri.encode(dateTime)}&userId=${PrefsManager().getPatient()?.userId.toString()}"
        Log.e("UploadError", "url: ${url}")
        val client = OkHttpClient()

        try {
            val file = File(imagePath)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("formFile", file.name, requestFile)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(multipartBody)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                Log.d("UploadSuccess", responseBody.orEmpty())

                val json = JSONObject(responseBody.orEmpty())
                val responseValue = json.optJSONArray("responseValue")
                return@withContext if (responseValue != null && responseValue.length() > 0) {
                    responseValue.getJSONObject(0).optString("url")
                } else {
                    null
                }
            } else {
                Log.e("UploadError", "Failed: ${response.code} - ${response.message}")
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("UploadException", e.localizedMessage ?: "Unknown error", e)
            return@withContext null
        }
    }
}