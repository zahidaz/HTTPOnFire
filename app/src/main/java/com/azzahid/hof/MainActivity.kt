package com.azzahid.hof

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.azzahid.hof.navigation.AppNavigation
import com.azzahid.hof.navigation.Screen
import com.azzahid.hof.ui.components.FirstLaunchDialog
import com.azzahid.hof.ui.components.MainBottomBar
import com.azzahid.hof.ui.components.PermissionRationaleDialog
import com.azzahid.hof.ui.providers.LocalViewModelFactory
import com.azzahid.hof.ui.theme.HTTPOnFireTheme
import com.azzahid.hof.ui.viewmodel.PermissionViewModel
import com.azzahid.hof.ui.viewmodel.factory.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModelFactory = remember { ViewModelFactory(application) }

            HTTPOnFireTheme {
                CompositionLocalProvider(LocalViewModelFactory provides viewModelFactory) {
                    AppWithNavigation()
                }
            }
        }
    }
}

@Composable
fun AppWithNavigation() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    val currentScreen = when (currentDestination?.route) {
        Screen.Home::class.qualifiedName -> Screen.Home
        Screen.Logs::class.qualifiedName -> Screen.Logs
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AppNavigation(
            navController = navController,
            modifier = Modifier.weight(1f)
        )

        if (currentScreen != null) {
            MainBottomBar(
                currentScreen = currentScreen,
                onNavigateToHome = {
                    navController.navigate(Screen.Home) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToLogs = {
                    navController.navigate(Screen.Logs) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }

    PermissionDialogs()
}

@Composable
private fun PermissionDialogs() {
    val context = LocalContext.current
    val viewModelFactory = LocalViewModelFactory.current
    val permissionViewModel: PermissionViewModel = viewModel(factory = viewModelFactory)

    val showFirstLaunchDialog by permissionViewModel.showFirstLaunchDialog.collectAsState()
    val showPermissionRationale by permissionViewModel.showPermissionRationale.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionViewModel.onPermissionResult(isGranted, context as ComponentActivity)
    }

    if (showFirstLaunchDialog) {
        FirstLaunchDialog(
            onDismiss = { permissionViewModel.onFirstLaunchDialogDismissed() },
            onEnableNotifications = {
                permissionViewModel.requestNotificationPermission(notificationPermissionLauncher)
                permissionViewModel.onFirstLaunchDialogDismissed()
            },
            onSkip = { permissionViewModel.onFirstLaunchDialogDismissed() }
        )
    }

    if (showPermissionRationale) {
        PermissionRationaleDialog(
            onDismiss = { permissionViewModel.dismissPermissionRationale() },
            onOpenSettings = { permissionViewModel.openAppSettings() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    HTTPOnFireTheme {
        AppWithNavigation()
    }
}