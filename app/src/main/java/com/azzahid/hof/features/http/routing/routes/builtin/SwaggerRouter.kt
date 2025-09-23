package com.azzahid.hof.features.http.routing.routes.builtin

import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import io.github.smiley4.ktoropenapi.route
import io.github.smiley4.ktorswaggerui.swaggerUI

internal fun io.ktor.server.routing.Route.addSwaggerRoute(
    route: Route,
    type: RouteType.SwaggerRoute
) {
    route(route.path, {
        description = route.description
    }) {
        swaggerUI("/api/json") {}
    }
}