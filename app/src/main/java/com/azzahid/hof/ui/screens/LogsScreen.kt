package com.azzahid.hof.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azzahid.hof.R
import com.azzahid.hof.domain.model.HttpRequestLog
import com.azzahid.hof.domain.state.LogsUiState
import com.azzahid.hof.ui.components.appbars.LogsAppBar
import com.azzahid.hof.ui.providers.LocalViewModelFactory
import com.azzahid.hof.ui.viewmodel.LogsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabLogsScreen() {
    val viewModelFactory = LocalViewModelFactory.current
    val logsViewModel: LogsViewModel = viewModel(factory = viewModelFactory)
    val uiState by logsViewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf(uiState.searchQuery) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showStatsDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        LogsAppBar(
            totalEntries = uiState.totalEntries,
            errorCount = uiState.errorCount,
            hasLogs = uiState.logs.isNotEmpty(),
            onStatsClick = { showStatsDialog = true },
            onFilterClick = { showFilterMenu = true },
            onCopyAllClick = { logsViewModel.copyAllLogsToClipboard() },
            onClearClick = { showClearDialog = true }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    logsViewModel.setSearchQuery(it)
                },
                label = { Text(stringResource(R.string.logs_search_placeholder)) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (!uiState.enableLogs) {
                LogsDisabledCard()
            } else if (uiState.logs.isEmpty()) {
                EmptyLogsCard()
            } else {
                LogsListCard(
                    logs = uiState.logs,
                    onCopyLog = logsViewModel::copyLogToClipboard,
                    onLogClick = logsViewModel::setSelectedLog
                )
            }
        }
    }

    if (showFilterMenu) {
        FilterMenuDropdown(
            onDismiss = { showFilterMenu = false },
            onMethodSelected = { method ->
                logsViewModel.setMethodFilter(method)
                showFilterMenu = false
            }
        )
    }

    if (showClearDialog) {
        ClearLogsDialog(
            onConfirm = {
                logsViewModel.clearLogs()
                showClearDialog = false
            },
            onDismiss = { showClearDialog = false }
        )
    }

    if (showStatsDialog) {
        StatisticsDialog(
            uiState = uiState,
            onDismiss = { showStatsDialog = false }
        )
    }

    uiState.selectedLog?.let { log ->
        LogDetailDialog(
            log = log,
            onDismiss = { logsViewModel.setSelectedLog(null) },
            onCopy = { logsViewModel.copyLogToClipboard(log) }
        )
    }
}


@Composable
private fun LogsDisabledCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(40.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.VisibilityOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.logs_disabled_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.logs_disabled_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyLogsCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(40.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.HourglassEmpty,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.logs_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


@Composable
private fun LogsListCard(
    logs: List<HttpRequestLog>,
    onCopyLog: (HttpRequestLog) -> Unit,
    onLogClick: (HttpRequestLog) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyColumn(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(logs) { log ->
                LogItemCard(
                    log = log,
                    onCopy = { onCopyLog(log) },
                    onClick = { onLogClick(log) }
                )
            }
        }
    }
}

@Composable
private fun LogItemCard(
    log: HttpRequestLog,
    onCopy: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MethodChip(log.method)
                    StatusChip(log.statusCode)
                    Text(
                        text = log.getFormattedTimestamp(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = log.path,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${log.clientIp} ${log.responseTimeMs?.let { "• ${it}ms" } ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = stringResource(R.string.action_copy),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun MethodChip(method: String) {
    val (containerColor, contentColor) = when (method.uppercase()) {
        "GET" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "POST" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        "PUT" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "DELETE" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        "PATCH" -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = method,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun StatusChip(statusCode: Int?) {
    statusCode?.let { code ->
        val (containerColor, contentColor) = when (code) {
            in 200..299 -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
            in 300..399 -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
            in 400..499 -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
            in 500..599 -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        }

        Surface(
            color = containerColor,
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = code.toString(),
                color = contentColor,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
            )
        }
    }
}


@Composable
private fun FilterMenuDropdown(
    onDismiss: () -> Unit,
    onMethodSelected: (String?) -> Unit
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.logs_filter_all_methods)) },
            onClick = { onMethodSelected(null) }
        )
        listOf("GET", "POST", "PUT", "DELETE", "PATCH").forEach { method ->
            DropdownMenuItem(
                text = { Text(method) },
                onClick = { onMethodSelected(method) }
            )
        }
    }
}

@Composable
private fun StatItem(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.headlineSmall,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ClearLogsDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.logs_clear_dialog_title)) },
        text = { Text(stringResource(R.string.logs_clear_dialog_message)) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.action_clear))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun StatisticsDialog(
    uiState: LogsUiState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.logs_statistics_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        "Total",
                        uiState.totalEntries.toString(),
                        MaterialTheme.colorScheme.primary
                    )
                    StatItem(
                        "Success",
                        uiState.successCount.toString(),
                        MaterialTheme.colorScheme.primary
                    )
                    StatItem(
                        "Errors",
                        uiState.errorCount.toString(),
                        MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
private fun LogDetailDialog(
    log: HttpRequestLog,
    onDismiss: () -> Unit,
    onCopy: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.logs_details_title)) },
        text = {
            LazyColumn {
                item {
                    Text(
                        text = log.getFormattedDetails(),
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onCopy) {
                Text(stringResource(R.string.action_copy))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}
