package com.azzahid.hof.domain.state

import androidx.compose.ui.graphics.ImageBitmap
import com.azzahid.hof.Constants
import com.azzahid.hof.domain.model.Route

data class HomeUiState(
    val serverStatus: ServerStatus = ServerStatus.STOPPED,
    val serverPort: String = Constants.DEFAULT_PORT_STRING,
    val serverUrl: String? = null,
    val isServerRunning: Boolean = false,
    val connectionCount: Int = 0,
    val routes: List<Route> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val serverQrBitmap: ImageBitmap? = null,
    val routeQrBitmap: ImageBitmap? = null,
    val networkAddresses: List<Pair<String, String>> = emptyList()
)


enum class ServerStatus {
    STOPPED,
    STARTING,
    RUNNING,
    STOPPING,
    ERROR
}