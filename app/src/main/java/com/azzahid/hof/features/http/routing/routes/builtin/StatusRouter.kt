package com.azzahid.hof.features.http.routing.routes.builtin

import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import com.azzahid.hof.domain.model.ServerConfiguration
import com.azzahid.hof.domain.model.Success
import io.github.smiley4.ktoropenapi.get
import io.ktor.server.response.respond

internal fun io.ktor.server.routing.Route.addStatusRoute(
    route: Route,
    type: RouteType.StatusRoute,
    config: ServerConfiguration
) {
    get(route.path, {
        description = route.description
    }) {
        config.customHeaders.forEach { (key, value) ->
            call.response.headers.append(key, value)
        }

        call.respond(Success(data = mapOf("status" to "ok")))
    }
}