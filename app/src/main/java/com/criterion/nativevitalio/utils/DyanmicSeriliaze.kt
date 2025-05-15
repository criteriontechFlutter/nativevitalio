package com.critetiontech.ctvitalio.utils

import com.critetiontech.ctvitalio.model.ApiGenericResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class ApiDynamicDeserializer<T>(
    private val dataType: Class<T>
) : JsonDeserializer<Pair<ApiGenericResponse, T?>> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Pair<ApiGenericResponse, T?> {
        val jsonObject = json.asJsonObject

        val status = jsonObject["status"]?.asInt ?: 0
        val message = jsonObject["message"]?.asString ?: "No message"

        val fixedResponse = ApiGenericResponse(status, message)

        val dynamicKey = jsonObject.entrySet()
            .firstOrNull { it.key != "status" && it.key != "message" }
            ?.key

        val payload = dynamicKey?.let {
            context.deserialize<T>(jsonObject[it], dataType)
        }

        return Pair(fixedResponse, payload)
    }
}
