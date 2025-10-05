package com.azzahid.hof.features.http.routing.routes

import com.azzahid.hof.domain.model.Route
import io.github.smiley4.ktoropenapi.route
import io.ktor.server.response.respondRedirect

internal fun io.ktor.server.routing.Route.addRedirect(
    route: Route,
    isPermanentRedirect: Boolean,
    url: String
) {
    route(route.path, io.ktor.http.HttpMethod.Get, {
        description = route.description
    }) {
        handle {
            call.respondRedirect(
                url = url,
                permanent = isPermanentRedirect
            )
        }
    }
}