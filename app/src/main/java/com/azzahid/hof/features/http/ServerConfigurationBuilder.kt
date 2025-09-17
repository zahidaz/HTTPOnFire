package com.azzahid.hof.features.http

import com.azzahid.hof.domain.model.CorsConfiguration
import com.azzahid.hof.domain.model.Endpoint
import com.azzahid.hof.domain.model.ServerConfiguration
import io.ktor.server.application.Application

class ServerConfigurationBuilder {
    private var port: Int = 8080
    private var enableLogs: Boolean = true
    private var autoStart: Boolean = false
    private var endpoints: List<Endpoint> = emptyList()
    private var corsConfig: CorsConfiguration = CorsConfiguration()
    private var enableSwagger: Boolean = true
    private var enableOpenApi: Boolean = true
    private var customHeaders: Map<String, String> = emptyMap()
    private var logLevel: String = "INFO"
    private var customConfigurer: (Application.() -> Unit)? = null

    fun port(port: Int) = apply { this.port = port }

    fun enableLogs(enable: Boolean = true) = apply { this.enableLogs = enable }

    fun autoStart(enable: Boolean = true) = apply { this.autoStart = enable }

    fun endpoints(endpoints: List<Endpoint>) = apply { this.endpoints = endpoints }

    fun cors(config: CorsConfiguration) = apply { this.corsConfig = config }

    fun cors(configure: CorsConfigurationBuilder.() -> Unit) = apply {
        this.corsConfig = CorsConfigurationBuilder().apply(configure).build()
    }

    fun swagger(enable: Boolean = true) = apply { this.enableSwagger = enable }

    fun openApi(enable: Boolean = true) = apply { this.enableOpenApi = enable }

    fun customHeaders(headers: Map<String, String>) = apply { this.customHeaders = headers }

    fun logLevel(level: String) = apply { this.logLevel = level }

    fun customConfigurer(configurer: Application.() -> Unit) = apply {
        this.customConfigurer = configurer
    }

    fun build(): ServerConfiguration = ServerConfiguration(
        port = port,
        enableLogs = enableLogs,
        autoStart = autoStart,
        endpoints = endpoints,
        corsConfiguration = corsConfig,
        enableSwagger = enableSwagger,
        enableOpenApi = enableOpenApi,
        customHeaders = customHeaders,
        logLevel = logLevel,
        customConfigurer = customConfigurer
    )
}

class CorsConfigurationBuilder {
    private var allowAnyHost: Boolean = false
    private var allowedHosts: List<String> = emptyList()
    private var allowedMethods: List<String> =
        listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
    private var allowedHeaders: List<String> = listOf("Content-Type", "Authorization")
    private var allowCredentials: Boolean = false

    fun allowAnyHost(allow: Boolean = true) = apply { this.allowAnyHost = allow }

    fun allowedHosts(hosts: List<String>) = apply { this.allowedHosts = hosts }

    fun allowedMethods(methods: List<String>) = apply { this.allowedMethods = methods }

    fun allowedHeaders(headers: List<String>) = apply { this.allowedHeaders = headers }

    fun allowCredentials(allow: Boolean = true) = apply { this.allowCredentials = allow }

    fun build(): CorsConfiguration = CorsConfiguration(
        allowAnyHost = allowAnyHost,
        allowedHosts = allowedHosts,
        allowedMethods = allowedMethods,
        allowedHeaders = allowedHeaders,
        allowCredentials = allowCredentials
    )
}

fun serverConfiguration(configure: ServerConfigurationBuilder.() -> Unit): ServerConfiguration {
    return ServerConfigurationBuilder().apply(configure).build()
}