package com.azzahid.hof.features.http.utils

import androidx.documentfile.provider.DocumentFile
import io.ktor.http.content.OutgoingContent
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully

fun DocumentFile.streamFileContent(
    context: android.content.Context,
    contentType: String
): OutgoingContent.WriteChannelContent? {
    val inputStream = context.contentResolver.openInputStream(this.uri)
    return inputStream?.let {
        object : OutgoingContent.WriteChannelContent() {
            override val contentType = io.ktor.http.ContentType.parse(contentType)
            override suspend fun writeTo(channel: ByteWriteChannel) {
                it.use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        channel.writeFully(buffer, 0, bytesRead)
                    }
                }
            }
        }
    }
}