package com.azzahid.hof.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.azzahid.hof.data.repository.NetworkRepository
import com.azzahid.hof.data.repository.RouteRepository
import com.azzahid.hof.data.repository.SettingsRepository
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.state.HomeUiState
import com.azzahid.hof.domain.state.ServerStatus
import com.azzahid.hof.services.ClipboardService
import com.azzahid.hof.services.QRCodeService
import com.azzahid.hof.services.ServerServiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class HomeViewModel(
    private val routeRepository: RouteRepository,
    private val settingsRepository: SettingsRepository,
    private val serverServiceManager: ServerServiceManager,
    private val clipboardService: ClipboardService,
    private val qrCodeService: QRCodeService,
    private val networkRepository: NetworkRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRoutes()
        serverServiceManager.bindToService()
        loadSettingsAndSetupAutoStart()
        viewModelScope.launch {
            serverServiceManager.serverStatus.collect { status ->
                updateServerState(status)
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

                if (autoStart && _uiState.value.serverStatus == ServerStatus.STOPPED) {
                    startServer()
                }
            }.collect { }
        }
    }

    private fun loadRoutes() {
        viewModelScope.launch {
            routeRepository.getAllRoutes().collect { allRoutes ->
                val previousRoutes = _uiState.value.routes
                _uiState.value = _uiState.value.copy(routes = allRoutes)

                if (hasRoutesChanged(
                        previousRoutes,
                        allRoutes
                    ) && _uiState.value.serverStatus == ServerStatus.STARTED
                ) {
                    restartServerWithNewConfiguration()
                }
            }
        }
    }

    private fun updateServerState(status: ServerStatus) {
        val currentState = _uiState.value
        val wasStarted = currentState.serverStatus == ServerStatus.STARTED
        val isStarted = status == ServerStatus.STARTED

        val serverUrl = when {
            isStarted -> try {
                "http://${networkRepository.getLocalIpAddress()}:${currentState.serverPort}"
            } catch (_: Exception) {
                "http://localhost:${currentState.serverPort}"
            }

            !isStarted -> null
            else -> currentState.serverUrl
        }

        val networkAddresses = if (isStarted && !wasStarted) {
            networkRepository.getNetworkAddresses(currentState.serverPort)
        } else if (!isStarted) {
            emptyList()
        } else {
            currentState.networkAddresses
        }

        _uiState.value = currentState.copy(
            serverStatus = status,
            serverUrl = serverUrl,
            networkAddresses = networkAddresses
        )
    }

    fun removeRoute(routeId: String) {
        viewModelScope.launch {
            routeRepository.deleteRouteById(routeId)
        }
    }

    fun toggleRoute(route: Route) {
        viewModelScope.launch {
            routeRepository.toggleRoute(route)
        }
    }


    private fun startServer() {
        serverServiceManager.startServer()
    }

    private fun stopServer() {
        serverServiceManager.stopServer()
    }

    fun toggleServer() {
        serverServiceManager.toggleServer()
    }

    fun restartServerWithNewConfiguration() {
        serverServiceManager.restartServer()
    }

    override fun onCleared() {
        super.onCleared()
        serverServiceManager.unbindFromService()
    }

    fun updateServerPort(port: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(serverPort = port)
            settingsRepository.updateDefaultPort(port)

            if (_uiState.value.serverStatus == ServerStatus.STARTED) {
                restartServerWithNewConfiguration()
            }
        }
    }

    fun copyToClipboard(text: String) {
        clipboardService.copyToClipboard(text, "HTTP on Fire URL")
    }

    fun generateServerQr(url: String) {
        try {
            val qrBitmap = qrCodeService.generateQrCode(url)
            _uiState.value = _uiState.value.copy(serverQrBitmap = qrBitmap)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Failed to generate server QR code", e)
        }
    }

    fun generateRouteQr(url: String) {
        try {
            val qrBitmap = qrCodeService.generateQrCode(url)
            _uiState.value = _uiState.value.copy(routeQrBitmap = qrBitmap)
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Failed to generate route QR code", e)
        }
    }

    private fun hasRoutesChanged(previous: List<Route>, current: List<Route>): Boolean {
        if (previous.isEmpty()) return false

        if (previous.size != current.size) return true

        val previousIds = previous.map { "${it.id}-${it.isEnabled}" }.toSet()
        val currentIds = current.map { "${it.id}-${it.isEnabled}" }.toSet()

        return previousIds != currentIds
    }

}