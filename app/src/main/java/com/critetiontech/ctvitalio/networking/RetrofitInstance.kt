package com.critetiontech.ctvitalio.networking

// BuildConfig is generated at compile time – suppress any lint warning
import com.critetiontech.ctvitalio.utils.MyApplication
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitInstance.kt
 *
 * Centralized Retrofit factory.
 *
 * Previously had 6 near-identical createApiService*() functions — consolidated
 * into a single [createApiService] that accepts the base URL as a parameter.
 *
 * Usage examples:
 *   RetrofitInstance.createApiService()                              // default :5082
 *   RetrofitInstance.createApiService(NetworkConfig.BASE_URL_5096)   // :5096
 *   RetrofitInstance.createApiService(NetworkConfig.BASE_URL_5119)   // :5119
 */
object RetrofitInstance {

    // ─── Shared OkHttpClient (no auth headers) ────────────────────────────────
    private val baseOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .apply {
                // Only attach full-body logging in debug builds
                if (com.critetiontech.ctvitalio.BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .build()
    }

    // ─── Single factory method (replaces 6 duplicates) ────────────────────────
    /**
     * @param baseUrl        Override the base URL; defaults to [NetworkConfig.BASE_URL].
     * @param includeAuth    When true, adds `Content-Type: application/json` header.
     * @param additionalHeaders Any extra headers to inject (e.g. custom tokens).
     * @param skipNetworkCheck Pass true to skip the offline guard (e.g. log upload worker).
     */
    fun createApiService(
        baseUrl: String = NetworkConfig.BASE_URL,
        includeAuth: Boolean = false,
        additionalHeaders: Map<String, String> = emptyMap(),
        skipNetworkCheck: Boolean = false
    ): ApiService {
        val headers = buildHeaderMap(includeAuth, additionalHeaders)

        val client = baseOkHttpClient.newBuilder()
            .addInterceptor(createHeaderInterceptor(headers))
            .apply {
                if (!skipNetworkCheck) {
                    addInterceptor(NetworkConnectionInterceptor(MyApplication.appContext))
                }
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private fun createHeaderInterceptor(headers: Map<String, String>): Interceptor =
        Interceptor { chain ->
            val request = chain.request().newBuilder().apply {
                headers.forEach { (key, value) -> addHeader(key, value) }
            }.build()
            chain.proceed(request)
        }

    private fun buildHeaderMap(
        includeAuth: Boolean,
        additionalHeaders: Map<String, String>
    ): Map<String, String> = buildMap {
        if (includeAuth) put("Content-Type", "application/json")
        putAll(additionalHeaders)
    }

    // ─── Convenience shortcuts (backward-compat aliases) ─────────────────────

    /** Staging base host URL — used for building image/asset URLs across the app. */
    val StaggingbaseUrl: String get() = "https://demo.vitalio.care"

    /** WebSocket base URL for the voice/hold-speak feature. */
    val holdSpeakWsUrl: String get() = NetworkConfig.VOICE_WS_URL

    /** Shortcut: port 5082 (default) */
    fun createApiService5082(includeAuth: Boolean = false) =
        createApiService(NetworkConfig.BASE_URL_5082, includeAuth)

    /** Shortcut: port 5083 */
    fun createApiService5083(includeAuth: Boolean = false) =
        createApiService(NetworkConfig.BASE_URL_5083, includeAuth)

    /** Shortcut: port 5090 */
    fun createApiService5090(includeAuth: Boolean = false) =
        createApiService(NetworkConfig.BASE_URL_5090, includeAuth)

    /** Shortcut: port 5096 */
    fun createApiService5096(includeAuth: Boolean = false) =
        createApiService(NetworkConfig.BASE_URL_5096, includeAuth)

    /** Shortcut: port 5100 */
    fun createApiService5100(includeAuth: Boolean = false) =
        createApiService(NetworkConfig.BASE_URL_5100, includeAuth)

    /** Shortcut: port 5119 */
    fun createApiService5119(includeAuth: Boolean = false) =
        createApiService(NetworkConfig.BASE_URL_5119, includeAuth)

    /** Shortcut: port 5082 (legacy name used in several ViewModels) */
    fun createApiService7082(includeAuth: Boolean = false) =
        createApiService(NetworkConfig.BASE_URL_5082, includeAuth)

    /** Shortcut: port 5096 (legacy name used in several ViewModels) */
    fun createApiService7096(includeAuth: Boolean = false) =
        createApiService(NetworkConfig.BASE_URL_5096, includeAuth)
}
