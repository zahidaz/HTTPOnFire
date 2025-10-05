package com.azzahid.hof.features.http.routing.routes.builtin

import com.azzahid.hof.domain.model.Failure
import com.azzahid.hof.domain.model.Route
import io.github.smiley4.ktoropenapi.route
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import kotlinx.coroutines.withTimeout
import java.net.URL

internal fun io.ktor.server.routing.Route.addProxyRoute(route: Route) {
    route("{...}", {
        description = route.description
    }) {
        handle {
            try {
                val fullPath = call.request.path()

                if (!fullPath.startsWith(route.path)) {
                    return@handle
                }

                val targetUrl = fullPath.removePrefix(route.path).removePrefix("/")

                if (targetUrl.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        Failure(error = "Target URL is required after ${route.path}/")
                    )
                    return@handle
                }

                val isValidUrl = try {
                    URL(targetUrl)
                    targetUrl.startsWith("http://") || targetUrl.startsWith("https://")
                } catch (_: Exception) {
                    false
                }

                if (!isValidUrl) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        Failure(error = "Invalid target URL. Must start with http:// or https://")
                    )
                    return@handle
                }

                val timeout = call.request.headers["X-Proxy-Timeout"]?.toLongOrNull() ?: 30000L
                val render = call.request.headers["X-Proxy-Render"]?.toBooleanStrictOrNull() ?: false

                if (render) {
                    call.respond(
                        HttpStatusCode.NotImplemented,
                        Failure(error = "Rendering feature not yet implemented. Coming in Phase 2.")
                    )
                    return@handle
                }

                val forwardHeaders = call.request.headers.entries()
                    .filter { (key, _) -> !key.startsWith("X-Proxy-", ignoreCase = true) }
                    .filter { (key, _) -> key !in listOf(HttpHeaders.Host, HttpHeaders.Connection, HttpHeaders.ContentLength) }

                val method = call.request.httpMethod
                val requestBody = if (method in listOf(HttpMethod.Post, HttpMethod.Put, HttpMethod.Patch)) {
                    call.receiveText()
                } else {
                    null
                }

                val client = HttpClient(CIO) {
                    expectSuccess = false
                    engine {
                        requestTimeout = timeout
                    }
                }

                try {
                    withTimeout(timeout + 5000) {
                        val response: HttpResponse = client.request(targetUrl) {
                            this.method = method

                            forwardHeaders.forEach { (key, values) ->
                                values.forEach { value ->
                                    header(key, value)
                                }
                            }

                            requestBody?.let {
                                setBody(it)
                            }
                        }

                        val responseBody = response.bodyAsText()
                        val statusCode = HttpStatusCode.fromValue(response.status.value)

                        response.headers.entries().forEach { (key, values) ->
                            if (key !in listOf(HttpHeaders.TransferEncoding, HttpHeaders.ContentLength)) {
                                values.forEach { value ->
                                    call.response.headers.append(key, value)
                                }
                            }
                        }

                        call.respondText(
                            text = responseBody,
                            status = statusCode,
                            contentType = response.contentType()
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadGateway,
                        Failure(error = "Proxy request failed: ${e.message}")
                    )
                } finally {
                    client.close()
                }

            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    Failure(error = "Internal error: ${e.message}")
                )
            }
        }
    }
}
