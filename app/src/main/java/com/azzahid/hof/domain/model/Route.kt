package com.azzahid.hof.domain.model

import com.azzahid.hof.domain.model.serialization.HttpMethodSerializer
import com.azzahid.hof.features.http.routing.routes.addApi
import com.azzahid.hof.features.http.routing.routes.addDirectory
import com.azzahid.hof.features.http.routing.routes.addProxy
import com.azzahid.hof.features.http.routing.routes.addRedirect
import com.azzahid.hof.features.http.routing.routes.addStaticFile
import com.azzahid.hof.features.http.routing.routes.builtin.addNotificationRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addOpenApiRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addStatusRoute
import com.azzahid.hof.features.http.routing.routes.builtin.addSwaggerRoute
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import io.ktor.server.routing.Route as ServerRoute

@Serializable
data class Route(
    val id: String,
    val path: String,
    @Serializable(with = HttpMethodSerializer::class)
    val method: HttpMethod,
    val description: String = "",
    val type: RouteType,
    val isEnabled: Boolean = true,
    val order: Int = 0
)


@Serializable
sealed class RouteType {
    abstract fun install(serverRoute: ServerRoute, route: Route, config: ServerConfiguration)

    @Serializable
    sealed class BuiltInRoute : RouteType()

    @Serializable
    data class ApiRoute(
        val responseBody: String = "",
        val statusCode: Int = 200,
        val headers: Map<String, String> = emptyMap()
    ) : RouteType() {
        fun getKtorStatusCode(): HttpStatusCode = HttpStatusCode.fromValue(statusCode)

        override fun install(serverRoute: ServerRoute, route: Route, config: ServerConfiguration) {
            serverRoute.addApi(route, this, config)
        }
    }

    @Serializable
    data class StaticFile(
        val fileUri: String,
        val mimeType: String? = null
    ) : RouteType() {
        override fun install(serverRoute: ServerRoute, route: Route, config: ServerConfiguration) {
            serverRoute.addStaticFile(route, this)
        }
    }

    @Serializable
    data class Directory(
        val directoryUri: String,
        val allowBrowsing: Boolean = true,
        val indexFile: String? = "index.html"
    ) : RouteType() {
        override fun install(serverRoute: ServerRoute, route: Route, config: ServerConfiguration) {
            serverRoute.addDirectory(route, this)
        }
    }

    @Serializable
    data class RedirectRoute(
        val targetUrl: String,
        val statusCode: Int = 302
    ) : RouteType() {
        fun isPermanentRedirect(): Boolean = statusCode == HttpStatusCode.MovedPermanently.value

        override fun install(serverRoute: ServerRoute, route: Route, config: ServerConfiguration) {
            serverRoute.addRedirect(route, this)
        }
    }

    @Serializable
    data class ProxyRoute(
        val targetUrl: String,
        val preserveHostHeader: Boolean = false,
        val timeout: Long = 30000
    ) : RouteType() {
        override fun install(serverRoute: ServerRoute, route: Route, config: ServerConfiguration) {
            serverRoute.addProxy(route, this)
        }
    }

    @Serializable
    object StatusRoute : BuiltInRoute() {
        override fun install(serverRoute: ServerRoute, route: Route, config: ServerConfiguration) {
            serverRoute.addStatusRoute(route, this, config)
        }
    }

    @Serializable
    object OpenApiRoute : BuiltInRoute() {
        override fun install(serverRoute: ServerRoute, route: Route, config: ServerConfiguration) {
            serverRoute.addOpenApiRoute(route, this)
        }
    }

    @Serializable
    object SwaggerRoute : BuiltInRoute() {
        override fun install(serverRoute: ServerRoute, route: Route, config: ServerConfiguration) {
            serverRoute.addSwaggerRoute(route, this)
        }
    }

    @Serializable
    object NotificationRoute : BuiltInRoute() {
        override fun install(serverRoute: ServerRoute, route: Route, config: ServerConfiguration) {
            serverRoute.addNotificationRoute(route, this, config)
        }
    }

}


