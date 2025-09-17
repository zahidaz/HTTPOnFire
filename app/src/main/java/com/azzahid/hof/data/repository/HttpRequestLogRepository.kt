package com.azzahid.hof.data.repository

import com.azzahid.hof.data.database.HttpRequestLogDao
import com.azzahid.hof.domain.model.HttpRequestLog
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class HttpRequestLogRepository(private val logDao: HttpRequestLogDao) {

    fun getAllLogs(): Flow<List<HttpRequestLog>> = logDao.getAllLogs()

    fun getRecentLogs(limit: Int = 100): Flow<List<HttpRequestLog>> = logDao.getRecentLogs(limit)

    fun getLogsByMethod(method: String): Flow<List<HttpRequestLog>> = logDao.getLogsByMethod(method)

    fun getLogsByPath(pathPattern: String): Flow<List<HttpRequestLog>> =
        logDao.getLogsByPath(pathPattern)

    fun getLogsByClientIp(clientIp: String): Flow<List<HttpRequestLog>> =
        logDao.getLogsByClientIp(clientIp)

    fun getLogsSince(startTime: Long): Flow<List<HttpRequestLog>> = logDao.getLogsSince(startTime)

    suspend fun getLogCount(): Int = logDao.getLogCount()

    suspend fun getLogCountSince(startTime: Long): Int = logDao.getLogCountSince(startTime)

    suspend fun logRequest(
        method: String,
        path: String,
        queryParameters: String?,
        clientIp: String,
        userAgent: String?,
        headers: Map<String, List<String>>,
        statusCode: Int? = null,
        responseTimeMs: Long? = null,
        contentType: String? = null,
        contentLength: Long? = null,
        referer: String? = null
    ) {
        val log = HttpRequestLog(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            method = method,
            path = path,
            queryParameters = queryParameters,
            clientIp = clientIp,
            userAgent = userAgent,
            headers = formatHeaders(headers),
            statusCode = statusCode,
            responseTimeMs = responseTimeMs,
            contentType = contentType,
            contentLength = contentLength,
            referer = referer
        )
        logDao.insertLog(log)
    }

    suspend fun clearAllLogs() = logDao.clearAllLogs()

    suspend fun deleteLogsOlderThan(olderThan: Long) = logDao.deleteLogsOlderThan(olderThan)

    suspend fun deleteOldestLogs(count: Int) = logDao.deleteOldestLogs(count)

    suspend fun getLogById(id: String): HttpRequestLog? = logDao.getLogById(id)

    private fun formatHeaders(headers: Map<String, List<String>>): String {
        return headers.entries.joinToString("\n") { (key, values) ->
            "$key: ${values.joinToString(", ")}"
        }
    }

    suspend fun cleanupOldLogs(maxLogs: Int = 1000) {
        val currentCount = getLogCount()
        if (currentCount > maxLogs) {
            val deleteCount = currentCount - maxLogs
            deleteOldestLogs(deleteCount)
        }
    }

    suspend fun cleanupLogsByAge(maxAgeMillis: Long) {
        val cutoffTime = System.currentTimeMillis() - maxAgeMillis
        deleteLogsOlderThan(cutoffTime)
    }

    suspend fun cleanupOldLogsByDate(cutoffTime: Long) {
        deleteLogsOlderThan(cutoffTime)
    }
}