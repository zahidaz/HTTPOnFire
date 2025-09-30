package com.azzahid.hof.features.http.routing.routes.builtin

import android.os.Build
import com.azzahid.hof.domain.model.Route
import io.github.smiley4.ktoropenapi.route
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.get

internal fun io.ktor.server.routing.Route.addSwaggerRoute(route: Route) {
    route(route.path, {
        description = route.description
    }) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            swaggerUI("/api/json") {}
        } else {
            get {
                call.respondText(
                    "Swagger UI requires Android 12 (API 31) or higher",
                    status = HttpStatusCode.NotImplemented
                )
            }
        }
    }
}