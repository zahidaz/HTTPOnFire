package com.azzahid.hof.features.http.routing.routes.builtin

import android.os.Build
import com.azzahid.hof.domain.model.Route
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.get

internal fun io.ktor.server.routing.Route.addOpenApiRoute(route: Route) {
    route(route.path, {
        description = route.description
    }) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            openApi()
        } else {
            get {
                call.respondText(
                    "OpenAPI documentation requires Android 12 (API 31) or higher",
                    status = HttpStatusCode.NotImplemented
                )
            }
        }
    }
}