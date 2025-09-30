package com.azzahid.hof.features.http.routing.routes

import com.azzahid.hof.domain.model.Route
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText

internal fun io.ktor.server.routing.Route.addApi(
    route: Route,
    headers: Map<String, String>,
    responseBody: String,
    status: HttpStatusCode
) {
    route(route.path, route.method, {
        description = route.description
    }) {
        handle {
            headers.forEach { (key, value) ->
                call.response.headers.append(key, value)
            }
            call.respondText(
                responseBody,
                status = status
            )
        }
    }
}