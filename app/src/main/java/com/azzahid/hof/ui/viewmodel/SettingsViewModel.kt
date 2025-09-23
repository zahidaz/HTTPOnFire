package com.azzahid.hof.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.azzahid.hof.data.repository.SettingsRepository
import com.azzahid.hof.domain.state.SettingsUiState
import com.azzahid.hof.services.PermissionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val permissionService: PermissionService,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _showPermissionRationale = MutableStateFlow(false)
    val showPermissionRationale: StateFlow<Boolean> = _showPermissionRationale.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.autoStart,
                settingsRepository.backgroundServiceEnabled,
                settingsRepository.enableLogs,
                settingsRepository.defaultPort,
                settingsRepository.logRetentionDays,
                settingsRepository.maxLogEntries
            ) { values: Array<Any> ->
                val autoStart = values[0] as Boolean
                val backgroundServiceEnabled = values[1] as Boolean
                val enableLogs = values[2] as Boolean
                val defaultPort = values[3] as String
                val logRetentionDays = values[4] as String
                val maxLogEntries = values[5] as String

                _uiState.value = _uiState.value.copy(
                    autoStart = autoStart,
                    backgroundServiceEnabled = backgroundServiceEnabled,
                    enableLogs = enableLogs,
                    defaultPort = defaultPort,
                    logRetentionDays = logRetentionDays,
                    maxLogEntries = maxLogEntries,
                    isLoading = false
                )
            }.collect { }
        }

        viewModelScope.launch {
            combine(
                settingsRepository.autoCleanupEnabled,
                settingsRepository.corsAllowAnyHost,
                settingsRepository.corsAllowedHosts,
                settingsRepository.corsAllowCredentials
            ) { autoCleanupEnabled, corsAllowAnyHost, corsAllowedHosts, corsAllowCredentials ->
                _uiState.value = _uiState.value.copy(
                    autoCleanupEnabled = autoCleanupEnabled,
                    corsAllowAnyHost = corsAllowAnyHost,
                    corsAllowedHosts = corsAllowedHosts,
                    corsAllowCredentials = corsAllowCredentials,
                    isLoading = false
                )
            }.collect { }
        }
    }

    fun updateAutoStart(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            settingsRepository.updateAutoStart(enabled)
            _uiState.value = _uiState.value.copy(autoStart = enabled, isLoading = false)
        }
    }

    fun updateBackgroundServiceEnabled(
        enabled: Boolean,
        activity: Activity?,
        launcher: ActivityResultLauncher<String>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            if (enabled) {
                permissionService.handleNotificationPermissionRequest(
                    activity = activity,
                    launcher = launcher,
                    onGranted = {
                        viewModelScope.launch {
                            settingsRepository.updateBackgroundServiceEnabled(true)
                            _uiState.value = _uiState.value.copy(
                                backgroundServiceEnabled = true,
                                isLoading = false
                            )
                        }
                    },
                    onShowRationale = {
                        _showPermissionRationale.value = true
                        _uiState.value = _uiState.value.copy(isLoading = false)
                    }
                )
            } else {
                settingsRepository.updateBackgroundServiceEnabled(false)
                _uiState.value =
                    _uiState.value.copy(backgroundServiceEnabled = false, isLoading = false)
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        viewModelScope.launch {
            if (isGranted) {
                settingsRepository.updateBackgroundServiceEnabled(true)
                _uiState.value = _uiState.value.copy(backgroundServiceEnabled = true)
            }
        }
    }

    fun openAppSettings() {
        permissionService.openAppSettings()
        _showPermissionRationale.value = false
    }

    fun dismissPermissionRationale() {
        _showPermissionRationale.value = false
    }

    fun updateEnableLogs(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            settingsRepository.updateEnableLogs(enabled)
            _uiState.value = _uiState.value.copy(enableLogs = enabled, isLoading = false)
        }
    }

    fun updateDefaultPort(port: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            settingsRepository.updateDefaultPort(port)
            _uiState.value = _uiState.value.copy(defaultPort = port, isLoading = false)
        }
    }

    fun updateLogRetentionDays(days: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            settingsRepository.updateLogRetentionDays(days)
            _uiState.value = _uiState.value.copy(logRetentionDays = days, isLoading = false)
        }
    }

    fun updateMaxLogEntries(maxEntries: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            settingsRepository.updateMaxLogEntries(maxEntries)
            _uiState.value = _uiState.value.copy(maxLogEntries = maxEntries, isLoading = false)
        }
    }

    fun updateAutoCleanupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            settingsRepository.updateAutoCleanupEnabled(enabled)
            _uiState.value = _uiState.value.copy(autoCleanupEnabled = enabled, isLoading = false)
        }
    }

    fun updateCorsAllowAnyHost(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            settingsRepository.updateCorsAllowAnyHost(enabled)
            _uiState.value = _uiState.value.copy(corsAllowAnyHost = enabled, isLoading = false)
        }
    }

    fun updateCorsAllowedHosts(hosts: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            settingsRepository.updateCorsAllowedHosts(hosts)
            _uiState.value = _uiState.value.copy(corsAllowedHosts = hosts, isLoading = false)
        }
    }

    fun updateCorsAllowCredentials(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            settingsRepository.updateCorsAllowCredentials(enabled)
            _uiState.value = _uiState.value.copy(corsAllowCredentials = enabled, isLoading = false)
        }
    }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}