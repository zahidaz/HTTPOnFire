package com.azzahid.hof.features.http

import android.content.Context
import com.azzahid.hof.domain.model.CIOEmbeddedServer
import com.azzahid.hof.domain.model.ServerConfiguration
import com.azzahid.hof.features.http.configuration.configureContentNegotiation
import com.azzahid.hof.features.http.configuration.configureCors
import com.azzahid.hof.features.http.configuration.configureLogging
import com.azzahid.hof.features.http.configuration.configureStatusPages
import io.github.smiley4.ktoropenapi.OpenApi
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import io.ktor.util.AttributeKey


internal fun Application.configureServerWithSettings(
    config: ServerConfiguration,
    httpRequestLogger: HttpRequestLogger? = null
) {
    configureCors(config.corsConfiguration)
    configureContentNegotiation()
    configureStatusPages()

    install(OpenApi)

    if (config.enableLogs) {
        configureLogging(config)
        httpRequestLogger?.let { logger ->
            install(logger.plugin)
        }
    }
}

private val AndroidContextKey = AttributeKey<Context>("AndroidContext")

private fun Application.setAndroidContext(context: Context) {
    attributes.put(AndroidContextKey, context)
}

val Application.androidContext: Context
    get() = attributes[AndroidContextKey]

internal fun buildServerWithConfiguration(
    androidContext: Context,
    config: ServerConfiguration,
    httpRequestLogger: HttpRequestLogger? = null
): CIOEmbeddedServer {
    return embeddedServer(CIO, port = config.port) {
        setAndroidContext(androidContext)
        configureServerWithSettings(config, httpRequestLogger)
        configureRoutingWithRoutes(config)
        config.customConfigurer?.invoke(this)
    }
}

internal fun Application.configureRoutingWithRoutes(config: ServerConfiguration) {
    routing {
        config.routes.filter { it.isEnabled }.forEach { route ->
            route.type.install(this, route, config)
        }
    }
}
