package com.azzahid.hof.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azzahid.hof.ui.screens.TabLogsScreen
import com.azzahid.hof.ui.screens.home.TabHomeScreen
import com.azzahid.hof.ui.screens.home.routes.RouteBuilderScreen
import com.azzahid.hof.ui.screens.settings.TabSettingsScreen
import kotlinx.serialization.Serializable


@Serializable
sealed class Screen {
    @Serializable
    data object Home : Screen()

    @Serializable
    data object Logs : Screen()

    @Serializable
    data object Settings : Screen()

    @Serializable
    data object RouteBuilder : Screen()
}

object AppAnimations {
    val slideEnterRight: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(500, easing = FastOutSlowInEasing)
    )

    val slideExitLeft: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(500, easing = FastOutSlowInEasing)
    )

    val slideEnterLeft: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(500, easing = FastOutSlowInEasing)
    )

    val slideExitRight: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(500, easing = FastOutSlowInEasing)
    )
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home,
        modifier = modifier
    ) {
        composable<Screen.Home>(
            enterTransition = { AppAnimations.slideEnterRight },
            exitTransition = { AppAnimations.slideExitLeft },
            popEnterTransition = { AppAnimations.slideEnterLeft },
            popExitTransition = { AppAnimations.slideExitRight }
        ) {
            TabHomeScreen(
                onNavigateToRouteBuilder = {
                    navController.navigate(Screen.RouteBuilder)
                }
            )
        }

        composable<Screen.Logs>(
            enterTransition = { AppAnimations.slideEnterRight },
            exitTransition = { AppAnimations.slideExitLeft },
            popEnterTransition = { AppAnimations.slideEnterLeft },
            popExitTransition = { AppAnimations.slideExitRight }
        ) {
            TabLogsScreen()
        }

        composable<Screen.Settings>(
            enterTransition = { AppAnimations.slideEnterRight },
            exitTransition = { AppAnimations.slideExitLeft },
            popEnterTransition = { AppAnimations.slideEnterLeft },
            popExitTransition = { AppAnimations.slideExitRight }
        ) {
            TabSettingsScreen()
        }

        composable<Screen.RouteBuilder>(
            enterTransition = { AppAnimations.slideEnterRight },
            exitTransition = { AppAnimations.slideExitLeft },
            popEnterTransition = { AppAnimations.slideEnterLeft },
            popExitTransition = { AppAnimations.slideExitRight }
        ) {
            RouteBuilderScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRouteCreated = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Home) { inclusive = false }
                    }
                }
            )
        }
    }
}
