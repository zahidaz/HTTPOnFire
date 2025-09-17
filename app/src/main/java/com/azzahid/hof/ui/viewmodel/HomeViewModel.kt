package com.azzahid.hof.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.azzahid.hof.data.database.AppDatabase
import com.azzahid.hof.data.repository.EndpointRepository
import com.azzahid.hof.data.repository.SettingsRepository
import com.azzahid.hof.domain.model.Endpoint
import com.azzahid.hof.domain.state.HomeUiState
import com.azzahid.hof.domain.state.ServerStatus
import com.azzahid.hof.domain.state.SystemEndpoint
import com.azzahid.hof.features.http.getLocalIpAddress
import com.azzahid.hof.services.ServerServiceManager
import io.nayuki.qrcodegen.QrCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.net.NetworkInterface

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val endpointRepository = EndpointRepository(
        AppDatabase.getDatabase(application).endpointDao()
    )
    private val settingsRepository = SettingsRepository(application)
    private val serverServiceManager = ServerServiceManager(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadEndpoints()
        loadSystemEndpoints()
        serverServiceManager.bindToService()
        loadSettingsAndSetupAutoStart()

        viewModelScope.launch {
            serverServiceManager.isServerRunning.collect { isRunning ->
                updateServerState(isRunning)
            }
        }
    }

    private fun loadSettingsAndSetupAutoStart() {
        viewModelScope.launch {
            combine(
                settingsRepository.defaultPort,
                settingsRepository.autoStart
            ) { port, autoStart ->
                _uiState.value = _uiState.value.copy(serverPort = port)

                if (autoStart && !_uiState.value.isServerRunning) {
                    startServer()
                }
            }.collect { }
        }
    }

    private fun loadEndpoints() {
        viewModelScope.launch {
            endpointRepository.getAllEndpoints().collect { endpoints ->
                _uiState.value = _uiState.value.copy(endpoints = endpoints)
            }
        }
    }

    private fun loadSystemEndpoints() {
        viewModelScope.launch {
            settingsRepository.enableLogs.collect { enableLogs ->
                val systemEndpoints = buildList {
                    add(SystemEndpoint("/api/status", "Server status and health check"))
                    add(SystemEndpoint("/api/swagger", "Interactive API documentation"))
                    add(SystemEndpoint("/api/json", "OpenAPI specification"))
                }
                _uiState.value = _uiState.value.copy(systemEndpoints = systemEndpoints)
            }
        }
    }

    private fun updateServerState(isRunning: Boolean) {
        val serverUrl = if (isRunning) {
            try {
                "http://${getLocalIpAddress()}:${_uiState.value.serverPort}"
            } catch (e: Exception) {
                "http://localhost:${_uiState.value.serverPort}"
            }
        } else null

        val qrCodeBitmap = if (serverUrl != null) {
            generateQrCode(serverUrl)
        } else null

        val networkAddresses = if (isRunning) {
            getNetworkAddresses(_uiState.value.serverPort)
        } else emptyList()

        _uiState.value = _uiState.value.copy(
            serverStatus = if (isRunning) ServerStatus.RUNNING else ServerStatus.STOPPED,
            isServerRunning = isRunning,
            serverUrl = serverUrl,
            qrCodeBitmap = qrCodeBitmap,
            networkAddresses = networkAddresses
        )
    }


    fun addEndpoint(endpoint: Endpoint) {
        viewModelScope.launch {
            val existingEndpoint = endpointRepository.getEndpointById(endpoint.id)

            if (existingEndpoint != null) {
                endpointRepository.updateEndpoint(endpoint)
            } else {
                val currentEndpoints = _uiState.value.endpoints
                currentEndpoints.forEach { existing ->
                    endpointRepository.updateEndpointOrder(existing.id, existing.order + 1)
                }
                // Insert new endpoint at order 0
                endpointRepository.insertEndpoint(endpoint.copy(order = 0))
            }

            if (_uiState.value.isServerRunning) {
                restartServerWithNewConfiguration()
            }
        }
    }

    fun removeEndpoint(endpointId: String) {
        viewModelScope.launch {
            endpointRepository.deleteEndpointById(endpointId)

            if (_uiState.value.isServerRunning) {
                restartServerWithNewConfiguration()
            }
        }
    }

    fun toggleEndpoint(endpointId: String) {
        viewModelScope.launch {
            val endpoint = endpointRepository.getEndpointById(endpointId)
            endpoint?.let {
                endpointRepository.updateEndpointEnabled(endpointId, !it.isEnabled)

                if (_uiState.value.isServerRunning) {
                    restartServerWithNewConfiguration()
                }
            }
        }
    }

    fun startServer() {
        _uiState.value = _uiState.value.copy(
            serverStatus = ServerStatus.STARTING,
            error = null
        )
        serverServiceManager.startServer()
    }

    fun stopServer() {
        _uiState.value = _uiState.value.copy(
            serverStatus = ServerStatus.STOPPING,
            error = null
        )
        serverServiceManager.stopServer()
    }

    fun toggleServer() {
        if (_uiState.value.isServerRunning) {
            stopServer()
        } else {
            startServer()
        }
    }

    fun restartServerWithNewConfiguration() {
        serverServiceManager.restartServerWithNewConfiguration()
    }

    override fun onCleared() {
        super.onCleared()
        serverServiceManager.unbindFromService()
    }

    fun updateServerPort(port: String) {
        viewModelScope.launch {
            val wasRunning = _uiState.value.isServerRunning

            _uiState.value = _uiState.value.copy(serverPort = port)

            if (wasRunning) {
                restartServerWithNewConfiguration()
            }
        }
    }

    fun copyToClipboard(text: String) {
        val clipboardManager =
            getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("HTTP on Fire URL", text)
        clipboardManager.setPrimaryClip(clip)
    }

    private fun generateQrCode(text: String): ImageBitmap? {
        return try {
            val qr = QrCode.encodeText(text, QrCode.Ecc.MEDIUM)
            val size = qr.size
            val scale = 8
            val border = 4
            val totalSize = (size + border * 2) * scale

            val bitmap = createBitmap(totalSize, totalSize)

            for (y in 0 until totalSize) {
                for (x in 0 until totalSize) {
                    val moduleX = (x / scale) - border
                    val moduleY = (y / scale) - border

                    val isBlack = if (moduleX in 0 until size && moduleY in 0 until size) {
                        qr.getModule(moduleX, moduleY)
                    } else {
                        false
                    }

                    bitmap[x, y] =
                        if (isBlack) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                }
            }

            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    private fun getNetworkAddresses(port: String): List<Pair<String, String>> {
        return buildList {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                for (networkInterface in interfaces) {
                    if (!networkInterface.isLoopback && networkInterface.isUp) {
                        for (address in networkInterface.inetAddresses) {
                            if (!address.isLoopbackAddress && address.hostAddress?.contains(':') == false) {
                                val url = "http://${address.hostAddress}:$port"
                                val interfaceName = networkInterface.displayName
                                add(url to interfaceName)
                            }
                        }
                    }
                }
                add("http://localhost:$port" to "Localhost")
            } catch (e: Exception) {
                add("http://localhost:$port" to "Localhost")
            }
        }
    }
}