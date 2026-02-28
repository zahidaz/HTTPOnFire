package com.azzahid.hof.domain.model.serialization

import io.ktor.http.HttpMethod
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Test

class HttpMethodSerializerTest {

    @Test
    fun `serialize GET method`() {
        val element = Json.encodeToJsonElement(HttpMethodSerializer, HttpMethod.Get)
        assertEquals("GET", element.jsonPrimitive.content)
    }

    @Test
    fun `serialize POST method`() {
        val element = Json.encodeToJsonElement(HttpMethodSerializer, HttpMethod.Post)
        assertEquals("POST", element.jsonPrimitive.content)
    }

    @Test
    fun `serialize PUT method`() {
        val element = Json.encodeToJsonElement(HttpMethodSerializer, HttpMethod.Put)
        assertEquals("PUT", element.jsonPrimitive.content)
    }

    @Test
    fun `serialize DELETE method`() {
        val element = Json.encodeToJsonElement(HttpMethodSerializer, HttpMethod.Delete)
        assertEquals("DELETE", element.jsonPrimitive.content)
    }

    @Test
    fun `serialize PATCH method`() {
        val element = Json.encodeToJsonElement(HttpMethodSerializer, HttpMethod.Patch)
        assertEquals("PATCH", element.jsonPrimitive.content)
    }

    @Test
    fun `deserialize GET method`() {
        val method = Json.decodeFromJsonElement(HttpMethodSerializer, JsonPrimitive("GET"))
        assertEquals(HttpMethod.Get, method)
    }

    @Test
    fun `deserialize POST method`() {
        val method = Json.decodeFromJsonElement(HttpMethodSerializer, JsonPrimitive("POST"))
        assertEquals(HttpMethod.Post, method)
    }

    @Test
    fun `deserialize custom method`() {
        val method = Json.decodeFromJsonElement(HttpMethodSerializer, JsonPrimitive("CUSTOM"))
        assertEquals(HttpMethod("CUSTOM"), method)
    }

    @Test
    fun `round-trip serialization preserves method`() {
        val methods = listOf(
            HttpMethod.Get, HttpMethod.Post, HttpMethod.Put,
            HttpMethod.Delete, HttpMethod.Patch, HttpMethod.Options, HttpMethod.Head
        )
        methods.forEach { original ->
            val encoded = Json.encodeToJsonElement(HttpMethodSerializer, original)
            val decoded = Json.decodeFromJsonElement(HttpMethodSerializer, encoded)
            assertEquals(original, decoded)
        }
    }
}
