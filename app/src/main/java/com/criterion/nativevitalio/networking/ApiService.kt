package com.criterion.nativevitalio.networking

import com.criterion.nativevitalio.model.Movies
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {
    @GET("popular")
    fun getPopularMovies(
        @Header("Authorization") authHeader: String
    ): Call<Movies>


}