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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.draw.clip
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
                .padding(16.dp),
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
                    Icon(Icons.Default.Search, contentDescription = null)
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
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = "Logging disabled",
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
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.HourglassEmpty,
                contentDescription = "No logs available",
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
        modifier = Modifier
            .fillMaxWidth()
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
                .padding(12.dp),
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
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.action_copy),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun MethodChip(method: String) {
    Surface(
        color = when (method.uppercase()) {
            "GET" -> Color(0xFF4CAF50)
            "POST" -> Color(0xFF2196F3)
            "PUT" -> Color(0xFFFF9800)
            "DELETE" -> Color(0xFFF44336)
            "PATCH" -> Color(0xFF9C27B0)
            else -> MaterialTheme.colorScheme.primary
        },
        shape = CircleShape,
        modifier = Modifier.clip(CircleShape)
    ) {
        Text(
            text = method,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun StatusChip(statusCode: Int?) {
    statusCode?.let { code ->
        val color = when (code) {
            in 200..299 -> Color(0xFF4CAF50)
            in 300..399 -> Color(0xFFFF9800)
            in 400..499 -> Color(0xFFF44336)
            in 500..599 -> Color(0xFF9C27B0)
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        }

        Surface(
            color = color.copy(alpha = 0.1f),
            shape = CircleShape
        ) {
            Text(
                text = code.toString(),
                color = color,
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
                    StatItem("Success", uiState.successCount.toString(), Color(0xFF4CAF50))
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