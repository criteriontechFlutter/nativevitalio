package com.critetiontech.ctvitalio.networking

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFactory {

    private val retrofitMap = mutableMapOf<String, Retrofit>()

    fun getRetrofit(baseUrl: String): Retrofit {
        return retrofitMap.getOrPut(baseUrl) {
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(NetworkClient.okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }
}
