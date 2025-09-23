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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.azzahid.hof.R
import com.azzahid.hof.domain.state.SettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    showSettingsSheet: Boolean,
    settingsUiState: SettingsUiState,
    onUpdateAutoStart: (Boolean) -> Unit,
    onUpdateBackgroundServiceEnabled: (Boolean) -> Unit,
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
                onUpdateBackgroundServiceEnabled = onUpdateBackgroundServiceEnabled,
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
    onUpdateBackgroundServiceEnabled: (Boolean) -> Unit,
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
            text = stringResource(R.string.settings_title),
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
                    text = stringResource(R.string.settings_server_config),
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
                    label = { Text(stringResource(R.string.settings_default_port)) },
                    supportingText = { Text(stringResource(R.string.settings_default_port_help)) },
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
                                text = stringResource(R.string.settings_auto_start),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(R.string.settings_auto_start_desc),
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
                                text = stringResource(R.string.settings_run_background),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(R.string.settings_run_background_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settingsUiState.backgroundServiceEnabled,
                            onCheckedChange = onUpdateBackgroundServiceEnabled
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
                                text = stringResource(R.string.settings_enable_logging),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(R.string.settings_enable_logging_desc),
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

        AnimatedVisibility(visible = settingsUiState.enableLogs) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.settings_log_management),
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
                        label = { Text(stringResource(R.string.settings_log_retention)) },
                        supportingText = { Text(stringResource(R.string.settings_log_retention_help)) },
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
                        label = { Text(stringResource(R.string.settings_max_log_entries)) },
                        supportingText = { Text(stringResource(R.string.settings_max_log_entries_help)) },
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
                                text = stringResource(R.string.settings_auto_cleanup),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(R.string.settings_auto_cleanup_desc),
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
            var showAdvanced by rememberSaveable { mutableStateOf(false) }

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
                        text = stringResource(R.string.settings_advanced),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Icon(
                        imageVector = if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }

                AnimatedVisibility(visible = showAdvanced) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        HorizontalDivider()

                        Text(
                            text = stringResource(R.string.settings_cors_config),
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
                                    text = stringResource(R.string.settings_allow_any_host),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = stringResource(R.string.settings_allow_any_host_desc),
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
                                label = { Text(stringResource(R.string.settings_allowed_hosts)) },
                                supportingText = { Text(stringResource(R.string.settings_allowed_hosts_help)) },
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
                                    text = stringResource(R.string.settings_allow_credentials),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = stringResource(R.string.settings_allow_credentials_desc),
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
                Text(stringResource(R.string.settings_saving))
            } else {
                Text(stringResource(R.string.action_done))
            }
        }
    }
}