package com.azzahid.hof.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.azzahid.hof.data.repository.SettingsRepository
import com.azzahid.hof.domain.state.SettingsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            combine(
                settingsRepository.autoStart,
                settingsRepository.enableLogs,
                settingsRepository.defaultPort,
                settingsRepository.logRetentionDays,
                settingsRepository.maxLogEntries
            ) { autoStart, enableLogs, defaultPort, logRetentionDays, maxLogEntries ->
                _uiState.value = _uiState.value.copy(
                    autoStart = autoStart,
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