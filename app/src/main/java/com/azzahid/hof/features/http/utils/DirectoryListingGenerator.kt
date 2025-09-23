package com.azzahid.hof.features.http.utils

import androidx.documentfile.provider.DocumentFile

object DirectoryListingGenerator {
    fun generate(
        directory: java.io.File,
        basePath: String,
        relativePath: String
    ): String {
        val files = directory.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name }))
            ?: emptyList()
        val currentPath = if (relativePath.isEmpty()) basePath else "$basePath/$relativePath"

        return buildString {
            append("<!DOCTYPE html><html><head><title>Directory listing for $currentPath</title>")
            append("<style>body{font-family:monospace;margin:40px;}a{text-decoration:none;color:#0066cc;}a:hover{text-decoration:underline;}.dir{font-weight:bold;}</style>")
            append("</head><body>")
            append("<h1>Directory listing for $currentPath</h1><hr>")

            if (relativePath.isNotEmpty()) {
                val parentPath = relativePath.substringBeforeLast("/", "")
                val parentUrl = if (parentPath.isEmpty()) basePath else "$basePath/$parentPath"
                append("<a href=\"$parentUrl/\">[Parent Directory]</a><br><br>")
            }

            files.forEach { file ->
                val fileName = file.name
                val fileUrl =
                    if (relativePath.isEmpty()) "$basePath/$fileName" else "$basePath/$relativePath/$fileName"
                val displayName = if (file.isDirectory) "$fileName/" else fileName
                val cssClass = if (file.isDirectory) "dir" else ""

                append("<a href=\"$fileUrl\" class=\"$cssClass\">$displayName</a>")
                if (file.isFile) {
                    append(" (${file.length()} bytes)")
                }
                append("<br>")
            }

            append("<hr></body></html>")
        }
    }

    fun generateFromDocumentFile(
        directory: DocumentFile,
        basePath: String,
        relativePath: String
    ): String {
        val files = directory.listFiles().sortedWith(compareBy({ !it.isDirectory }, { it.name }))
        val currentPath = if (relativePath.isEmpty()) basePath else "$basePath/$relativePath"

        return buildString {
            append("<!DOCTYPE html><html><head><title>Directory listing for $currentPath</title>")
            append("<style>body{font-family:monospace;margin:40px;}a{text-decoration:none;color:#0066cc;}a:hover{text-decoration:underline;}.dir{font-weight:bold;}</style>")
            append("</head><body>")
            append("<h1>Directory listing for $currentPath</h1><hr>")

            if (relativePath.isNotEmpty()) {
                val parentPath = relativePath.substringBeforeLast("/", "")
                val parentUrl = if (parentPath.isEmpty()) basePath else "$basePath/$parentPath"
                append("<a href=\"$parentUrl/\">[Parent Directory]</a><br><br>")
            }

            files.forEach { file ->
                val fileName = file.name ?: "unknown"
                val fileUrl =
                    if (relativePath.isEmpty()) "$basePath/$fileName" else "$basePath/$relativePath/$fileName"
                val displayName = if (file.isDirectory) "$fileName/" else fileName
                val cssClass = if (file.isDirectory) "dir" else ""

                append("<a href=\"$fileUrl\" class=\"$cssClass\">$displayName</a>")
                if (file.isFile) {
                    append(" (${file.length()} bytes)")
                }
                append("<br>")
            }

            append("<hr></body></html>")
        }
    }
}