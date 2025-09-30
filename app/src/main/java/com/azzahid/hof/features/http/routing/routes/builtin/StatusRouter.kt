package com.azzahid.hof.features.http.routing.routes.builtin

import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.Success
import io.github.smiley4.ktoropenapi.get
import io.ktor.server.response.respond

internal fun io.ktor.server.routing.Route.addStatusRoute(route: Route) {
    get(route.path, {
        description = route.description
    }) {
        call.respond(Success(data = mapOf("status" to "ok")))
    }
}