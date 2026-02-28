package com.azzahid.hof.domain.state

import io.ktor.http.HttpMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiRouteUiStateTest {

    @Test
    fun `default values are correct`() {
        val state = ApiRouteUiState()
        assertEquals("", state.path)
        assertEquals(HttpMethod.Get, state.method)
        assertEquals("", state.description)
        assertEquals("{}", state.responseBody)
        assertEquals("200", state.statusCode)
        assertEquals(1, state.headers.size)
        assertFalse(state.isValid)
    }

    @Test
    fun `headersMap filters blank keys`() {
        val state = ApiRouteUiState(
            headers = listOf(
                HeaderEntry(key = "Content-Type", value = "application/json"),
                HeaderEntry(key = "", value = "ignored"),
                HeaderEntry(key = "X-Custom", value = "value")
            )
        )
        val map = state.headersMap
        assertEquals(2, map.size)
        assertEquals("application/json", map["Content-Type"])
        assertEquals("value", map["X-Custom"])
    }

    @Test
    fun `headersMap returns empty map for blank keys only`() {
        val state = ApiRouteUiState(
            headers = listOf(
                HeaderEntry(key = "", value = "value1"),
                HeaderEntry(key = "  ", value = "value2")
            )
        )
        assertTrue(state.headersMap.isEmpty())
    }

    @Test
    fun `headersMap with default header entry`() {
        val state = ApiRouteUiState()
        assertTrue(state.headersMap.isEmpty())
    }

    @Test
    fun `HeaderEntry default values`() {
        val entry = HeaderEntry()
        assertEquals("", entry.key)
        assertEquals("", entry.value)
    }
}
