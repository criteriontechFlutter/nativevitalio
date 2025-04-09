package com.criterion.nativevitalio.networking

import PrefsManager
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.QueryMap
import retrofit2.http.Url
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.http.Body

fun generateAuthHeaderMap(
    token: Boolean,

): Map<String, String>  {

    val prefsManager = PrefsManager( )
    val patient = prefsManager.getPatient()

//    val accessToken = patient?.token
//    val userId = patient?.id
    val accessToken = " "
    val userId = " "
    return if (token && !accessToken.isNullOrEmpty() && !userId.isNullOrEmpty()) {
        mapOf(
            "x-access-token" to accessToken,
            "userID" to userId,
            "Content-Type" to "application/json"
        )
    } else {
        emptyMap()
    }
}

//interface ApiService {
//    @GET("")
//    fun loginApi( )  : Call<ResponseBody>
//
//
//
//
//
//}
interface ApiService {
    @GET
    suspend fun dynamicGet(
        @Url url: String,
        @HeaderMap headers: Map<String, String> = emptyMap(),
        @QueryMap(encoded = true) params: Map<String, @JvmSuppressWildcards Any>
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
        @QueryMap(encoded = true) params: Map<String, @JvmSuppressWildcards Any>
    ): Response<ResponseBody>
    // Add similar annotations for PUT, DELETE, etc.
}