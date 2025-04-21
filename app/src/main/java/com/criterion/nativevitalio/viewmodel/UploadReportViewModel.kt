package com.critetiontech.ctvitalio.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
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

class UploadReportViewModel : ViewModel() {




    suspend fun insertPatientMediaDataAndParseResponse(
        context: Context,
        imageFile: File
    ) = withContext(Dispatchers.IO) {

        val url = "http://182.156.200.178:8016/uploadLabreport/"
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                imageFile.name,
                imageFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        val client = OkHttpClient()

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Uploading...", Toast.LENGTH_SHORT).show()
        }

        val response = client.newCall(request).execute()

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Upload completed", Toast.LENGTH_SHORT).show()
        }

        if (response.isSuccessful) {
            val responseData = response.body?.string()
            val data = JSONObject(responseData.orEmpty())
            Log.e("UploadFailed", "HTTP ${data}")
//            if (data.has("response")) {
//                val responseList = data.getJSONArray("response")
//                val parsedReports = mutableListOf<Map<String, Any>>()
//
//                for (i in 0 until responseList.length()) {
//                    val item = responseList.getJSONObject(i)
//                    val patient = item.getJSONObject("patient_details")
//
//                    val mapped = mutableMapOf<String, Any>(
//                        "P_NameC" to EditText(context).apply {
//                            setText(patient.optString("patient_name"))
//                        },
//                        "genderC" to EditText(context).apply {
//                            setText(patient.optString("sex"))
//                        },
//                        "ageC" to EditText(context).apply {
//                            setText(patient.optString("age"))
//                        },
//                        "summaryC" to EditText(context).apply {
//                            setText(item.optString("summary"))
//                        },
//                        "statusC" to EditText(context).apply {
//                            setText(item.optString("status"))
//                        },
//                        "recommended_specialtyC" to EditText(context).apply {
//                            setText(item.optString("recommended_specialty"))
//                        },
//                    )
//
//                    val reportArray = item.getJSONArray("report")
//                    val reportList = mutableListOf<Map<String, EditText>>()
//
//                    for (j in 0 until reportArray.length()) {
//                        val reportItem = reportArray.getJSONObject(j)
//                        reportList.add(
//                            mapOf("val" to EditText(context).apply {
//                                setText(reportItem.optString("result"))
//                            })
//                        )
//                    }
//
//                    mapped["report"] = reportList
//                    parsedReports.add(mapped)
//                }
//
//                withContext(Dispatchers.Main) {
//                    Toast.makeText(context, "Reports parsed: ${parsedReports.size}", Toast.LENGTH_LONG).show()
//                    // Navigate or show LabFieldView screen
//                    // startActivity(Intent(context, LabFieldView::class.java))
//                }
//
//                return@withContext parsedReports
//            }
        } else {
            Log.e("UploadFailed", "HTTP ${response.code}: ${response.message}")
        }

        return@withContext emptyList<Map<String, Any>>()
    }
}