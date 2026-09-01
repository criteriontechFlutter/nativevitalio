package com.critetiontech.ctvitalio.networking

/**
 * NetworkConfig.kt
 *
 * Single source of truth for all base URLs used by the app's Retrofit clients.
 * Consolidates the fragmented URL constants previously scattered in RetrofitInstance.kt.
 *
 * NOTE: In production, move sensitive values to local.properties / BuildConfig.
 */
object NetworkConfig {

    // ─── Base server ──────────────────────────────────────────────────────────
    private const val STAGING_HOST = "https://demo.vitalio.care"
    // private const val DEV_HOST   = "http://182.156.200.177"   // development only

    // ─── Service URLs ─────────────────────────────────────────────────────────
    const val BASE_URL        = "$STAGING_HOST:5082/"
    const val BASE_URL_5082   = "$STAGING_HOST:5082/"
    const val BASE_URL_5083   = "$STAGING_HOST:5083/"
    const val BASE_URL_5090   = "$STAGING_HOST:5090/"
    const val BASE_URL_5096   = "$STAGING_HOST:5096/"
    const val BASE_URL_5100   = "$STAGING_HOST:5100/"
    const val BASE_URL_5119   = "$STAGING_HOST:5119/"

    // ─── External services ────────────────────────────────────────────────────
    const val SHOPRIGHT_URL        = "http://food.shopright.ai:3478/api/"
    const val UPLOAD_LAB_REPORT_URL= "http://182.156.200.178:8016/uploadLabreport/"
    const val VOICE_WS_URL         = "ws://182.156.200.177:8002/listen?token="

    // ─── Timeout settings (seconds) ───────────────────────────────────────────
    const val CONNECT_TIMEOUT  = 60L
    const val READ_TIMEOUT     = 60L
    const val WRITE_TIMEOUT    = 60L
}
