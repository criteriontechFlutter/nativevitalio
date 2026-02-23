package com.critetiontech.ctvitalio.viewmodel

import PrefsManager
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import com.critetiontech.ctvitalio.utils.ApiEndPoint
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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

        val url = RetrofitInstance.DEFAULT_BASE_URL+ ApiEndPoint().insertPatientMediaData +
                "?uhId=${PrefsManager().getPatient()?.uhID.toString()}&subCategory=${Uri.encode(testName)}&remark=${Uri.encode(category)}" +
                "&category=${Uri.encode(category)}&dateTime=${Uri.encode(dateTime)}&userId=0"
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



    fun Map<*, *>.getIfExists(key: String): String {
        return if (this.containsKey(key)) this[key]?.toString() ?: "" else ""
    }

    suspend fun insertInvestigation(
        context: Context,
        dateTime:String,
        reportData: List<Map<String, Any>>,
        patientDetails:  Map<*, *>
    ): Boolean = withContext(Dispatchers.IO) {
        val tempPatientData = mutableListOf<Map<String, String>>()
        val tempReportData = mutableListOf<Map<String, String>>()

        // ✅ Extension for safe value extraction
        fun Map<*, *>.getIfExists(key: String): String {
            return if (this.containsKey(key)) this[key]?.toString() ?: "" else ""
        }

        try {
            val collectionDate = patientDetails.getIfExists("collection_date")

            tempPatientData.add(
                mapOf(
                    "itemName" to patientDetails.getIfExists("itemName"),
                    "itemId" to patientDetails.getIfExists("itemId"),
                    "labName" to patientDetails.getIfExists("lab_name"),
                    "receiptNo" to patientDetails.getIfExists("receiptNo"),
                    "resultDateTime" to (dateTime)
                )
            )

            reportData.forEach { report ->
                val reportList = report["report"] as? List<Map<String, Any>> ?: emptyList()

                reportList.forEach { item ->
                    tempReportData.add(
                        mapOf(
                            "subTestId" to (item["id"]?.toString() ?: ""),
                            "subTestName" to (item["test_name"]?.toString() ?: ""),
                            "range" to (item["normal_values"]?.toString() ?: ""),
                            "resultDateTime" to collectionDate,
                            "result" to (item["result"]?.toString() ?: ""),
                            "unit" to (item["unit"]?.toString() ?: ""),
                            "isNormal" to "1"
                        )
                    )
                }
            }


            val body = mapOf(
                "uhid" to PrefsManager().getPatient()?.empId.toString(),
                "investigationDetailsJson" to Gson().toJson(tempPatientData),
                "clientId" to PrefsManager().getPatient()?.clientId.toString(),
                "investigationResultJson" to Gson().toJson(tempReportData),
                "userId" to PrefsManager().getPatient()?.userId.toString(),
            )

            val url = RetrofitInstance.DEFAULT_BASE_URL_5090 +  ApiEndPoint().insertResult

            val client = OkHttpClient()
            val mediaType = "application/json".toMediaTypeOrNull()
            val requestBody = Gson().toJson(body).toRequestBody(mediaType)
            Log.e("InsertInvestigation", "HTTP ${url}")
            Log.e("InsertInvestigation", "HTTP ${body}")
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful) {
                val json = JSONObject(responseBody.orEmpty())
                return@withContext json.optInt("status") == 1
            } else {
                Log.e("InsertInvestigation", "HTTP ${response.code}: ${response.message}")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e("InsertInvestigation", e.localizedMessage ?: "Unknown error", e)
            return@withContext false
        }
    }

}