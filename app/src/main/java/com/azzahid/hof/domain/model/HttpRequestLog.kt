package com.azzahid.hof.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "http_request_logs")
data class HttpRequestLog(
    @PrimaryKey
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val method: String,
    val path: String,
    val queryParameters: String?,
    val clientIp: String,
    val userAgent: String?,
    val headers: String,
    val statusCode: Int?,
    val responseTimeMs: Long?,
    val contentType: String?,
    val contentLength: Long?,
    val referer: String?
) {
    fun getFormattedTimestamp(): String {
        return try {
            val date = Date(timestamp)
            val formatter = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
            formatter.format(date)
        } catch (e: Exception) {
            "Invalid time"
        }
    }

    fun getFormattedDetails(): String {
        return buildString {
            appendLine("Timestamp: ${getFormattedTimestamp()}")
            appendLine("Method: $method")
            appendLine("Path: $path")
            if (!queryParameters.isNullOrBlank()) {
                appendLine("Query: $queryParameters")
            }
            appendLine("Client IP: $clientIp")
            statusCode?.let { appendLine("Status: $it") }
            responseTimeMs?.let { appendLine("Response Time: ${it}ms") }
            userAgent?.let { appendLine("User Agent: $it") }
            contentType?.let { appendLine("Content Type: $it") }
            contentLength?.let { appendLine("Content Length: ${it} bytes") }
            referer?.let { appendLine("Referer: $it") }
            appendLine("Headers:")
            appendLine(headers)
        }
    }
}