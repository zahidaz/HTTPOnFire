package com.azzahid.hof.domain.model

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

@Serializable
sealed class EndpointType {
    @Serializable
    data class ApiEndpoint(
        val responseBody: String = "",
        val statusCode: Int = 200,
        val headers: Map<String, String> = emptyMap()
    ) : EndpointType() {
        fun getKtorStatusCode(): HttpStatusCode = HttpStatusCode.fromValue(statusCode)
    }

    @Serializable
    data class StaticFile(
        val filePath: String,
        val mimeType: String? = null
    ) : EndpointType()

    @Serializable
    data class Directory(
        val directoryPath: String,
        val allowBrowsing: Boolean = true,
        val indexFile: String? = "index.html"
    ) : EndpointType()

    @Serializable
    data class RedirectEndpoint(
        val targetUrl: String,
        val statusCode: Int = 302
    ) : EndpointType() {
        fun isPermanentRedirect(): Boolean = statusCode == HttpStatusCode.MovedPermanently.value
    }

    @Serializable
    data class ProxyEndpoint(
        val targetUrl: String,
        val preserveHostHeader: Boolean = false,
        val timeout: Long = 30000
    ) : EndpointType()

}

@Serializable
data class Endpoint(
    val id: String,
    val path: String,
    val method: HttpMethod,
    val description: String = "",
    val type: EndpointType,
    val isEnabled: Boolean = true,
    val order: Int = 0
)

@Serializable
enum class HttpMethod {
    GET, POST, PUT, DELETE, PATCH;

    fun toKtorHttpMethod(): io.ktor.http.HttpMethod = when (this) {
        GET -> io.ktor.http.HttpMethod.Get
        POST -> io.ktor.http.HttpMethod.Post
        PUT -> io.ktor.http.HttpMethod.Put
        DELETE -> io.ktor.http.HttpMethod.Delete
        PATCH -> io.ktor.http.HttpMethod.Patch
    }
}