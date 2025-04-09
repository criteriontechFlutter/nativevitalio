package com.criterion.nativevitalio.networking

import com.criterion.nativevitalio.Utils.MyApplication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
//object RetrofitInstance {
//
//    private const val BASE_URL = "https://api.medvantage.tech:7082/"
//
//    // Create OkHttpClient once and reuse it
//    private val okHttpClient by lazy {
//        OkHttpClient.Builder()
//            .addInterceptor(createLoggingInterceptor())
//            .build()
//    }
//
//    // Create Retrofit instance once and reuse it
//    private val retrofit by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//    }
//
//    fun createApiService( ): ApiService {
//        val headers = generateAuthHeaderMap(false);
//        val clientWithHeaders = okHttpClient.newBuilder()
//            .addInterceptor(createAuthInterceptor(headers))
//            .build()
//
//        return retrofit.newBuilder()
//            .client(clientWithHeaders)
//            .build()
//            .create(ApiService::class.java)
//    }
//
//    private fun createAuthInterceptor(headers: Map<String, String> = emptyMap()) = Interceptor { chain ->
//        val request = chain.request().newBuilder().apply {
//            headers.forEach { (key, value) ->
//                addHeader(key, value)
//            }
//            // Add default headers if needed
//            if (!headers.containsKey("Content-Type")) {
//                addHeader("Content-Type", "application/json")
//            }
//        }.build()
//        chain.proceed(request)
//    }
//
//    private fun createLoggingInterceptor() = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }
//}

object RetrofitInstance {

    private const val DEFAULT_BASE_URL = "https://api.medvantage.tech:7082/"
//    private const val DEFAULT_BASE_URL = "http://52.172.134.222:205/api/v1.0/"

    private val baseOkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(createLoggingInterceptor())
            .build()
    }

    private fun getRetrofitInstance(baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(baseOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun createApiService(
        overrideBaseUrl: String? = null, // Optional parameter
        includeAuthHeader: Boolean = false,
        additionalHeaders: Map<String, String> = emptyMap()
    ): ApiService {
        val baseUrlToUse = overrideBaseUrl ?: DEFAULT_BASE_URL
        val headers = generateAuthHeaderMap(includeAuthHeader,additionalHeaders)

        val clientWithHeaders = baseOkHttpClient.newBuilder()
            .addInterceptor(createAuthInterceptor(headers))
            .build()

        return getRetrofitInstance(baseUrlToUse).newBuilder()
            .client(clientWithHeaders)
            .build()
            .create(ApiService::class.java)
    }

    private fun createAuthInterceptor(headers: Map<String, String> = emptyMap()) = Interceptor { chain ->
        val request = chain.request().newBuilder().apply {
            headers.forEach { (key, value) ->
                addHeader(key, value)
            }
//            if (!headers.containsKey("Content-Type")) {
//                addHeader("Content-Type", "application/json")
//            }
        }.build()
        chain.proceed(request)
    }

    private fun createLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

//    private fun generateAuthHeaderMap(includeAuth: Boolean ): Map<String, String> {
//        return if (includeAuth) {
//            mapOf(
//                "Authorization" to "Bearer  ",
//                "Content-Type" to "application/json"
//                // Add more headers if needed
//            )
//        } else {
//            emptyMap()
//        }
//    }

    private fun generateAuthHeaderMap(
        includeAuth: Boolean,
//        token: String? = null,
        additionalHeaders: Map<String, String> = emptyMap()
    ): Map<String, String> {
        val headers = mutableMapOf<String, String>()

        if (includeAuth  ) {
            headers["Content-Type"] = "application/json"
        }

        headers.putAll(additionalHeaders)

        return headers
    }
}
