package com.azzahid.hof.features.http

import android.content.Context
import android.os.Build
import com.azzahid.hof.domain.model.CIOEmbeddedServer
import com.azzahid.hof.domain.model.CorsConfiguration
import com.azzahid.hof.domain.model.Route
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
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.coroutineContext

internal suspend fun buildServerWithConfiguration(
    androidContext: Context,
    port: Int,
    corsConfig: CorsConfiguration,
    enableLogs: Boolean,
    logLevel: String,
    routes: List<Route>,
    httpRequestLogger: HttpRequestLogger? = null
): CIOEmbeddedServer {
    return CoroutineScope(coroutineContext).embeddedServer(
        CIO,
        port = port,
        parentCoroutineContext = coroutineContext
    ) {
        setAndroidContext(androidContext)
        configureServerWithSettings(corsConfig, enableLogs, logLevel, httpRequestLogger)
        configureRoutingWithRoutes(routes)
    }
}

private val AndroidContextKey = AttributeKey<Context>("AndroidContext")

private fun Application.setAndroidContext(context: Context) {
    attributes.put(AndroidContextKey, context)
}

val Application.androidContext: Context
    get() = attributes[AndroidContextKey]


internal fun Application.configureServerWithSettings(
    corsConfig: CorsConfiguration,
    enableLogs: Boolean,
    logLevel: String,
    httpRequestLogger: HttpRequestLogger? = null
) {
    configureCors(corsConfig)
    configureContentNegotiation()
    configureStatusPages()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        install(OpenApi)
    }

    if (enableLogs) {
        configureLogging(logLevel)
        httpRequestLogger?.let { logger ->
            install(logger.plugin)
        }
    }
}

internal fun Application.configureRoutingWithRoutes(routes: List<Route>) {
    routing {
        routes.filter { it.isEnabled }.forEach { route ->
            route.type.handler(route).invoke(this)
        }
    }
}
