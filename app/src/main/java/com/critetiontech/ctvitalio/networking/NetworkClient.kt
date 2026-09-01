package com.critetiontech.ctvitalio.networking

// BuildConfig is generated at compile time
import com.critetiontech.ctvitalio.utils.MyApplication
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * NetworkClient.kt
 *
 * Provides a shared, pre-configured OkHttpClient used across the app.
 * Includes the AuthInterceptor and NetworkConnectionInterceptor by default.
 * Logging is attached only in debug builds using the app's own BuildConfig.
 */
object NetworkClient {

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(AuthInterceptor())
            .addInterceptor(NetworkConnectionInterceptor(MyApplication.appContext))
            .apply {
                // Attach logging only in debug — never in release
                if (com.critetiontech.ctvitalio.BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BASIC
                    })
                }
            }
            .build()
    }
}
