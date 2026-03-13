package com.azzahid.hof.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azzahid.hof.R
import com.azzahid.hof.domain.state.SettingsUiState
import com.azzahid.hof.ui.providers.LocalViewModelFactory
import com.azzahid.hof.ui.viewmodel.HomeViewModel
import com.azzahid.hof.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabSettingsScreen() {
    val context = LocalContext.current
    val viewModelFactory = LocalViewModelFactory.current
    val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
    val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        settingsViewModel.onPermissionResult(isGranted)
    }

    val saveEnabled = !settingsUiState.isLoading &&
            (settingsUiState.defaultPort.toIntOrNull()?.let { it in 1..65535 } ?: false) &&
            (!settingsUiState.enableLogs ||
                    (settingsUiState.logRetentionDays.toIntOrNull()?.let { it in 1..365 } ?: false)) &&
            (!settingsUiState.enableLogs ||
                    (settingsUiState.maxLogEntries.toIntOrNull()?.let { it in 100..100000 } ?: false))

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.settings_title)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            actions = {
                FilledTonalButton(
                    onClick = settingsViewModel::saveSettings,
                    enabled = saveEnabled,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        if (settingsUiState.isLoading) stringResource(R.string.settings_saving)
                        else stringResource(R.string.action_save)
                    )
                }
            }
        )

        SettingsContent(
            settingsUiState = settingsUiState,
            onUpdateAutoStart = settingsViewModel::updateAutoStart,
            onUpdateBackgroundServiceEnabled = { enabled ->
                settingsViewModel.updateBackgroundServiceEnabled(
                    enabled,
                    context as? ComponentActivity,
                    notificationPermissionLauncher
                )
            },
            onUpdateEnableLogs = settingsViewModel::updateEnableLogs,
            onUpdateDefaultPort = homeViewModel::updateServerPort,
            onUpdateLogRetentionDays = settingsViewModel::updateLogRetentionDays,
            onUpdateMaxLogEntries = settingsViewModel::updateMaxLogEntries,
            onUpdateAutoCleanupEnabled = settingsViewModel::updateAutoCleanupEnabled,
            onUpdateCorsAllowAnyHost = settingsViewModel::updateCorsAllowAnyHost,
            onUpdateCorsAllowedHosts = settingsViewModel::updateCorsAllowedHosts,
            onUpdateCorsAllowCredentials = settingsViewModel::updateCorsAllowCredentials
        )
    }
}

@Composable
private fun SettingsContent(
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
    onUpdateCorsAllowCredentials: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ServerConfigCard(
            settingsUiState = settingsUiState,
            onUpdateAutoStart = onUpdateAutoStart,
            onUpdateBackgroundServiceEnabled = onUpdateBackgroundServiceEnabled,
            onUpdateEnableLogs = onUpdateEnableLogs,
            onUpdateDefaultPort = onUpdateDefaultPort
        )

        AnimatedVisibility(visible = settingsUiState.enableLogs) {
            LogManagementCard(
                settingsUiState = settingsUiState,
                onUpdateLogRetentionDays = onUpdateLogRetentionDays,
                onUpdateMaxLogEntries = onUpdateMaxLogEntries,
                onUpdateAutoCleanupEnabled = onUpdateAutoCleanupEnabled
            )
        }

        AdvancedSettingsCard(
            settingsUiState = settingsUiState,
            onUpdateCorsAllowAnyHost = onUpdateCorsAllowAnyHost,
            onUpdateCorsAllowedHosts = onUpdateCorsAllowedHosts,
            onUpdateCorsAllowCredentials = onUpdateCorsAllowCredentials
        )

        PermissionsCard()
    }
}

@Composable
private fun ServerConfigCard(
    settingsUiState: SettingsUiState,
    onUpdateAutoStart: (Boolean) -> Unit,
    onUpdateBackgroundServiceEnabled: (Boolean) -> Unit,
    onUpdateEnableLogs: (Boolean) -> Unit,
    onUpdateDefaultPort: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.settings_server_config),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = settingsUiState.defaultPort,
                onValueChange = { port ->
                    if (port.all { it.isDigit() } && port.length <= 5) {
                        val normalized = port.trimStart('0').ifEmpty { if (port.isNotEmpty()) "0" else "" }
                        onUpdateDefaultPort(normalized)
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

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_auto_start),
                    description = stringResource(R.string.settings_auto_start_desc),
                    checked = settingsUiState.autoStart,
                    onCheckedChange = onUpdateAutoStart
                )

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

                SettingsSwitchRow(
                    title = stringResource(R.string.settings_run_background),
                    description = stringResource(R.string.settings_run_background_desc),
                    checked = settingsUiState.backgroundServiceEnabled,
                    onCheckedChange = onUpdateBackgroundServiceEnabled
                )

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

                SettingsSwitchRow(
                    title = stringResource(R.string.settings_enable_logging),
                    description = stringResource(R.string.settings_enable_logging_desc),
                    checked = settingsUiState.enableLogs,
                    onCheckedChange = onUpdateEnableLogs
                )
            }
        }
    }
}

@Composable
private fun LogManagementCard(
    settingsUiState: SettingsUiState,
    onUpdateLogRetentionDays: (String) -> Unit,
    onUpdateMaxLogEntries: (String) -> Unit,
    onUpdateAutoCleanupEnabled: (Boolean) -> Unit
) {
    var showLogManagement by rememberSaveable { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogManagement = !showLogManagement }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_log_management),
                    style = MaterialTheme.typography.titleMedium
                )
                Icon(
                    imageVector = if (showLogManagement) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = showLogManagement) {
                Column {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
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

                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_auto_cleanup),
                        description = stringResource(R.string.settings_auto_cleanup_desc),
                        checked = settingsUiState.autoCleanupEnabled,
                        onCheckedChange = onUpdateAutoCleanupEnabled
                    )
                }
            }
        }
    }
}

@Composable
private fun AdvancedSettingsCard(
    settingsUiState: SettingsUiState,
    onUpdateCorsAllowAnyHost: (Boolean) -> Unit,
    onUpdateCorsAllowedHosts: (String) -> Unit,
    onUpdateCorsAllowCredentials: (Boolean) -> Unit
) {
    var showAdvanced by rememberSaveable { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
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
                    imageVector = if (showAdvanced) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = showAdvanced) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

                    Text(
                        text = stringResource(R.string.settings_cors_config),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_allow_any_host),
                        description = stringResource(R.string.settings_allow_any_host_desc),
                        checked = settingsUiState.corsAllowAnyHost,
                        onCheckedChange = onUpdateCorsAllowAnyHost,
                        titleStyle = MaterialTheme.typography.bodyMedium
                    )

                    if (!settingsUiState.corsAllowAnyHost) {
                        OutlinedTextField(
                            value = settingsUiState.corsAllowedHosts,
                            onValueChange = onUpdateCorsAllowedHosts,
                            label = { Text(stringResource(R.string.settings_allowed_hosts)) },
                            supportingText = { Text(stringResource(R.string.settings_allowed_hosts_help)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SettingsSwitchRow(
                        title = stringResource(R.string.settings_allow_credentials),
                        description = stringResource(R.string.settings_allow_credentials_desc),
                        checked = settingsUiState.corsAllowCredentials,
                        onCheckedChange = onUpdateCorsAllowCredentials,
                        titleStyle = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private data class PermissionItem(
    val permission: String,
    val titleRes: Int,
    val descriptionRes: Int
)

@Composable
private fun PermissionsCard() {
    val context = LocalContext.current
    var refreshKey by remember { mutableIntStateOf(0) }

    val permissions = remember {
        listOf(
            PermissionItem(
                Manifest.permission.ACCESS_FINE_LOCATION,
                R.string.settings_permission_location,
                R.string.settings_permission_location_desc
            ),
            PermissionItem(
                Manifest.permission.READ_CONTACTS,
                R.string.settings_permission_contacts,
                R.string.settings_permission_contacts_desc
            ),
            PermissionItem(
                Manifest.permission.CAMERA,
                R.string.settings_permission_camera,
                R.string.settings_permission_camera_desc
            ),
            PermissionItem(
                Manifest.permission.RECORD_AUDIO,
                R.string.settings_permission_microphone,
                R.string.settings_permission_microphone_desc
            )
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        refreshKey++
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.settings_permissions),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.settings_permissions_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            permissions.forEachIndexed { index, item ->
                val isGranted = remember(refreshKey) {
                    ContextCompat.checkSelfPermission(
                        context, item.permission
                    ) == PackageManager.PERMISSION_GRANTED
                }

                PermissionRow(
                    title = stringResource(item.titleRes),
                    description = stringResource(item.descriptionRes),
                    isGranted = isGranted,
                    onGrant = {
                        if (!isGranted) {
                            val activity = context as? ComponentActivity
                            val shouldShowRationale = activity?.shouldShowRequestPermissionRationale(item.permission) ?: false
                            if (shouldShowRationale) {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                                context.startActivity(intent)
                            } else {
                                permissionLauncher.launch(item.permission)
                            }
                        }
                    }
                )

                if (index < permissions.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    isGranted: Boolean,
    onGrant: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isGranted) stringResource(R.string.settings_permission_granted)
                else stringResource(R.string.settings_permission_not_granted),
                style = MaterialTheme.typography.labelSmall,
                color = if (isGranted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }

        if (!isGranted) {
            OutlinedButton(onClick = onGrant) {
                Text(stringResource(R.string.settings_permission_grant))
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    titleStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyLarge
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = titleStyle)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
