package com.azzahid.hof.services

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

interface ClipboardService {
    fun copyToClipboard(text: String, label: String = "HTTP on Fire")
}

class AndroidClipboardService(private val application: Application) : ClipboardService {
    override fun copyToClipboard(text: String, label: String) {
        val clipboardManager =
            application.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboardManager?.setPrimaryClip(clip)
    }
}