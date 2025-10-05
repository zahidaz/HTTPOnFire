package com.azzahid.hof.features.http.routing.routes

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.features.http.androidContext
import com.azzahid.hof.features.http.utils.FileServingUtils
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond

internal fun io.ktor.server.routing.Route.addStaticFile(
    route: Route,
    uri: Uri
) {
    get(route.path, {
        description = route.description
    }) {
        val context = call.application.androidContext

        try {
            val documentFile = DocumentFile.fromSingleUri(context, uri)

            if (documentFile != null && documentFile.exists() && documentFile.isFile) {
                FileServingUtils.serveDocumentFile(call, documentFile, context)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    "File not found or URI invalid: $uri"
                )
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Error serving file: ${e.message}")
        }
    }
}
