package com.critetiontech.ctvitalio.networking

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitInstance {

//    private const val DEFAULT_BASE_URL = "https://api.medvantage.tech:7082/"
//    private const val DEFAULT_BASE_URL_7096 = "https://api.medvantage.tech:7096/"
//    private const val DEFAULT_BASE_URL_7082 = "https://api.medvantage.tech:7082/"
//    private const val DEFAULT_BASE_URL = "http://52.172.134.222:205/api/v1.0/"


   //Vitalio Development Server
    private const val DEFAULT_BASE_URL = "http://172.16.61.31:5082/"
    private const val DEFAULT_BASE_URL_7096 = "http://172.16.61.31:5096/"
    private const val DEFAULT_BASE_URL_7082 = "http://172.16.61.31:5082/"
//    private const val DEFAULT_BASE_URL = "http://52.172.134.222:205/api/v1.0/"

    private val baseOkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(createLoggingInterceptor())
            .connectTimeout(60, TimeUnit.SECONDS) // default is 10 seconds
            .readTimeout(60, TimeUnit.SECONDS)    // default is 10 seconds
            .writeTimeout(60, TimeUnit.SECONDS)   // default is 10 seconds
            .retryOnConnectionFailure(true)
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


    fun createApiService7096(
        overrideBaseUrl: String? = null, // Optional parameter
        includeAuthHeader: Boolean = false,
        additionalHeaders: Map<String, String> = emptyMap()
    ): ApiService {
        val baseUrlToUse = overrideBaseUrl ?: DEFAULT_BASE_URL_7096
        val headers = generateAuthHeaderMap(includeAuthHeader,additionalHeaders)

        val clientWithHeaders = baseOkHttpClient.newBuilder()
            .addInterceptor(createAuthInterceptor(headers))
            .build()

        return getRetrofitInstance(baseUrlToUse).newBuilder()
            .client(clientWithHeaders)
            .build()
            .create(ApiService::class.java)
    }


    fun createApiService7082(
        overrideBaseUrl: String? = null, // Optional parameter
        includeAuthHeader: Boolean = false,
        additionalHeaders: Map<String, String> = emptyMap()
    ): ApiService {
        val baseUrlToUse = overrideBaseUrl ?: DEFAULT_BASE_URL_7082
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
