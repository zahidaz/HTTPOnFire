package com.azzahid.hof.features.http.routing.routes.builtin

import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.features.http.utils.DashboardHtmlBuilder
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.ContentType
import io.ktor.server.response.respondText

internal fun io.ktor.server.routing.Route.addDashboardRoute(route: Route) {
    get(route.path, {
        description = route.description
    }) {
        val scheme = call.request.local.scheme
        val host = call.request.local.serverHost
        val port = call.request.local.serverPort
        val baseUrl = "$scheme://$host:$port"
        val html = DashboardHtmlBuilder.build(baseUrl)
        call.respondText(html, ContentType.Text.Html)
    }
}
