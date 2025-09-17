package com.azzahid.hof.domain.state

data class SettingsUiState(
    val autoStart: Boolean = false,
    val enableLogs: Boolean = true,
    val defaultPort: String = "8080",
    val logRetentionDays: String = "7",
    val maxLogEntries: String = "1000",
    val autoCleanupEnabled: Boolean = true,
    val corsAllowAnyHost: Boolean = false,
    val corsAllowedHosts: String = "",
    val corsAllowCredentials: Boolean = false,
    val isLoading: Boolean = false
)