package com.critetiontech.ctvitalio.networking

import PrefsManager
import okhttp3.Interceptor

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        PrefsManager().getPatient()

        val request = chain.request().newBuilder().apply {
            addHeader("Content-Type", "application/json")
        }.build()

        return chain.proceed(request)
    }
}
