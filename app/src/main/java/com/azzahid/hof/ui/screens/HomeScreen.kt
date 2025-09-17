package com.azzahid.hof.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azzahid.hof.domain.model.Endpoint
import com.azzahid.hof.domain.state.HomeUiState
import com.azzahid.hof.ui.components.EndpointCard
import com.azzahid.hof.ui.components.SettingsBottomSheet
import com.azzahid.hof.ui.viewmodel.HomeViewModel
import com.azzahid.hof.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    onNavigateToEndpointBuilder: (String?) -> Unit
) {
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            CustomAppBar(
                homeUiState = homeUiState,
                onToggleServer = homeViewModel::toggleServer,
                onShowSettings = { showSettingsSheet = true },
                onShowShare = { showShareDialog = true }
            )

            HomeScreenContent(
                modifier = Modifier.fillMaxSize(),
                homeUiState = homeUiState,
                onToggleEndpoint = homeViewModel::toggleEndpoint,
                onRemoveEndpoint = homeViewModel::removeEndpoint,
                onNavigateToEndpointBuilder = onNavigateToEndpointBuilder,
                onCopyToClipboard = homeViewModel::copyToClipboard
            )
        }

        FloatingActionButton(
            onClick = { onNavigateToEndpointBuilder(null) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Endpoint"
            )
        }
    }

    SettingsBottomSheet(
        showSettingsSheet = showSettingsSheet,
        settingsUiState = settingsUiState,
        onUpdateAutoStart = settingsViewModel::updateAutoStart,
        onUpdateEnableLogs = settingsViewModel::updateEnableLogs,
        onUpdateDefaultPort = { port ->
            settingsViewModel.updateDefaultPort(port)
            homeViewModel.updateServerPort(port)
        },
        onUpdateLogRetentionDays = settingsViewModel::updateLogRetentionDays,
        onUpdateMaxLogEntries = settingsViewModel::updateMaxLogEntries,
        onUpdateAutoCleanupEnabled = settingsViewModel::updateAutoCleanupEnabled,
        onUpdateCorsAllowAnyHost = settingsViewModel::updateCorsAllowAnyHost,
        onUpdateCorsAllowedHosts = settingsViewModel::updateCorsAllowedHosts,
        onUpdateCorsAllowCredentials = settingsViewModel::updateCorsAllowCredentials,
        onSaveSettings = settingsViewModel::saveSettings,
        onDismiss = { showSettingsSheet = false }
    )

    if (showShareDialog) {
        ShareDialog(
            homeUiState = homeUiState,
            onDismiss = { showShareDialog = false },
            onCopyToClipboard = homeViewModel::copyToClipboard
        )
    }
}

@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    onToggleEndpoint: (String) -> Unit,
    onRemoveEndpoint: (String) -> Unit,
    onNavigateToEndpointBuilder: (String?) -> Unit,
    onCopyToClipboard: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EndpointList(
            endpoints = homeUiState.endpoints,
            systemEndpoints = homeUiState.systemEndpoints,
            serverUrl = homeUiState.serverUrl,
            onToggleEndpoint = onToggleEndpoint,
            onRemoveEndpoint = onRemoveEndpoint,
            onNavigateToEndpointBuilder = onNavigateToEndpointBuilder,
            onCopyToClipboard = onCopyToClipboard
        )
    }
}

@Composable
private fun EndpointList(
    endpoints: List<Endpoint>,
    systemEndpoints: List<com.azzahid.hof.domain.state.SystemEndpoint>,
    serverUrl: String?,
    onToggleEndpoint: (String) -> Unit,
    onRemoveEndpoint: (String) -> Unit,
    onNavigateToEndpointBuilder: (String?) -> Unit,
    onCopyToClipboard: (String) -> Unit
) {
    var showSystemEndpoints by remember { mutableStateOf(false) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSystemEndpoints = !showSystemEndpoints }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "System Endpoints",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Icon(
                            imageVector = if (showSystemEndpoints) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (showSystemEndpoints) "Collapse" else "Expand"
                        )
                    }

                    AnimatedVisibility(visible = showSystemEndpoints) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (serverUrl != null) {
                                systemEndpoints.forEach { systemEndpoint ->
                                    SystemEndpointCard(
                                        systemEndpoint = systemEndpoint,
                                        serverUrl = serverUrl,
                                        onCopyUrl = onCopyToClipboard
                                    )
                                }
                            } else {
                                Text(
                                    text = "Start the server to access system endpoints",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (endpoints.isNotEmpty()) {
            item {
                Text(
                    text = "User Endpoints",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            items(
                count = endpoints.size,
                key = { index -> endpoints.sortedBy { it.order }[index].id }
            ) { index ->
                val endpoint = endpoints.sortedBy { it.order }[index]
                EndpointCard(
                    endpoint = endpoint,
                    onToggle = { onToggleEndpoint(endpoint.id) },
                    onRemove = { onRemoveEndpoint(endpoint.id) },
                    onEdit = { onNavigateToEndpointBuilder(endpoint.id) }
                )
            }
        } else {
            item {
                EmptyState()
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No Endpoints Created",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create your first endpoint using the + button",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
private fun ServerStatus(
    serverStatus: com.azzahid.hof.domain.state.ServerStatus,
    isServerRunning: Boolean,
    serverPort: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(
                    when (serverStatus) {
                        com.azzahid.hof.domain.state.ServerStatus.RUNNING -> Color(0xFF4CAF50)
                        com.azzahid.hof.domain.state.ServerStatus.ERROR -> Color.Red
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
        )

        Column {
            Text(
                text = when (serverStatus) {
                    com.azzahid.hof.domain.state.ServerStatus.RUNNING -> "Running"
                    com.azzahid.hof.domain.state.ServerStatus.ERROR -> "Error"
                    else -> "Stopped"
                },
                style = MaterialTheme.typography.titleMedium
            )
            if (isServerRunning) {
                Text(
                    text = "localhost:$serverPort",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AppBarActions(
    isServerRunning: Boolean,
    onToggleServer: () -> Unit,
    onShowSettings: () -> Unit,
    onShowShare: () -> Unit
) {
    Row {
        if (isServerRunning) {
            IconButton(onClick = onShowShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Server"
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = onToggleServer) {
            Icon(
                imageVector = if (isServerRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = if (isServerRunning) "Stop Server" else "Start Server"
            )
        }

        IconButton(onClick = onShowSettings) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings"
            )
        }
    }
}

@Composable
private fun CustomAppBar(
    homeUiState: HomeUiState,
    onToggleServer: () -> Unit,
    onShowSettings: () -> Unit,
    onShowShare: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ServerStatus(
                serverStatus = homeUiState.serverStatus,
                isServerRunning = homeUiState.isServerRunning,
                serverPort = homeUiState.serverPort
            )
            AppBarActions(
                isServerRunning = homeUiState.isServerRunning,
                onToggleServer = onToggleServer,
                onShowSettings = onShowSettings,
                onShowShare = onShowShare
            )
        }
    }
}

@Composable
private fun ShareDialog(
    homeUiState: HomeUiState,
    onDismiss: () -> Unit,
    onCopyToClipboard: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Share Server Access")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (homeUiState.isServerRunning && homeUiState.serverUrl != null) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (homeUiState.qrCodeBitmap != null) {
                                    Image(
                                        bitmap = homeUiState.qrCodeBitmap,
                                        contentDescription = "QR Code for ${homeUiState.serverUrl}",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp)
                                    )
                                } else {
                                    Text(
                                        "QR Code",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Scan to connect or copy URLs below",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    homeUiState.networkAddresses.forEach { address ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = address.first,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = address.second,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onCopyToClipboard(address.first) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy URL",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        "Server is not running. Start the server to share access URLs.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}




