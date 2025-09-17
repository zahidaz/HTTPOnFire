package com.azzahid.hof.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.azzahid.hof.ui.screens.EndpointBuilderScreen
import com.azzahid.hof.ui.screens.HomeScreen
import com.azzahid.hof.ui.screens.LogsScreen
import com.azzahid.hof.ui.viewmodel.HomeViewModel
import com.azzahid.hof.ui.viewmodel.MainViewModel


sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Logs : Screen("logs")
    data object EndpointBuilder : Screen("endpoint_builder/{endpointId}") {
        fun createRoute(endpointId: String? = null): String {
            return if (endpointId != null) {
                "endpoint_builder/$endpointId"
            } else {
                "endpoint_builder/new"
            }
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = { slideEnterRight() },
            exitTransition = { slideExitLeft() },
            popEnterTransition = { slideEnterLeft() },
            popExitTransition = { slideExitRight() }
        ) {
            HomeScreen(
                homeViewModel = homeViewModel,
                onNavigateToEndpointBuilder = { endpointId ->
                    navController.navigate(Screen.EndpointBuilder.createRoute(endpointId))
                }
            )
        }
        composable(
            route = Screen.Logs.route,
            enterTransition = { slideEnterRight() },
            exitTransition = { slideExitLeft() },
            popEnterTransition = { slideEnterLeft() },
            popExitTransition = { slideExitRight() }
        ) {
            LogsScreen()
        }
        composable(
            route = Screen.EndpointBuilder.route,
            arguments = listOf(
                navArgument("endpointId") {
                    type = NavType.StringType
                    defaultValue = "new"
                }
            )
        ) { backStackEntry ->
            val endpointId = backStackEntry.arguments?.getString("endpointId")
            val isEditing = endpointId != null && endpointId != "new"
            EndpointBuilderScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEndpointCreated = { endpoint ->
                    homeViewModel.addEndpoint(endpoint)
                },
                endpointId = if (isEditing) endpointId else null,
                homeViewModel = homeViewModel
            )
        }
    }
}

private fun slideEnterRight(): EnterTransition = slideInHorizontally(
    initialOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(300)
)

private fun slideExitLeft(): ExitTransition = slideOutHorizontally(
    targetOffsetX = { fullWidth -> -fullWidth },
    animationSpec = tween(300)
)

private fun slideEnterLeft(): EnterTransition = slideInHorizontally(
    initialOffsetX = { fullWidth -> -fullWidth },
    animationSpec = tween(300)
)

private fun slideExitRight(): ExitTransition = slideOutHorizontally(
    targetOffsetX = { fullWidth -> fullWidth },
    animationSpec = tween(300)
)

