package com.azzahid.hof.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.azzahid.hof.data.repository.NetworkRepository
import com.azzahid.hof.data.repository.RouteRepository
import com.azzahid.hof.data.repository.SettingsRepository
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import com.azzahid.hof.domain.state.HomeUiState
import com.azzahid.hof.domain.state.ServerStatus
import com.azzahid.hof.features.http.ServerConfigurationService
import com.azzahid.hof.services.ClipboardService
import com.azzahid.hof.services.QRCodeService
import com.azzahid.hof.services.ServerServiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(
    private val routeRepository: RouteRepository,
    private val settingsRepository: SettingsRepository,
    private val serverServiceManager: ServerServiceManager,
    private val clipboardService: ClipboardService,
    private val qrCodeService: QRCodeService,
    private val networkRepository: NetworkRepository,
    private val serverConfigurationService: ServerConfigurationService,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRoutes()
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

    private fun loadRoutes() {
        viewModelScope.launch {
            serverConfigurationService.getServerConfiguration().collect { config ->
                val userRoutes = routeRepository.getUserRoutes().first()
                val builtInRoutes = routeRepository.getAllBuiltInRoutes(config)
                val allRoutes = userRoutes + builtInRoutes

                val previousRoutes = _uiState.value.routes
                _uiState.value = _uiState.value.copy(routes = allRoutes)

                if (hasRoutesChanged(
                        previousRoutes,
                        allRoutes
                    ) && _uiState.value.isServerRunning
                ) {
                    restartServerWithNewConfiguration()
                }
            }
        }
    }

    private fun updateServerState(isRunning: Boolean) {
        val serverUrl = if (isRunning) {
            try {
                "http://${networkRepository.getLocalIpAddress()}:${_uiState.value.serverPort}"
            } catch (_: Exception) {
                "http://localhost:${_uiState.value.serverPort}"
            }
        } else null

        val networkAddresses = if (isRunning) {
            networkRepository.getNetworkAddresses(_uiState.value.serverPort)
        } else emptyList()

        _uiState.value = _uiState.value.copy(
            serverStatus = if (isRunning) ServerStatus.RUNNING else ServerStatus.STOPPED,
            isServerRunning = isRunning,
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
        if (route.type is RouteType.BuiltInRoute) {
            toggleBuiltInRoute(route)
        } else {
            toggleUserRoute(route)
        }
    }

    private fun toggleUserRoute(route: Route) {
        viewModelScope.launch {
            val route = routeRepository.getRouteById(route.id)
            route?.let {
                routeRepository.updateRouteEnabled(route.id, !it.isEnabled)
            }
        }
    }

    private fun toggleBuiltInRoute(route: Route) {
        viewModelScope.launch {
            when (route.type) {
                is RouteType.SwaggerRoute -> {
                    settingsRepository.updateEnableSwagger(!route.isEnabled)
                }

                is RouteType.OpenApiRoute -> {
                    settingsRepository.updateEnableOpenApi(!route.isEnabled)
                }

                is RouteType.StatusRoute -> {
                    settingsRepository.updateEnableStatus(!route.isEnabled)
                }

                else -> {}
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
        clipboardService.copyToClipboard(text, "HTTP on Fire URL")
    }

    fun generateServerQr(url: String) {
        val qrBitmap = qrCodeService.generateQrCode(url)
        _uiState.value = _uiState.value.copy(serverQrBitmap = qrBitmap)
    }

    fun generateRouteQr(url: String) {
        val qrBitmap = qrCodeService.generateQrCode(url)
        _uiState.value = _uiState.value.copy(routeQrBitmap = qrBitmap)
    }

    private fun hasRoutesChanged(previous: List<Route>, current: List<Route>): Boolean {
        if (previous.isEmpty()) return false

        if (previous.size != current.size) return true

        val previousIds = previous.map { "${it.id}-${it.isEnabled}" }.toSet()
        val currentIds = current.map { "${it.id}-${it.isEnabled}" }.toSet()

        return previousIds != currentIds
    }

}