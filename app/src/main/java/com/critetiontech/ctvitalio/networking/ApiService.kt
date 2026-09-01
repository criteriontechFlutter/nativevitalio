package com.critetiontech.ctvitalio.networking

import com.critetiontech.ctvitalio.model.InsertMindfulnessRequest
import com.critetiontech.ctvitalio.model.InsertMindfulnessResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * ApiService.kt
 *
 * Retrofit interface for all HTTP endpoints.
 *
 * Strongly-typed endpoints are preferred. Dynamic URL methods (dynamicGet,
 * dynamicRawPost, etc.) are kept for flexibility but should be gradually
 * replaced with typed endpoints as the API stabilises.
 *
 * NOTE: The `generateAuthHeaderMap` function that was previously defined here
 * has been removed — it was a duplicate of the one in RetrofitInstance and
 * contained hardcoded empty-string credentials that made auth headers non-functional.
 * Auth headers should be injected via AuthInterceptor or passed via [additionalHeaders]
 * in RetrofitInstance.createApiService().
 */
interface ApiService {

    // ─── Strongly-typed endpoints ─────────────────────────────────────────────

    @POST("api/CorporateMindfulness/GetCorporateMindfulness")
    suspend fun insertMindfulness(
        @Body request: InsertMindfulnessRequest
    ): Response<InsertMindfulnessResponse>

    // ─── Dynamic URL endpoints (flexible, use sparingly) ─────────────────────

    @GET
    suspend fun dynamicGet(
        @Url url: String,
        @HeaderMap headers: Map<String, String> = emptyMap(),
        @QueryMap(encoded = true) params: Map<String, @JvmSuppressWildcards Any> = emptyMap()
    ): Response<ResponseBody>

    @POST
    suspend fun dynamicRawPost(
        @Url url: String,
        @HeaderMap headers: Map<String, String> = emptyMap(),
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<ResponseBody>

    @POST
    suspend fun queryDynamicRawPost(
        @Url url: String,
        @HeaderMap headers: Map<String, String> = emptyMap(),
        @QueryMap(encoded = false) params: Map<String, String> = emptyMap()
    ): Response<ResponseBody>

    @Multipart
    @POST
    suspend fun dynamicMultipartPost(
        @Url url: String,
        @HeaderMap headers: Map<String, String> = emptyMap(),
        @Part parts: List<MultipartBody.Part>
    ): Response<ResponseBody>

    @Multipart
    @PUT
    suspend fun dynamicMultipartPut(
        @Url url: String,
        @HeaderMap headers: Map<String, String> = emptyMap(),
        @Part parts: List<MultipartBody.Part>
    ): Response<ResponseBody>

    @PUT
    suspend fun queryDynamicPut(
        @Url url: String,
        @HeaderMap headers: Map<String, String> = emptyMap(),
        @QueryMap params: Map<String, String> = emptyMap()
    ): Response<ResponseBody>

    @DELETE
    suspend fun dynamicDelete(
        @Url url: String,
        @HeaderMap headers: Map<String, String> = emptyMap(),
        @QueryMap(encoded = true) params: Map<String, @JvmSuppressWildcards Any> = emptyMap()
    ): Response<ResponseBody>
}