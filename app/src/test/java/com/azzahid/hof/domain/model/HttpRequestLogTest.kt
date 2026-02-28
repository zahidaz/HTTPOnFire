package com.azzahid.hof.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpRequestLogTest {

    private fun createLog(
        id: String = "test-id",
        timestamp: Long = 1700000000000L,
        method: String = "GET",
        path: String = "/api/test",
        queryParameters: String? = null,
        clientIp: String = "192.168.1.1",
        userAgent: String? = "TestAgent/1.0",
        headers: String = "Content-Type: application/json",
        statusCode: Int? = 200,
        responseTimeMs: Long? = 50,
        contentType: String? = "application/json",
        contentLength: Long? = 1024,
        referer: String? = null
    ) = HttpRequestLog(
        id = id,
        timestamp = timestamp,
        method = method,
        path = path,
        queryParameters = queryParameters,
        clientIp = clientIp,
        userAgent = userAgent,
        headers = headers,
        statusCode = statusCode,
        responseTimeMs = responseTimeMs,
        contentType = contentType,
        contentLength = contentLength,
        referer = referer
    )

    @Test
    fun `getFormattedTimestamp returns formatted date`() {
        val log = createLog(timestamp = 1700000000000L)
        val formatted = log.getFormattedTimestamp()
        assertTrue(formatted.isNotBlank())
        assertTrue(formatted != "Invalid time")
    }

    @Test
    fun `getFormattedDetails includes all non-null fields`() {
        val log = createLog(
            queryParameters = "key=value",
            referer = "http://example.com"
        )
        val details = log.getFormattedDetails()

        assertTrue(details.contains("Method: GET"))
        assertTrue(details.contains("Path: /api/test"))
        assertTrue(details.contains("Query: key=value"))
        assertTrue(details.contains("Client IP: 192.168.1.1"))
        assertTrue(details.contains("Status: 200"))
        assertTrue(details.contains("Response Time: 50ms"))
        assertTrue(details.contains("User Agent: TestAgent/1.0"))
        assertTrue(details.contains("Content Type: application/json"))
        assertTrue(details.contains("Content Length: 1024 bytes"))
        assertTrue(details.contains("Referer: http://example.com"))
        assertTrue(details.contains("Headers:"))
    }

    @Test
    fun `getFormattedDetails excludes null fields`() {
        val log = createLog(
            queryParameters = null,
            userAgent = null,
            statusCode = null,
            responseTimeMs = null,
            contentType = null,
            contentLength = null,
            referer = null
        )
        val details = log.getFormattedDetails()

        assertTrue(details.contains("Method: GET"))
        assertTrue(details.contains("Path: /api/test"))
        assertTrue(!details.contains("Query:"))
        assertTrue(!details.contains("Status:"))
        assertTrue(!details.contains("Response Time:"))
        assertTrue(!details.contains("User Agent:"))
        assertTrue(!details.contains("Content Type:"))
        assertTrue(!details.contains("Content Length:"))
        assertTrue(!details.contains("Referer:"))
    }

    @Test
    fun `getFormattedDetails excludes blank queryParameters`() {
        val log = createLog(queryParameters = "  ")
        val details = log.getFormattedDetails()
        assertTrue(!details.contains("Query:"))
    }

    @Test
    fun `data class equality works correctly`() {
        val log1 = createLog()
        val log2 = createLog()
        assertEquals(log1, log2)
    }

    @Test
    fun `data class copy works correctly`() {
        val log = createLog()
        val updated = log.copy(statusCode = 404)
        assertEquals(404, updated.statusCode)
        assertEquals(log.method, updated.method)
    }
}
