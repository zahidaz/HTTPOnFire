package com.azzahid.hof.ui.screens.home

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azzahid.hof.Constants
import com.azzahid.hof.R
import com.azzahid.hof.domain.model.Route
import com.azzahid.hof.domain.model.RouteType
import com.azzahid.hof.domain.state.HomeUiState
import com.azzahid.hof.domain.state.ServerStatus
import com.azzahid.hof.ui.components.RouteCard
import com.azzahid.hof.ui.components.RouteDetailsDialog
import com.azzahid.hof.ui.components.ShareDialog
import com.azzahid.hof.ui.components.appbars.HomeAppBar
import com.azzahid.hof.ui.providers.LocalViewModelFactory
import com.azzahid.hof.ui.viewmodel.HomeViewModel


@Composable
fun TabHomeScreen(
    onNavigateToRouteBuilder: (String?) -> Unit
) {
    val viewModelFactory = LocalViewModelFactory.current
    val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    var showShareDialog by remember { mutableStateOf(false) }
    var selectedRouteForDetails by remember { mutableStateOf<Route?>(null) }
    var selectedRouteForShare by remember { mutableStateOf<Route?>(null) }

    val serverUrlPairs = remember(homeUiState.networkAddresses, homeUiState.serverStatus) {
        if (homeUiState.serverStatus == ServerStatus.STARTED) homeUiState.networkAddresses else emptyList()
    }
    var selectedServerQrUrl by remember(serverUrlPairs) {
        mutableStateOf(serverUrlPairs.firstOrNull()?.first)
    }

    val routeUrlPairs = remember(
        homeUiState.networkAddresses,
        homeUiState.serverStatus,
        selectedRouteForShare?.path
    ) {
        val route = selectedRouteForShare
        if (homeUiState.serverStatus != ServerStatus.STARTED || route == null) {
            emptyList()
        } else {
            val cleanPath = if (route.path.startsWith("/")) route.path else "/${route.path}"
            homeUiState.networkAddresses.map { (url, interfaceName) ->
                "$url$cleanPath" to interfaceName
            }
        }
    }
    var selectedRouteQrUrl by remember(routeUrlPairs) {
        mutableStateOf(routeUrlPairs.firstOrNull()?.first)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    var previousServerStatus by remember { mutableStateOf(homeUiState.serverStatus) }
    val serverPort = homeUiState.serverPort.toIntOrNull() ?: Constants.DEFAULT_PORT
    val startedMessage = stringResource(R.string.server_snackbar_started, serverPort)
    val errorMessage = stringResource(R.string.server_snackbar_error)

    LaunchedEffect(homeUiState.serverStatus) {
        val current = homeUiState.serverStatus
        if (current != previousServerStatus) {
            when (current) {
                ServerStatus.STARTED -> snackbarHostState.showSnackbar(startedMessage)
                ServerStatus.ERROR -> snackbarHostState.showSnackbar(errorMessage)
                else -> {}
            }
            previousServerStatus = current
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HomeAppBar(
                serverStatus = homeUiState.serverStatus,
                serverPort = serverPort,
                onToggleServer = homeViewModel::toggleServer,
                onShareClick = { showShareDialog = true }
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
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
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.background,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            height = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                divider = {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    routes = homeUiState.routes.filter { it.type !is RouteType.BuiltInRoute },
                    onToggleRoute = onToggleRoute,
                    onRemoveRoute = onRemoveRoute,
                    onShowRouteDetails = onShowRouteDetails,
                    onShareRoute = onShareRoute,
                    onNavigateToRouteBuilder = onNavigateToRouteBuilder,
                )

                1 -> BuiltInRoutesList(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
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
                    imageVector = Icons.Outlined.Add,
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
    onNavigateToRouteBuilder: (String?) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
                                .size(48.dp)
                                .padding(bottom = 16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.home_empty_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.home_empty_subtitle),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { onNavigateToRouteBuilder(null) }) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.padding(start = 8.dp))
                            Text(stringResource(R.string.home_empty_button))
                        }
                    }
                }
            }
        }
    }
}
