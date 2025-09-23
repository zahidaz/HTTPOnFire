package com.azzahid.hof.domain.state

import com.azzahid.hof.domain.model.HttpRequestLog

data class LogsUiState(
    val logs: List<HttpRequestLog> = emptyList(),
    val isLoading: Boolean = false,
    val totalEntries: Int = 0,
    val errorCount: Int = 0,
    val successCount: Int = 0,
    val selectedLog: HttpRequestLog? = null,
    val filterMethod: String? = null,
    val searchQuery: String = "",
    val enableLogs: Boolean = true
)