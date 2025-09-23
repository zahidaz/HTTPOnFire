package com.azzahid.hof.features.http.routing.routes

import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import com.azzahid.hof.features.http.androidContext
import com.azzahid.hof.features.http.utils.DirectoryListingGenerator
import com.azzahid.hof.features.http.utils.FileServingUtils
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText

internal fun io.ktor.server.routing.Route.addDirectory(
    route: Route,
    type: RouteType.Directory
) {
    route(route.path, {
        description = route.description
    }) {
        get("/{path...}") {
            val context = call.application.androidContext
            val relativePath = call.parameters.getAll("path")?.joinToString("/") ?: ""

            try {
                val baseUri = type.directoryUri.toUri()
                val baseDocumentFile = DocumentFile.fromTreeUri(context, baseUri)

                if (baseDocumentFile == null || !baseDocumentFile.exists()) {
                    call.respond(HttpStatusCode.NotFound, "Directory not found")
                    return@get
                }

                val targetFile = if (relativePath.isEmpty()) {
                    baseDocumentFile
                } else {
                    relativePath.split("/")
                        .fold(baseDocumentFile as DocumentFile?) { current, segment ->
                            current?.findFile(segment)
                        }
                }

                when {
                    targetFile == null || !targetFile.exists() -> {
                        call.respond(HttpStatusCode.NotFound, "Path not found")
                    }

                    targetFile.isFile -> {
                        FileServingUtils.serveDocumentFile(call, targetFile, context)
                    }

                    targetFile.isDirectory -> {
                        if (type.indexFile != null) {
                            val indexFile = targetFile.findFile(type.indexFile)
                            if (indexFile != null && indexFile.exists() && indexFile.isFile) {
                                FileServingUtils.serveDocumentFile(call, indexFile, context)
                                return@get
                            }
                        }

                        if (type.allowBrowsing) {
                            val listing = DirectoryListingGenerator.generateFromDocumentFile(
                                targetFile,
                                route.path,
                                relativePath
                            )
                            call.response.headers.append(
                                HttpHeaders.CacheControl,
                                CacheControl.NoCache(null).toString()
                            )
                            call.respondText(listing, ContentType.Text.Html)
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "Directory browsing is disabled")
                        }
                    }
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Error accessing path: ${e.message}"
                )
            }
        }

        get {
            call.respondRedirect("${route.path}/", permanent = false)
        }
    }
}