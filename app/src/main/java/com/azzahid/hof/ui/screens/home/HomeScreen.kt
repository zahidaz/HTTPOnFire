package com.azzahid.hof.ui.screens.home

import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azzahid.hof.R
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import com.azzahid.hof.domain.state.HomeUiState
import com.azzahid.hof.ui.components.PermissionRationaleDialog
import com.azzahid.hof.ui.components.RouteCard
import com.azzahid.hof.ui.components.RouteDetailsDialog
import com.azzahid.hof.ui.components.SettingsBottomSheet
import com.azzahid.hof.ui.components.ShareDialog
import com.azzahid.hof.ui.components.appbars.HomeAppBar
import com.azzahid.hof.ui.providers.LocalViewModelFactory
import com.azzahid.hof.ui.viewmodel.HomeViewModel
import com.azzahid.hof.ui.viewmodel.SettingsViewModel


@Composable
fun TabHomeScreen(
    onNavigateToRouteBuilder: (String?) -> Unit
) {
    val context = LocalContext.current
    val viewModelFactory = LocalViewModelFactory.current
    val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = viewModelFactory)
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val showPermissionRationale by settingsViewModel.showPermissionRationale.collectAsState()
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var selectedRouteForDetails by remember { mutableStateOf<Route?>(null) }
    var selectedRouteForShare by remember { mutableStateOf<Route?>(null) }

    val serverUrlPairs = remember(homeUiState.networkAddresses) {
        if (homeUiState.isServerRunning) homeUiState.networkAddresses else emptyList()
    }
    var selectedServerQrUrl by remember(serverUrlPairs) {
        mutableStateOf(serverUrlPairs.firstOrNull()?.first)
    }

    val routeUrlPairs = remember(homeUiState.networkAddresses, selectedRouteForShare?.path) {
        selectedRouteForShare?.let { route ->
            if (homeUiState.isServerRunning) {
                homeUiState.networkAddresses.map { (url, interfaceName) ->
                    val cleanPath =
                        if (route.path.startsWith("/")) route.path else "/${route.path}"
                    "$url$cleanPath" to interfaceName
                }
            } else emptyList()
        } ?: emptyList()
    }
    var selectedRouteQrUrl by remember(routeUrlPairs) {
        mutableStateOf(routeUrlPairs.firstOrNull()?.first)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        settingsViewModel.onPermissionResult(isGranted)
        if (isGranted && homeUiState.isServerRunning) {
            homeViewModel.restartServerWithNewConfiguration()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HomeAppBar(
            serverStatus = homeUiState.serverStatus,
            isServerRunning = homeUiState.isServerRunning,
            serverPort = homeUiState.serverPort.toIntOrNull() ?: 8080,
            onToggleServer = homeViewModel::toggleServer,
            onShareClick = { showShareDialog = true },
            onSettingsClick = { showSettingsSheet = true }
        )

        HomeScreenContent(
            modifier = Modifier.weight(1f),
            homeUiState = homeUiState,
            onToggleRoute = homeViewModel::toggleRoute,
            onRemoveRoute = homeViewModel::removeRoute,
            onNavigateToRouteBuilder = onNavigateToRouteBuilder,
            onShowRouteDetails = { route -> selectedRouteForDetails = route },
            onShareRoute = { route -> selectedRouteForShare = route }
        )
    }

    SettingsBottomSheet(
        showSettingsSheet = showSettingsSheet,
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

    if (showPermissionRationale) {
        PermissionRationaleDialog(
            onDismiss = { settingsViewModel.dismissPermissionRationale() },
            onOpenSettings = { settingsViewModel.openAppSettings() }
        )
    }

    if (showShareDialog) {
        selectedServerQrUrl?.let { url ->
            homeViewModel.generateServerQr(url)
        }

        ShareDialog(
            urlPairs = serverUrlPairs,
            selectedQrUrl = selectedServerQrUrl,
            qrBitmap = homeUiState.serverQrBitmap,
            onQrUrlSelected = { url ->
                selectedServerQrUrl = url
                homeViewModel.generateServerQr(url)
            },
            onCopyToClipboard = homeViewModel::copyToClipboard,
            onDismiss = { showShareDialog = false }
        )
    }

    selectedRouteForDetails?.let { route ->
        RouteDetailsDialog(
            route = route,
            onDismiss = { selectedRouteForDetails = null }
        )
    }

    selectedRouteForShare?.let { route ->
        selectedRouteQrUrl?.let { url ->
            homeViewModel.generateRouteQr(url)
        }

        ShareDialog(
            urlPairs = routeUrlPairs,
            selectedQrUrl = selectedRouteQrUrl,
            qrBitmap = homeUiState.routeQrBitmap,
            onQrUrlSelected = { url ->
                selectedRouteQrUrl = url
                homeViewModel.generateRouteQr(url)
            },
            onCopyToClipboard = homeViewModel::copyToClipboard,
            onDismiss = { selectedRouteForShare = null },
            route = route
        )
    }
}

@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    homeUiState: HomeUiState,
    onToggleRoute: (Route) -> Unit,
    onRemoveRoute: (String) -> Unit,
    onNavigateToRouteBuilder: (String?) -> Unit,
    onShowRouteDetails: (Route) -> Unit,
    onShareRoute: (Route) -> Unit,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs =
        listOf(stringResource(R.string.home_tab_user), stringResource(R.string.home_tab_system))

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> UserRoutesList(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    routes = homeUiState.routes.filter { it.type !is RouteType.BuiltInRoute },
                    onToggleRoute = onToggleRoute,
                    onRemoveRoute = onRemoveRoute,
                    onShowRouteDetails = onShowRouteDetails,
                    onShareRoute = onShareRoute,
                )

                1 -> BuiltInRoutesList(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    routes = homeUiState.routes.filter { it.type is RouteType.BuiltInRoute },
                    onShareRoute = onShareRoute,
                    onToggleRoute = onToggleRoute,
                )
            }
        }

        if (selectedTabIndex == 0) {
            FloatingActionButton(
                onClick = { onNavigateToRouteBuilder(null) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.cd_add_route)
                )
            }
        }
    }
}


@Composable
private fun BuiltInRoutesList(
    modifier: Modifier = Modifier,
    routes: List<Route>,
    onShareRoute: (Route) -> Unit,
    onToggleRoute: (Route) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        if (routes.isNotEmpty()) {
            items(
                count = routes.size,
                key = { index -> routes[index].id }
            ) { index ->
                val route = routes[index]
                RouteCard(
                    route = route,
                    onToggle = { onToggleRoute(route) },
                    onRemove = { },
                    onShare = { onShareRoute(route) },
                    onDetails = { }
                )
            }
        }
    }
}

@Composable
private fun UserRoutesList(
    modifier: Modifier = Modifier,
    routes: List<Route>,
    onRemoveRoute: (String) -> Unit,
    onShowRouteDetails: (Route) -> Unit,
    onShareRoute: (Route) -> Unit,
    onToggleRoute: (Route) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        if (routes.isNotEmpty()) {
            items(
                count = routes.size,
                key = { index -> routes.sortedBy { it.order }[index].id }
            ) { index ->
                val route = routes.sortedBy { it.order }[index]
                RouteCard(
                    route = route,
                    onToggle = { onToggleRoute(route) },
                    onRemove = { onRemoveRoute(route.id) },
                    onShare = { onShareRoute(route) },
                    onDetails = { onShowRouteDetails(route) }
                )
            }
        } else {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Route,
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .padding(bottom = 16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.home_empty_title),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.home_empty_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
