package com.azzahid.hof.features.http.utils

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import io.ktor.http.CacheControl
import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

object FileServingUtils {
    suspend fun serveDocumentFile(
        call: ApplicationCall,
        documentFile: DocumentFile,
        context: Context,
        forceDownload: Boolean = false
    ) {
        val contentType =
            context.contentResolver.getType(documentFile.uri) ?: "application/octet-stream"
        val fileName = documentFile.name ?: "download"

        call.response.headers.apply {
            append(HttpHeaders.CacheControl, CacheControl.NoCache(null).toString())
            append(HttpHeaders.ContentType, contentType)

            if (forceDownload) {
                append(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(
                        ContentDisposition.Parameters.FileName, fileName
                    ).toString()
                )
            }
        }

        documentFile.streamFileContent(context, contentType)?.let { content ->
            call.respond(content)
        } ?: run {
            call.respond(HttpStatusCode.InternalServerError, "Unable to open input stream for file")
        }
    }
}