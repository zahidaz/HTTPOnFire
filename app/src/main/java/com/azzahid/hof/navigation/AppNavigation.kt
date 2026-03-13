package com.azzahid.hof.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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

private const val TAB_ANIM_DURATION = 200
private const val PUSH_ANIM_DURATION = 350

object AppAnimations {
    val tabEnter: EnterTransition = fadeIn(animationSpec = tween(TAB_ANIM_DURATION))
    val tabExit: ExitTransition = fadeOut(animationSpec = tween(TAB_ANIM_DURATION))

    val pushEnter: EnterTransition = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(PUSH_ANIM_DURATION, easing = FastOutSlowInEasing)
    )
    val pushExit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { -it / 3 },
        animationSpec = tween(PUSH_ANIM_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(PUSH_ANIM_DURATION))

    val popEnter: EnterTransition = slideInHorizontally(
        initialOffsetX = { -it / 3 },
        animationSpec = tween(PUSH_ANIM_DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(PUSH_ANIM_DURATION))
    val popExit: ExitTransition = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(PUSH_ANIM_DURATION, easing = FastOutSlowInEasing)
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
            enterTransition = {
                if (initialState.destination.route == Screen.RouteBuilder::class.qualifiedName)
                    AppAnimations.popEnter else AppAnimations.tabEnter
            },
            exitTransition = {
                if (targetState.destination.route == Screen.RouteBuilder::class.qualifiedName)
                    AppAnimations.pushExit else AppAnimations.tabExit
            },
            popEnterTransition = {
                if (initialState.destination.route == Screen.RouteBuilder::class.qualifiedName)
                    AppAnimations.popEnter else AppAnimations.tabEnter
            },
            popExitTransition = { AppAnimations.tabExit }
        ) {
            TabHomeScreen(
                onNavigateToRouteBuilder = {
                    navController.navigate(Screen.RouteBuilder)
                }
            )
        }

        composable<Screen.Logs>(
            enterTransition = { AppAnimations.tabEnter },
            exitTransition = { AppAnimations.tabExit },
            popEnterTransition = { AppAnimations.tabEnter },
            popExitTransition = { AppAnimations.tabExit }
        ) {
            TabLogsScreen()
        }

        composable<Screen.Settings>(
            enterTransition = { AppAnimations.tabEnter },
            exitTransition = { AppAnimations.tabExit },
            popEnterTransition = { AppAnimations.tabEnter },
            popExitTransition = { AppAnimations.tabExit }
        ) {
            TabSettingsScreen()
        }

        composable<Screen.RouteBuilder>(
            enterTransition = { AppAnimations.pushEnter },
            exitTransition = { AppAnimations.pushExit },
            popEnterTransition = { AppAnimations.popEnter },
            popExitTransition = { AppAnimations.popExit }
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
