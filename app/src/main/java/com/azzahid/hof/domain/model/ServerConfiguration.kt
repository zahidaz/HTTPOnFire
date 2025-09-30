package com.azzahid.hof.domain.model

data class ServerConfiguration(
    val port: Int,
    val enableLogs: Boolean,
    val autoStart: Boolean,
    val routes: List<Route>,
    val corsConfiguration: CorsConfiguration = CorsConfiguration(),
    val logLevel: String = "INFO",
)

data class CorsConfiguration(
    val allowAnyHost: Boolean = false,
    val allowedHosts: List<String> = emptyList(),
    val allowedMethods: List<String> = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"),
    val allowedHeaders: List<String> = listOf("Content-Type", "Authorization"),
    val allowCredentials: Boolean = false
)