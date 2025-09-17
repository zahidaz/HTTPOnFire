package com.azzahid.hof.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.azzahid.hof.domain.state.SettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    showSettingsSheet: Boolean,
    settingsUiState: SettingsUiState,
    onUpdateAutoStart: (Boolean) -> Unit,
    onUpdateEnableLogs: (Boolean) -> Unit,
    onUpdateDefaultPort: (String) -> Unit,
    onUpdateLogRetentionDays: (String) -> Unit,
    onUpdateMaxLogEntries: (String) -> Unit,
    onUpdateAutoCleanupEnabled: (Boolean) -> Unit,
    onUpdateCorsAllowAnyHost: (Boolean) -> Unit,
    onUpdateCorsAllowedHosts: (String) -> Unit,
    onUpdateCorsAllowCredentials: (Boolean) -> Unit,
    onSaveSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showSettingsSheet) {
        val bottomSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = bottomSheetState
        ) {
            SettingsSheetContent(
                settingsUiState = settingsUiState,
                onUpdateAutoStart = onUpdateAutoStart,
                onUpdateEnableLogs = onUpdateEnableLogs,
                onUpdateDefaultPort = onUpdateDefaultPort,
                onUpdateLogRetentionDays = onUpdateLogRetentionDays,
                onUpdateMaxLogEntries = onUpdateMaxLogEntries,
                onUpdateAutoCleanupEnabled = onUpdateAutoCleanupEnabled,
                onUpdateCorsAllowAnyHost = onUpdateCorsAllowAnyHost,
                onUpdateCorsAllowedHosts = onUpdateCorsAllowedHosts,
                onUpdateCorsAllowCredentials = onUpdateCorsAllowCredentials,
                onSaveSettings = onSaveSettings,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun SettingsSheetContent(
    settingsUiState: SettingsUiState,
    onUpdateAutoStart: (Boolean) -> Unit,
    onUpdateEnableLogs: (Boolean) -> Unit,
    onUpdateDefaultPort: (String) -> Unit,
    onUpdateLogRetentionDays: (String) -> Unit,
    onUpdateMaxLogEntries: (String) -> Unit,
    onUpdateAutoCleanupEnabled: (Boolean) -> Unit,
    onUpdateCorsAllowAnyHost: (Boolean) -> Unit,
    onUpdateCorsAllowedHosts: (String) -> Unit,
    onUpdateCorsAllowCredentials: (Boolean) -> Unit,
    onSaveSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Server Configuration",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = settingsUiState.defaultPort,
                    onValueChange = { port ->
                        if (port.all { it.isDigit() } && port.length <= 5) {
                            onUpdateDefaultPort(port)
                        }
                    },
                    label = { Text("Default Port") },
                    supportingText = { Text("Port number (1-65535)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = settingsUiState.defaultPort.toIntOrNull()
                        ?.let { it < 1 || it > 65535 } ?: false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Auto-start server",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Automatically start server when app opens",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settingsUiState.autoStart,
                            onCheckedChange = onUpdateAutoStart
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Enable request logging",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Log all incoming HTTP requests for debugging",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settingsUiState.enableLogs,
                            onCheckedChange = onUpdateEnableLogs
                        )
                    }
                }
            }
        }

        if (settingsUiState.enableLogs) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Log Management",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = settingsUiState.logRetentionDays,
                        onValueChange = { days ->
                            if (days.all { it.isDigit() } && days.length <= 3) {
                                onUpdateLogRetentionDays(days)
                            }
                        },
                        label = { Text("Log Retention (Days)") },
                        supportingText = { Text("Keep logs for N days (1-365)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = settingsUiState.logRetentionDays.toIntOrNull()
                            ?.let { it < 1 || it > 365 } ?: false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = settingsUiState.maxLogEntries,
                        onValueChange = { entries ->
                            if (entries.all { it.isDigit() } && entries.length <= 6) {
                                onUpdateMaxLogEntries(entries)
                            }
                        },
                        label = { Text("Max Log Entries") },
                        supportingText = { Text("Maximum stored logs (100-100000)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = settingsUiState.maxLogEntries.toIntOrNull()
                            ?.let { it < 100 || it > 100000 } ?: false,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Auto cleanup",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Automatically remove old logs based on retention settings",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settingsUiState.autoCleanupEnabled,
                            onCheckedChange = onUpdateAutoCleanupEnabled
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            var showAdvanced by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdvanced = !showAdvanced }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Advanced",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        imageVector = if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (showAdvanced) "Collapse" else "Expand"
                    )
                }

                AnimatedVisibility(visible = showAdvanced) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HorizontalDivider()

                        Text(
                            text = "CORS Configuration",
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Allow any host",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Allows requests from any origin (less secure)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settingsUiState.corsAllowAnyHost,
                                onCheckedChange = onUpdateCorsAllowAnyHost
                            )
                        }

                        if (!settingsUiState.corsAllowAnyHost) {
                            OutlinedTextField(
                                value = settingsUiState.corsAllowedHosts,
                                onValueChange = onUpdateCorsAllowedHosts,
                                label = { Text("Allowed Hosts") },
                                supportingText = { Text("Comma-separated: example.com, app.com") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Allow credentials",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Allow cookies and authentication headers",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settingsUiState.corsAllowCredentials,
                                onCheckedChange = onUpdateCorsAllowCredentials
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                onSaveSettings()
                onDismiss()
            },
            enabled = !settingsUiState.isLoading &&
                    (settingsUiState.defaultPort.toIntOrNull()?.let { it in 1..65535 } ?: false) &&
                    (!settingsUiState.enableLogs ||
                            (settingsUiState.logRetentionDays.toIntOrNull()?.let { it in 1..365 }
                                ?: false)) &&
                    (!settingsUiState.enableLogs ||
                            (settingsUiState.maxLogEntries.toIntOrNull()?.let { it in 100..100000 }
                                ?: false)),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (settingsUiState.isLoading) {
                Text("Saving...")
            } else {
                Text("Done")
            }
        }
    }
}