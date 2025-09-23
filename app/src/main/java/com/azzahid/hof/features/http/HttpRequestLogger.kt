package com.azzahid.hof.features.http

import com.azzahid.hof.data.repository.HttpRequestLogRepository
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.CallSetup
import io.ktor.server.application.hooks.ResponseSent
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.userAgent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HttpRequestLogger(private val logRepository: HttpRequestLogRepository) {

    private val requestTimings = mutableMapOf<ApplicationCall, Long>()

    val plugin = createApplicationPlugin(name = "HttpRequestLogger") {
        on(CallSetup) { call ->
            requestTimings[call] = System.currentTimeMillis()
        }

        on(ResponseSent) { call ->
            val startTime = requestTimings.remove(call)
            val responseTime = startTime?.let { System.currentTimeMillis() - it }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    logRequest(call, responseTime)
                } catch (e: Exception) {
                    android.util.Log.e("HttpRequestLogger", "Failed to log request", e)
                }
            }
        }
    }

    private suspend fun logRequest(call: ApplicationCall, responseTimeMs: Long?) {
        val request = call.request
        val response = call.response

        val clientIp = extractClientIp(call)
        val queryString = request.queryParameters.entries()
            .takeIf { it.isNotEmpty() }
            ?.joinToString("&") { (key, values) ->
                values.joinToString("&") { value -> "$key=$value" }
            }

        val headers = try {
            request.headers.entries().associate { (key, values) ->
                key to values
            }
        } catch (e: Exception) {
            android.util.Log.w("HttpRequestLogger", "Failed to extract headers: ${e.message}")
            emptyMap()
        }

        logRepository.logRequest(
            method = request.httpMethod.value,
            path = request.path(),
            queryParameters = queryString,
            clientIp = clientIp,
            userAgent = try {
                request.userAgent()
            } catch (e: Exception) {
                "unknown"
            },
            headers = headers,
            statusCode = response.status()?.value,
            responseTimeMs = responseTimeMs,
            contentType = try {
                response.headers["Content-Type"]
            } catch (e: Exception) {
                null
            },
            contentLength = try {
                response.headers["Content-Length"]?.toLongOrNull()
            } catch (e: Exception) {
                null
            },
            referer = try {
                request.headers["Referer"]
            } catch (e: Exception) {
                null
            }
        )
    }

    private fun extractClientIp(call: ApplicationCall): String {
        return try {
            val request = call.request
            request.headers["X-Forwarded-For"]?.split(",")?.firstOrNull()?.trim()
                ?: request.headers["X-Real-IP"]
                ?: request.headers["X-Client-IP"]
                ?: request.headers["X-Forwarded"]
                ?: request.headers["Forwarded-For"]
                ?: request.headers["Forwarded"]
                ?: try {
                    call.request.local.remoteHost
                } catch (e: Exception) {
                    "unknown"
                }
        } catch (e: Exception) {
            android.util.Log.w("HttpRequestLogger", "Failed to extract client IP: ${e.message}")
            "unknown"
        }
    }
}