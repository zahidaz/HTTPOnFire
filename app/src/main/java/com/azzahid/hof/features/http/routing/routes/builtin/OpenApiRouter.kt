package com.azzahid.hof.features.http.routing.routes.builtin

import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.route

internal fun io.ktor.server.routing.Route.addOpenApiRoute(
    route: Route,
    type: RouteType.OpenApiRoute
) {
    route(route.path, {
        description = route.description
    }) {
        openApi()
    }
}