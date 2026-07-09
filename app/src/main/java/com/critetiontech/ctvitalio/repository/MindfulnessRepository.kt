package com.critetiontech.ctvitalio.repository

import com.critetiontech.ctvitalio.model.InsertMindfulnessRequest
import com.critetiontech.ctvitalio.model.InsertMindfulnessResponse
import com.critetiontech.ctvitalio.networking.RetrofitInstance
import retrofit2.Response

class MindfulnessRepository {
    suspend fun insertMindfulness(request: InsertMindfulnessRequest): Response<InsertMindfulnessResponse> {
        return RetrofitInstance.createApiService().insertMindfulness(request)
    }
}
