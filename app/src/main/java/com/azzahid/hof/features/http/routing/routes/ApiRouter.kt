package com.azzahid.hof.features.http.routing.routes

import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import com.azzahid.hof.domain.model.ServerConfiguration
import io.github.smiley4.ktoropenapi.route
import io.ktor.server.response.respondText

internal fun io.ktor.server.routing.Route.addApi(
    route: Route,
    type: RouteType.ApiRoute,
    config: ServerConfiguration
) {
    route(route.path, route.method, {
        description = route.description
    }) {
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