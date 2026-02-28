package com.azzahid.hof.data.repository

import com.azzahid.hof.data.database.HttpRequestLogDao
import com.azzahid.hof.domain.model.HttpRequestLog
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HttpRequestLogRepositoryTest {

    private lateinit var logDao: HttpRequestLogDao
    private lateinit var repository: HttpRequestLogRepository

    @Before
    fun setup() {
        logDao = mockk(relaxed = true)
        repository = HttpRequestLogRepository(logDao)
    }

    @Test
    fun `logRequest creates log with formatted headers`() = runTest {
        val logSlot = slot<HttpRequestLog>()
        coEvery { logDao.insertLog(capture(logSlot)) } returns Unit

        repository.logRequest(
            method = "GET",
            path = "/api/test",
            queryParameters = "key=value",
            clientIp = "192.168.1.1",
            userAgent = "TestAgent",
            headers = mapOf(
                "Content-Type" to listOf("application/json"),
                "Accept" to listOf("text/html", "application/json")
            ),
            statusCode = 200,
            responseTimeMs = 42,
            contentType = "application/json",
            contentLength = 256,
            referer = "http://example.com"
        )

        val captured = logSlot.captured
        assertEquals("GET", captured.method)
        assertEquals("/api/test", captured.path)
        assertEquals("key=value", captured.queryParameters)
        assertEquals("192.168.1.1", captured.clientIp)
        assertEquals("TestAgent", captured.userAgent)
        assertEquals(200, captured.statusCode)
        assertEquals(42L, captured.responseTimeMs)
        assertTrue(captured.headers.contains("Content-Type: application/json"))
        assertTrue(captured.headers.contains("Accept: text/html, application/json"))
        assertTrue(captured.id.isNotEmpty())
    }

    @Test
    fun `logRequest generates unique IDs`() = runTest {
        val logs = mutableListOf<HttpRequestLog>()
        coEvery { logDao.insertLog(capture(logs)) } returns Unit

        repeat(10) {
            repository.logRequest(
                method = "GET",
                path = "/test",
                queryParameters = null,
                clientIp = "127.0.0.1",
                userAgent = null,
                headers = emptyMap()
            )
        }

        val ids = logs.map { it.id }.toSet()
        assertEquals(10, ids.size)
    }

    @Test
    fun `cleanupOldLogs deletes excess logs`() = runTest {
        coEvery { logDao.getLogCount() } returns 1500

        repository.cleanupOldLogs(maxLogs = 1000)

        coVerify { logDao.deleteOldestLogs(500) }
    }

    @Test
    fun `cleanupOldLogs does nothing when under limit`() = runTest {
        coEvery { logDao.getLogCount() } returns 500

        repository.cleanupOldLogs(maxLogs = 1000)

        coVerify(exactly = 0) { logDao.deleteOldestLogs(any()) }
    }

    @Test
    fun `cleanupOldLogs does nothing when at limit`() = runTest {
        coEvery { logDao.getLogCount() } returns 1000

        repository.cleanupOldLogs(maxLogs = 1000)

        coVerify(exactly = 0) { logDao.deleteOldestLogs(any()) }
    }

    @Test
    fun `cleanupLogsByAge calculates cutoff correctly`() = runTest {
        val maxAgeMillis = 7 * 24 * 60 * 60 * 1000L
        val cutoffSlot = slot<Long>()
        coEvery { logDao.deleteLogsOlderThan(capture(cutoffSlot)) } returns Unit

        repository.cleanupLogsByAge(maxAgeMillis)

        val expectedCutoff = System.currentTimeMillis() - maxAgeMillis
        val actualCutoff = cutoffSlot.captured
        assertTrue(kotlin.math.abs(actualCutoff - expectedCutoff) < 1000)
    }

    @Test
    fun `cleanupOldLogsByDate delegates to dao`() = runTest {
        val cutoff = 1700000000000L
        repository.cleanupOldLogsByDate(cutoff)
        coVerify { logDao.deleteLogsOlderThan(cutoff) }
    }

    @Test
    fun `clearAllLogs delegates to dao`() = runTest {
        repository.clearAllLogs()
        coVerify { logDao.clearAllLogs() }
    }

    @Test
    fun `getLogCount delegates to dao`() = runTest {
        coEvery { logDao.getLogCount() } returns 42
        assertEquals(42, repository.getLogCount())
    }

    @Test
    fun `logRequest handles empty headers map`() = runTest {
        val logSlot = slot<HttpRequestLog>()
        coEvery { logDao.insertLog(capture(logSlot)) } returns Unit

        repository.logRequest(
            method = "POST",
            path = "/submit",
            queryParameters = null,
            clientIp = "10.0.0.1",
            userAgent = null,
            headers = emptyMap()
        )

        assertEquals("", logSlot.captured.headers)
    }

    @Test
    fun `logRequest handles optional null parameters`() = runTest {
        val logSlot = slot<HttpRequestLog>()
        coEvery { logDao.insertLog(capture(logSlot)) } returns Unit

        repository.logRequest(
            method = "GET",
            path = "/test",
            queryParameters = null,
            clientIp = "127.0.0.1",
            userAgent = null,
            headers = emptyMap()
        )

        val captured = logSlot.captured
        assertEquals(null, captured.queryParameters)
        assertEquals(null, captured.userAgent)
        assertEquals(null, captured.statusCode)
        assertEquals(null, captured.responseTimeMs)
        assertEquals(null, captured.contentType)
        assertEquals(null, captured.contentLength)
        assertEquals(null, captured.referer)
    }
}
