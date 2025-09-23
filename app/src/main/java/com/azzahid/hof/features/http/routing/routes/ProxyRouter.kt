package com.azzahid.hof.features.http.routing.routes

import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond

internal fun io.ktor.server.routing.Route.addProxy(
    route: Route,
    type: RouteType.ProxyRoute
) {
    route(route.path, {
        description = route.description
    }) {
        handle {
            call.respond(HttpStatusCode.NotImplemented, "Proxy Routes not yet implemented")
        }
    }
}