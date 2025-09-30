package com.azzahid.hof.features.http.configuration

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import org.slf4j.event.Level

internal fun Application.configureLogging(logLevel: String) {
    install(CallLogging) {
        level = when (logLevel.uppercase()) {
            "DEBUG" -> Level.DEBUG
            "WARN" -> Level.WARN
            "ERROR" -> Level.ERROR
            else -> Level.INFO
        }
        format { call ->
            val userAgent = call.request.headers["User-Agent"] ?: "unknown"
            "Method: ${call.request.httpMethod.value}, Path: ${call.request.path()}, User-Agent: $userAgent"
        }
    }
}