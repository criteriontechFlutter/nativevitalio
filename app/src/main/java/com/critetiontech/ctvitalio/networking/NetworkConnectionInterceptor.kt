package com.critetiontech.ctvitalio.networking


import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException


class NetworkConnectionInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            if (!NetworkUtils.isInternetAvailable(context)) {
                Log.e("NetworkInterceptor", "No internet. Skipping request.")
                // Return a fake 503 (Service Unavailable) response
                return Response.Builder()
                    .code(503)
                    .message("No Internet Connection")
                    .protocol(okhttp3.Protocol.HTTP_1_1)
                    .request(chain.request())
                    .body("".toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            }
            chain.proceed(chain.request())
        } catch (e: IOException) {
            Log.e("NetworkInterceptor", "Request failed: ${e.message}")
            throw e
        }
    }
}
