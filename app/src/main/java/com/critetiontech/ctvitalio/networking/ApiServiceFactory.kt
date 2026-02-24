package com.critetiontech.ctvitalio.networking

object ApiServiceFactory {

    fun getApi(baseUrl: String): ApiService {
        return RetrofitFactory
            .getRetrofit(baseUrl)
            .create(ApiService::class.java)
    }
}
