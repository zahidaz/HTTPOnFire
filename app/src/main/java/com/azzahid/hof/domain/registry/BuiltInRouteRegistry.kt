package com.azzahid.hof.domain.registry

import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType

import io.ktor.http.HttpMethod

object BuiltInRouteRegistry {
    val routes = listOf(
        Route(
            id = "built-in-status",
            path = "/api/status",
            method = HttpMethod.Get,
            description = "Server health and status check",
            type = RouteType.StatusRoute,
            isEnabled = true,
            order = -1000
        ),
        Route(
            id = "built-in-openapi",
            path = "/api/json",
            method = HttpMethod.Get,
            description = "OpenAPI JSON specification",
            type = RouteType.OpenApiRoute,
            isEnabled = true,
            order = -999
        ),
        Route(
            id = "built-in-swagger",
            path = "/api/swagger",
            method = HttpMethod.Get,
            description = "Swagger UI documentation",
            type = RouteType.SwaggerRoute,
            isEnabled = true,
            order = -998
        ),
        Route(
            id = "built-in-notification",
            path = "/api/notify",
            method = HttpMethod.Post,
            description = "Trigger device notifications",
            type = RouteType.NotificationRoute,
            isEnabled = true,
            order = -997
        ),
        Route(
            id = "built-in-proxy",
            path = "/api/proxy",
            method = HttpMethod.Get,
            description = "Forward requests to external URLs",
            type = RouteType.ProxyRoute,
            isEnabled = true,
            order = -996
        )
    )

    fun getPreferenceKey(route: Route): String {
        val name = route.id.removePrefix("built-in-")
        return "enable_$name"
    }

    fun findByRouteType(routeType: RouteType.BuiltInRoute): Route? {
        return routes.find { it.type == routeType }
    }

    fun getAllPreferenceKeys(): List<String> = routes.map { getPreferenceKey(it) }
}