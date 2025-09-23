package com.azzahid.hof.services

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
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
                val displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "Selected file"
                FileSelection(
                    uri = uri,
                    displayName = displayName,
                    isDirectory = false
                )
            } catch (e: Exception) {
                val displayName = "Selected file"
                FileSelection(
                    uri = uri,
                    displayName = displayName,
                    isDirectory = false
                )
            }
        }
    }

    override fun processSelectedDirectory(uri: Uri): FileSelection {
        return try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "Selected folder"
            FileSelection(
                uri = uri,
                displayName = displayName,
                isDirectory = true
            )
        } catch (e: Exception) {
            val displayName = "Selected folder"
            FileSelection(
                uri = uri,
                displayName = displayName,
                isDirectory = true
            )
        }
    }
}