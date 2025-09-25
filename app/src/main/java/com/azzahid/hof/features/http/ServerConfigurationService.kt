package com.azzahid.hof.features.http

import com.azzahid.hof.Constants
import com.azzahid.hof.data.repository.HttpRequestLogRepository
import com.azzahid.hof.data.repository.RouteRepository
import com.azzahid.hof.data.repository.SettingsRepository
import com.azzahid.hof.domain.model.CIOEmbeddedServer
import com.azzahid.hof.domain.model.CorsConfiguration
import com.azzahid.hof.domain.model.ServerConfiguration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class ServerConfigurationService(
    private val settingsRepository: SettingsRepository,
    private val routeRepository: RouteRepository,
    private val httpRequestLogRepository: HttpRequestLogRepository? = null
) {

    fun getServerConfiguration(): Flow<ServerConfiguration> {
        return combine(
            settingsRepository.defaultPort,
            settingsRepository.enableLogs,
            settingsRepository.autoStart,
            settingsRepository.corsAllowAnyHost,
            settingsRepository.corsAllowedHosts
        ) { port, enableLogs, autoStart, corsAllowAnyHost, corsAllowedHosts ->
            ServerConfiguration(
                port = validatePort(port),
                enableLogs = enableLogs,
                autoStart = autoStart,
                routes = emptyList(),
                corsConfiguration = buildCorsConfiguration(corsAllowAnyHost, corsAllowedHosts)
            )
        }.combine(settingsRepository.corsAllowCredentials) { config, corsAllowCredentials ->
            config.copy(
                corsConfiguration = config.corsConfiguration.copy(
                    allowCredentials = corsAllowCredentials
                )
            )
        }.combine(settingsRepository.enableSwagger) { config, enableSwagger ->
            config.copy(enableSwagger = enableSwagger)
        }.combine(settingsRepository.enableOpenApi) { config, enableOpenApi ->
            config.copy(enableOpenApi = enableOpenApi)
        }.combine(settingsRepository.enableStatus) { config, enableStatus ->
            config.copy(enableStatus = enableStatus)
        }.combine(settingsRepository.enableNotification) { config, enableNotification ->
            config.copy(enableNotification = enableNotification)
        }.combine(routeRepository.getEnabledRoutes()) { config, userRoutes ->
            val builtInRoutes = routeRepository.getAllBuiltInRoutes(config)
            config.copy(routes = userRoutes + builtInRoutes)
        }
    }

    private fun validatePort(portString: String): Int {
        val port = portString.toIntOrNull() ?: Constants.DEFAULT_PORT
        return when {
            port < 1024 -> Constants.DEFAULT_PORT
            port > 65535 -> Constants.DEFAULT_PORT
            else -> port
        }
    }

    private fun buildCorsConfiguration(
        allowAnyHost: Boolean,
        allowedHosts: String
    ): CorsConfiguration {
        return CorsConfiguration(
            allowAnyHost = allowAnyHost,
            allowedHosts = if (allowedHosts.isNotBlank()) {
                parseCorsHosts(allowedHosts)
            } else {
                emptyList()
            }
        )
    }

    private fun parseCorsHosts(hostsString: String): List<String> {
        return hostsString
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() && isValidHost(it) }
    }

    private fun isValidHost(host: String): Boolean {
        return host.matches(Regex("^[a-zA-Z0-9.-]+$")) && !host.startsWith("-") && !host.endsWith("-")
    }


    private suspend fun getServerConfigurationSnapshot(): ServerConfiguration {
        return getServerConfiguration().first()
    }

    suspend fun buildConfiguredServer(androidContext: android.content.Context): CIOEmbeddedServer {
        val config = getServerConfigurationSnapshot()
        val httpRequestLogger = httpRequestLogRepository?.let { HttpRequestLogger(it) }
        return buildServerWithConfiguration(androidContext, config, httpRequestLogger)
    }
}


