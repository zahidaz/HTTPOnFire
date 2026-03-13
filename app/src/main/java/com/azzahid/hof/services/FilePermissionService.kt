package com.azzahid.hof.services

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import com.azzahid.hof.domain.state.FileSelection

interface FilePermissionService {
    fun processSelectedFiles(uris: List<Uri>): List<FileSelection>
    fun processSelectedDirectory(uri: Uri): FileSelection
}

class AndroidFilePermissionService(
    private val contentResolver: ContentResolver
) : FilePermissionService {

    override fun processSelectedFiles(uris: List<Uri>): List<FileSelection> {
        return uris.map { uri ->
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { }

            FileSelection(
                uri = uri,
                displayName = resolveFileName(uri),
                isDirectory = false
            )
        }
    }

    override fun processSelectedDirectory(uri: Uri): FileSelection {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { }
        }

        return FileSelection(
            uri = uri,
            displayName = resolveDirectoryName(uri),
            isDirectory = true
        )
    }

    private fun resolveFileName(uri: Uri): String {
        try {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val name = cursor.getString(0)
                        if (!name.isNullOrBlank()) return sanitizeName(name)
                    }
                }
        } catch (_: Exception) { }

        val segment = uri.lastPathSegment ?: return "file"
        return sanitizeName(segment.substringAfterLast('/').substringAfterLast(':'))
    }

    private fun resolveDirectoryName(uri: Uri): String {
        val segment = uri.lastPathSegment ?: return "folder"
        val raw = segment.substringAfterLast(':').substringAfterLast('/')
        return if (raw.isNotBlank()) sanitizeName(raw) else "folder"
    }

    private fun sanitizeName(name: String): String {
        return name
            .replace(Regex("[^a-zA-Z0-9._\\s-]"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')
            .ifBlank { "file" }
    }
}
