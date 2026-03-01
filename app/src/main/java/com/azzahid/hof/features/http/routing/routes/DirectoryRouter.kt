package com.azzahid.hof.features.http.routing.routes

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.features.http.androidContext
import com.azzahid.hof.features.http.utils.DirectoryListingGenerator
import com.azzahid.hof.features.http.utils.FileServingUtils
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray

internal fun io.ktor.server.routing.Route.addDirectory(
    route: Route,
    baseUri: Uri,
    allowBrowsing: Boolean,
    indexFile: String?,
    allowUpload: Boolean = false
) {
    route(route.path, {
        description = route.description
    }) {
        get("/{path...}") {
            val context = call.application.androidContext
            val relativePath = call.parameters.getAll("path")?.joinToString("/") ?: ""

            try {
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
                        if (indexFile != null) {
                            val indexFile = targetFile.findFile(indexFile)
                            if (indexFile != null && indexFile.exists() && indexFile.isFile) {
                                FileServingUtils.serveDocumentFile(call, indexFile, context)
                                return@get
                            }
                        }

                        if (allowBrowsing) {
                            val listing = DirectoryListingGenerator.generateFromDocumentFile(
                                targetFile,
                                route.path,
                                relativePath,
                                allowUpload
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

        if (allowUpload) {
            post("/{path...}") {
                val context = call.application.androidContext
                val relativePath = call.parameters.getAll("path")?.joinToString("/") ?: ""

                try {
                    val baseDocumentFile = DocumentFile.fromTreeUri(context, baseUri)
                    if (baseDocumentFile == null || !baseDocumentFile.exists()) {
                        call.respondText(
                            """{"success":false,"error":"Directory not found"}""",
                            ContentType.Application.Json,
                            HttpStatusCode.NotFound
                        )
                        return@post
                    }

                    val targetDir = if (relativePath.isEmpty()) {
                        baseDocumentFile
                    } else {
                        relativePath.split("/")
                            .fold(baseDocumentFile as DocumentFile?) { current, segment ->
                                current?.findFile(segment)
                            }
                    }

                    if (targetDir == null || !targetDir.exists() || !targetDir.isDirectory) {
                        call.respondText(
                            """{"success":false,"error":"Target directory not found"}""",
                            ContentType.Application.Json,
                            HttpStatusCode.NotFound
                        )
                        return@post
                    }

                    val uploadedFiles = mutableListOf<String>()
                    val multipart = call.receiveMultipart()

                    multipart.forEachPart { part ->
                        if (part is PartData.FileItem) {
                            val fileName = part.originalFileName ?: "upload"
                            val mimeType = part.contentType?.toString() ?: "application/octet-stream"
                            val newFile = targetDir.createFile(mimeType, fileName)

                            if (newFile != null) {
                                val bytes = part.provider().readRemaining().readByteArray()
                                context.contentResolver.openOutputStream(newFile.uri)?.use { output ->
                                    output.write(bytes)
                                }
                                uploadedFiles.add(fileName)
                            }
                        }
                        part.dispose()
                    }

                    val filesJson = uploadedFiles.joinToString(",") { "\"$it\"" }
                    call.respondText(
                        """{"success":true,"files":[$filesJson]}""",
                        ContentType.Application.Json
                    )
                } catch (e: Exception) {
                    call.respondText(
                        """{"success":false,"error":"Upload failed: ${e.message}"}""",
                        ContentType.Application.Json,
                        HttpStatusCode.InternalServerError
                    )
                }
            }
        }

        get {
            call.respondRedirect("${route.path}/", permanent = false)
        }
    }
}
