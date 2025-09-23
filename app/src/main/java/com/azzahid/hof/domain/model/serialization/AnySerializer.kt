package com.azzahid.hof.domain.model.serialization

import com.google.gson.Gson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

object AnySerializer : KSerializer<Any> {
    private val gson = Gson()
    private val jsonElementSerializer = JsonElement.serializer()
    override val descriptor = jsonElementSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Any) {
        val jsonString = gson.toJson(value)
        val jsonElement = Json.parseToJsonElement(jsonString)
        jsonElementSerializer.serialize(encoder, jsonElement)
    }

    override fun deserialize(decoder: Decoder): Map<String, Any> {
        throw NotImplementedError("Deserialization is not implemented for MapStringAny")
    }
}