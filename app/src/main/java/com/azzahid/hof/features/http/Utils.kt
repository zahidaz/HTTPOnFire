package com.azzahid.hof.features.http

import com.google.gson.Gson
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.net.Inet4Address
import java.net.NetworkInterface

fun getLocalIpAddress(): String = try {
    NetworkInterface.getNetworkInterfaces().toList()
        .flatMap { it.inetAddresses.toList() }
        .firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
        ?.hostAddress ?: "127.0.0.1"
} catch (_: Exception) {
    "127.0.0.1"
}

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

