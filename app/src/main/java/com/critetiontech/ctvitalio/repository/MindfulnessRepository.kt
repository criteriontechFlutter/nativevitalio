package com.critetiontech.vitalioCorporate.repository

import com.critetiontech.vitalioCorporate.model.InsertMindfulnessRequest
import com.critetiontech.vitalioCorporate.model.InsertMindfulnessResponse
import com.critetiontech.vitalioCorporate.networking.RetrofitInstance
import retrofit2.Response

class MindfulnessRepository {
    suspend fun insertMindfulness(request: InsertMindfulnessRequest): Response<InsertMindfulnessResponse> {
        return RetrofitInstance.createApiService().insertMindfulness(request)
    }
}
