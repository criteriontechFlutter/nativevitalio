package com.criterion.nativevitalio.networking

import PrefsManager
import android.content.Context
import com.criterion.nativevitalio.model.Movies
import retrofit2.Call
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
            "userID" to userId
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
    suspend fun dynamicPost(
        @Url url: String,
        @HeaderMap headers: Map<String, String> = emptyMap(),
        @Body body: Any? = null
    ): Call<ResponseBody>

    // Add similar annotations for PUT, DELETE, etc.
}