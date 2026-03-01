package com.azzahid.hof.features.http.utils

import androidx.documentfile.provider.DocumentFile

object DirectoryListingGenerator {
    fun generate(
        directory: java.io.File,
        basePath: String,
        relativePath: String,
        allowUpload: Boolean = false
    ): String {
        val files = directory.listFiles()
            ?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
            ?: emptyList()
        val currentPath = if (relativePath.isEmpty()) basePath else "$basePath/$relativePath"

        val entries = files.map { file ->
            FileExplorerHtmlBuilder.FileEntry(
                name = file.name,
                isDirectory = file.isDirectory,
                size = if (file.isFile) file.length() else 0L,
                lastModified = file.lastModified()
            )
        }

        return FileExplorerHtmlBuilder.build(entries, currentPath, basePath, relativePath, allowUpload)
    }

    fun generateFromDocumentFile(
        directory: DocumentFile,
        basePath: String,
        relativePath: String,
        allowUpload: Boolean = false
    ): String {
        val files = directory.listFiles().sortedWith(compareBy({ !it.isDirectory }, { it.name }))
        val currentPath = if (relativePath.isEmpty()) basePath else "$basePath/$relativePath"

        val entries = files.map { file ->
            FileExplorerHtmlBuilder.FileEntry(
                name = file.name ?: "unknown",
                isDirectory = file.isDirectory,
                size = if (file.isFile) file.length() else 0L,
                lastModified = file.lastModified()
            )
        }

        return FileExplorerHtmlBuilder.build(entries, currentPath, basePath, relativePath, allowUpload)
    }
}
