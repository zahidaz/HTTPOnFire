package com.azzahid.hof.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.azzahid.hof.data.repository.HttpRequestLogRepository
import com.azzahid.hof.data.repository.SettingsRepository
import com.azzahid.hof.domain.model.HttpRequestLog
import com.azzahid.hof.domain.state.LogsUiState
import com.azzahid.hof.services.ClipboardService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class LogsViewModel(
    private val logRepository: HttpRequestLogRepository,
    private val settingsRepository: SettingsRepository,
    private val clipboardService: ClipboardService,
    application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    init {
        loadLogs()
        observeAutoCleanup()
    }

    private fun loadLogs() {
        viewModelScope.launch {
            combine(
                logRepository.getRecentLogs(1000),
                settingsRepository.enableLogs
            ) { logs, enableLogs ->
                val currentState = _uiState.value
                val filteredLogs =
                    filterLogs(logs, currentState.filterMethod, currentState.searchQuery)
                val errorCount = filteredLogs.count { (it.statusCode ?: 0) >= 400 }
                val successCount = filteredLogs.count { (it.statusCode ?: 0) in 200..299 }

                _uiState.value = currentState.copy(
                    logs = filteredLogs,
                    totalEntries = filteredLogs.size,
                    errorCount = errorCount,
                    successCount = successCount,
                    enableLogs = enableLogs,
                    isLoading = false
                )
            }.collect {}
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            logRepository.clearAllLogs()
            _uiState.value = _uiState.value.copy(
                logs = emptyList(),
                totalEntries = 0,
                errorCount = 0,
                successCount = 0,
                isLoading = false
            )
        }
    }

    fun copyLogToClipboard(log: HttpRequestLog) {
        clipboardService.copyToClipboard(log.getFormattedDetails(), "HTTP Request Log")
    }

    fun copyAllLogsToClipboard() {
        val allLogsText = _uiState.value.logs.joinToString("\n\n") { it.getFormattedDetails() }
        clipboardService.copyToClipboard(allLogsText, "All HTTP Request Logs")
    }

    fun setSelectedLog(log: HttpRequestLog?) {
        _uiState.value = _uiState.value.copy(selectedLog = log)
    }

    fun setMethodFilter(method: String?) {
        _uiState.value = _uiState.value.copy(filterMethod = method)
        loadLogs()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadLogs()
    }

    fun cleanupOldLogs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            logRepository.cleanupOldLogs(maxLogs = 500)
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun filterLogs(
        logs: List<HttpRequestLog>,
        methodFilter: String?,
        searchQuery: String
    ): List<HttpRequestLog> {
        var filtered = logs

        methodFilter?.let { method ->
            filtered = filtered.filter { it.method.equals(method, ignoreCase = true) }
        }

        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { log ->
                log.path.contains(searchQuery, ignoreCase = true) ||
                        log.clientIp.contains(searchQuery, ignoreCase = true) ||
                        log.userAgent?.contains(searchQuery, ignoreCase = true) == true ||
                        log.statusCode?.toString()?.contains(searchQuery) == true
            }
        }

        return filtered
    }

    private fun observeAutoCleanup() {
        viewModelScope.launch {
            combine(
                settingsRepository.autoCleanupEnabled,
                settingsRepository.logRetentionDays,
                settingsRepository.maxLogEntries
            ) { autoCleanupEnabled, retentionDays, maxEntries ->
                if (autoCleanupEnabled) {
                    performAutoCleanup(
                        retentionDays.toIntOrNull() ?: 7,
                        maxEntries.toIntOrNull() ?: 1000
                    )
                }
            }.collect { }
        }
    }

    private suspend fun performAutoCleanup(retentionDays: Int, maxEntries: Int) {
        val cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
        logRepository.cleanupOldLogsByDate(cutoffTime)
        logRepository.cleanupOldLogs(maxEntries)
    }
}