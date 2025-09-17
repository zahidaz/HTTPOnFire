package com.azzahid.hof.features.http

import com.azzahid.hof.data.repository.EndpointRepository
import com.azzahid.hof.data.repository.HttpRequestLogRepository
import com.azzahid.hof.data.repository.SettingsRepository
import com.azzahid.hof.domain.model.CorsConfiguration
import com.azzahid.hof.domain.model.Endpoint
import com.azzahid.hof.domain.model.EndpointType
import com.azzahid.hof.domain.model.ServerConfiguration
import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.slf4j.event.Level
import kotlin.reflect.KClass

class ServerConfigurationService(
    private val settingsRepository: SettingsRepository,
    private val endpointRepository: EndpointRepository,
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
                port = port.toIntOrNull() ?: 8080,
                enableLogs = enableLogs,
                autoStart = autoStart,
                endpoints = emptyList(),
                corsConfiguration = CorsConfiguration(
                    allowAnyHost = corsAllowAnyHost,
                    allowedHosts = if (corsAllowedHosts.isNotBlank()) {
                        corsAllowedHosts.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    } else {
                        emptyList()
                    }
                )
            )
        }.combine(settingsRepository.corsAllowCredentials) { config, corsAllowCredentials ->
            config.copy(
                corsConfiguration = config.corsConfiguration.copy(
                    allowCredentials = corsAllowCredentials
                )
            )
        }.combine(endpointRepository.getEnabledEndpoints()) { config, endpoints ->
            config.copy(endpoints = endpoints)
        }
    }

    private suspend fun getServerConfigurationSnapshot(): ServerConfiguration {
        return getServerConfiguration().first()
    }

    suspend fun buildConfiguredServer(): CIOEmbeddedServer {
        val config = getServerConfigurationSnapshot()
        val httpRequestLogger = httpRequestLogRepository?.let { HttpRequestLogger(it) }
        return buildServerWithConfiguration(config, httpRequestLogger)
    }
}

internal fun Application.configureServerWithSettings(
    config: ServerConfiguration,
    httpRequestLogger: HttpRequestLogger? = null
) {
    configureCorsWithSettings(config.corsConfiguration)
    configureContentNegotiation()
    configureStatusPages()

    if (config.enableOpenApi) {
        install(OpenApi)
    }

    if (config.enableLogs) {
        configureLoggingWithSettings(config)
        httpRequestLogger?.let { logger ->
            install(logger.plugin)
        }
    }
}

private fun Application.configureCorsWithSettings(corsConfig: CorsConfiguration) {
    install(CORS) {
        if (corsConfig.allowAnyHost) {
            anyHost()
        } else {
            corsConfig.allowedHosts.forEach { host ->
                allowHost(host)
            }
        }

        corsConfig.allowedMethods.forEach { method ->
            when (method.uppercase()) {
                "GET" -> allowMethod(io.ktor.http.HttpMethod.Get)
                "POST" -> allowMethod(io.ktor.http.HttpMethod.Post)
                "PUT" -> allowMethod(io.ktor.http.HttpMethod.Put)
                "DELETE" -> allowMethod(io.ktor.http.HttpMethod.Delete)
                "PATCH" -> allowMethod(io.ktor.http.HttpMethod.Patch)
                "OPTIONS" -> allowMethod(io.ktor.http.HttpMethod.Options)
            }
        }

        corsConfig.allowedHeaders.forEach { header ->
            allowHeader(header)
        }

        allowCredentials = corsConfig.allowCredentials
    }
}

private fun Application.configureContentNegotiation() {
    install(ContentNegotiation) {
        val module = SerializersModule {
            contextual(Success::class as KClass<*>) {
                Success.serializer(AnySerializer)
            }
        }

        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            serializersModule = module
        })
    }
}

private fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                Failure(error = cause.message ?: "Invalid request")
            )
        }
        exception<IllegalStateException> { call, cause ->
            call.respond(
                HttpStatusCode.Conflict,
                Failure(error = cause.message ?: "Request could not be completed")
            )
        }
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                Failure(
                    error = mapOf(
                        "type" to cause::class.simpleName,
                        "message" to cause.message,
                        "localizedMessage" to cause.localizedMessage,
                        "stackTrace" to cause.stackTrace.take(5).map { it.toString() },
                        "cause" to cause.cause?.let {
                            mapOf(
                                "type" to it::class.simpleName,
                                "message" to it.message
                            )
                        }
                    ).toString())
            )
        }
//        status(HttpStatusCode.NotFound) { call, status ->
//            call.respond(status, Failure(error = "Not Found"))
//        }
    }
}

private fun Application.configureLoggingWithSettings(config: ServerConfiguration) {
    install(CallLogging) {
        level = when (config.logLevel.uppercase()) {
            "DEBUG" -> Level.DEBUG
            "WARN" -> Level.WARN
            "ERROR" -> Level.ERROR
            else -> Level.INFO
        }
//        filter { call ->
//            call.request.path().startsWith("/api") || call.request.path().startsWith("/")
//        }
        format { call ->
            val userAgent = call.request.headers["User-Agent"] ?: "unknown"
            "Method: ${call.request.httpMethod.value}, Path: ${call.request.path()}, User-Agent: $userAgent"
        }
    }
}

internal fun buildServerWithConfiguration(
    config: ServerConfiguration,
    httpRequestLogger: HttpRequestLogger? = null
): CIOEmbeddedServer {
    return embeddedServer(CIO, port = config.port) {
        configureServerWithSettings(config, httpRequestLogger)
        configureRoutingWithEndpoints(config)

        config.customConfigurer?.invoke(this)
    }
}

internal fun Application.configureRoutingWithEndpoints(config: ServerConfiguration) {
    routing {
        configureServerApiRoutes(config)
        configureUserEndpoints(config)
//        configureDefaultWebRoutes()
    }
}

private fun Route.configureServerApiRoutes(config: ServerConfiguration) {
    route("/api") {
        get("/status", {
            description = "Checks the health/status of the API"
        }) {
            config.customHeaders.forEach { (key, value) ->
                call.response.headers.append(key, value)
            }
            call.respond(
                Success(
                    data = mapOf(
                        "status" to "ok",
                        "endpoints" to config.endpoints.size,
                    )
                )
            )
        }

        if (config.enableOpenApi) {
            route("json") {
                openApi()
            }
        }

        if (config.enableSwagger) {
            route("/swagger") {
                swaggerUI("/api/json") {
                }
            }
        }
    }
}

private fun Route.configureUserEndpoints(config: ServerConfiguration) {
    config.endpoints.forEach { endpoint ->
        when (val type = endpoint.type) {
            is EndpointType.ApiEndpoint -> {
                configureApiEndpoint(endpoint, type, config)
            }

            is EndpointType.RedirectEndpoint -> {
                configureRedirectEndpoint(endpoint, type)
            }

            is EndpointType.StaticFile -> {
                configureStaticFileEndpoint(endpoint, type)
            }

            is EndpointType.Directory -> {
                configureDirectoryEndpoint(endpoint, type)
            }

            is EndpointType.ProxyEndpoint -> {
                configureProxyEndpoint(endpoint, type)
            }
        }
    }
}

private fun Route.configureApiEndpoint(
    endpoint: Endpoint,
    type: EndpointType.ApiEndpoint,
    config: ServerConfiguration
) {
    route(endpoint.path, endpoint.method.toKtorHttpMethod()) {
        handle {
            config.customHeaders.forEach { (key, value) ->
                call.response.headers.append(key, value)
            }
            type.headers.forEach { (key, value) ->
                call.response.headers.append(key, value)
            }
            call.respondText(
                type.responseBody,
                status = type.getKtorStatusCode()
            )
        }
    }
}

private fun Route.configureRedirectEndpoint(
    endpoint: Endpoint,
    type: EndpointType.RedirectEndpoint
) {
    route(endpoint.path, io.ktor.http.HttpMethod.Get) {
        handle {
            call.respondRedirect(
                type.targetUrl,
                permanent = type.isPermanentRedirect()
            )
        }
    }
}

private fun Route.configureStaticFileEndpoint(
    endpoint: Endpoint,
    type: EndpointType.StaticFile
) {
    get(endpoint.path) {
        try {
            val file = java.io.File(type.filePath)
            if (file.exists() && file.isFile) {
                val contentType = type.mimeType ?: "application/octet-stream"
                call.response.headers.append(HttpHeaders.ContentType, contentType)
                call.response.headers.append("Cache-Control", "no-cache, no-store, must-revalidate")
                call.response.headers.append("Pragma", "no-cache")
                call.response.headers.append("Expires", "0")
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound, "File not found: ${type.filePath}")
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error serving file: ${e.message}")
        }
    }
}

private fun Route.configureDirectoryEndpoint(
    endpoint: Endpoint,
    type: EndpointType.Directory
) {
    route(endpoint.path) {
        get("/{path...}") {
            val relativePath = call.parameters.getAll("path")?.joinToString("/") ?: ""
            val fullPath = java.io.File(type.directoryPath, relativePath)

            try {
                when {
                    !fullPath.exists() -> {
                        call.respond(HttpStatusCode.NotFound, "Path not found")
                    }

                    fullPath.isFile -> {
                        call.response.headers.append(
                            "Cache-Control",
                            "no-cache, no-store, must-revalidate"
                        )
                        call.response.headers.append("Pragma", "no-cache")
                        call.response.headers.append("Expires", "0")
                        call.respondFile(fullPath)
                    }

                    fullPath.isDirectory -> {
                        if (type.indexFile != null) {
                            val indexFile = java.io.File(fullPath, type.indexFile)
                            if (indexFile.exists() && indexFile.isFile) {
                                call.response.headers.append(
                                    "Cache-Control",
                                    "no-cache, no-store, must-revalidate"
                                )
                                call.response.headers.append("Pragma", "no-cache")
                                call.response.headers.append("Expires", "0")
                                call.respondFile(indexFile)
                                return@get
                            }
                        }

                        if (type.allowBrowsing) {
                            val listing =
                                generateDirectoryListing(fullPath, endpoint.path, relativePath)
                            call.response.headers.append(
                                "Cache-Control",
                                "no-cache, no-store, must-revalidate"
                            )
                            call.response.headers.append("Pragma", "no-cache")
                            call.response.headers.append("Expires", "0")
                            call.respondText(listing, ContentType.Text.Html)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Directory browsing is disabled")
                        }
                    }
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Error accessing path: ${e.message}"
                )
            }
        }

        get {
            call.respondRedirect("${endpoint.path}/", permanent = false)
        }
    }
}

private fun generateDirectoryListing(
    directory: java.io.File,
    basePath: String,
    relativePath: String
): String {
    val files = directory.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
        ?: emptyList()
    val currentPath = if (relativePath.isEmpty()) basePath else "$basePath/$relativePath"

    return buildString {
        append("<!DOCTYPE html><html><head><title>Directory listing for $currentPath</title>")
        append("<style>body{font-family:monospace;margin:40px;}a{text-decoration:none;color:#0066cc;}a:hover{text-decoration:underline;}.dir{font-weight:bold;}</style>")
        append("</head><body>")
        append("<h1>Directory listing for $currentPath</h1><hr>")

        if (relativePath.isNotEmpty()) {
            val parentPath = relativePath.substringBeforeLast("/", "")
            val parentUrl = if (parentPath.isEmpty()) basePath else "$basePath/$parentPath"
            append("<a href=\"$parentUrl/\">[Parent Directory]</a><br><br>")
        }

        files.forEach { file ->
            val fileName = file.name
            val fileUrl =
                if (relativePath.isEmpty()) "$basePath/$fileName" else "$basePath/$relativePath/$fileName"
            val displayName = if (file.isDirectory) "$fileName/" else fileName
            val cssClass = if (file.isDirectory) "dir" else ""

            append("<a href=\"$fileUrl\" class=\"$cssClass\">$displayName</a>")
            if (file.isFile) {
                append(" (${file.length()} bytes)")
            }
            append("<br>")
        }

        append("<hr></body></html>")
    }
}

private fun Route.configureProxyEndpoint(
    endpoint: Endpoint,
    type: EndpointType.ProxyEndpoint
) {
    route(endpoint.path) {
        handle {
            call.respond(HttpStatusCode.NotImplemented, "Proxy endpoints not yet implemented")
        }
    }
}

private fun Route.configureDefaultWebRoutes() {
    val webFiles = AssetsResourceProvider("web")

    get {
        call.respondRedirect("/index.html", permanent = false)
    }

    get("/{file...}") {
        val path = call.parameters.getAll("file")?.joinToString("/")
        if (path == null) {
            call.respond(HttpStatusCode.NotFound)
            return@get
        }

        val resource = webFiles.getResource(path)
        if (resource != null) {
            call.respondAssetNoCache(resource)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }
}