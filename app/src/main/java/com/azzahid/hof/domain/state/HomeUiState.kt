package com.azzahid.hof.domain.state

import androidx.compose.ui.graphics.ImageBitmap
import com.azzahid.hof.domain.model.Endpoint

data class HomeUiState(
    val serverStatus: ServerStatus = ServerStatus.STOPPED,
    val serverPort: String = "8080",
    val serverUrl: String? = null,
    val isServerRunning: Boolean = false,
    val connectionCount: Int = 0,
    val endpoints: List<Endpoint> = emptyList(),
    val systemEndpoints: List<SystemEndpoint> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val qrCodeBitmap: ImageBitmap? = null,
    val networkAddresses: List<Pair<String, String>> = emptyList()
)

data class SystemEndpoint(
    val path: String,
    val description: String,
    val isEnabled: Boolean = true
)

enum class ServerStatus {
    STOPPED,
    STARTING,
    RUNNING,
    STOPPING,
    ERROR
}