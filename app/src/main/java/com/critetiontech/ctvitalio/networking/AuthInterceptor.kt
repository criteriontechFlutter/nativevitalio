package com.critetiontech.ctvitalio.networking

import okhttp3.Interceptor
import okhttp3.Response

/**
 * AuthInterceptor.kt
 *
 * OkHttp interceptor that attaches request headers to every call.
 *
 * Currently adds Content-Type only. To attach a bearer token, retrieve
 * it from PrefsManager and add it here:
 *
 *   val token = PrefsManager().getPatient()?.token
 *   if (!token.isNullOrBlank()) addHeader("Authorization", "Bearer $token")
 *
 * Note: Previously called PrefsManager().getPatient() but discarded the
 * result — that useless call has been removed.
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Content-Type", "application/json")
            // TODO: Add bearer token here once auth flow is implemented
            .build()
        return chain.proceed(request)
    }
}
