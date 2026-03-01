package com.azzahid.hof.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azzahid.hof.R
import com.azzahid.hof.navigation.Screen

private data class TabItem(
    val titleRes: Int,
    val icon: ImageVector,
    val screen: Screen
)

@Composable
fun MainBottomBar(
    currentScreen: Screen,
    onNavigateToHome: () -> Unit,
    onNavigateToLogs: () -> Unit
) {
    val tabs = listOf(
        TabItem(R.string.nav_home, Icons.Outlined.Home, Screen.Home),
        TabItem(R.string.nav_logs, Icons.AutoMirrored.Outlined.Assignment, Screen.Logs)
    )

    Column {
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outline
        )

        NavigationBar(
            tonalElevation = 0.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabs.forEach { tab ->
                NavigationBarItem(
                    selected = currentScreen::class == tab.screen::class,
                    onClick = {
                        when (tab.screen) {
                            Screen.Home -> onNavigateToHome()
                            Screen.Logs -> onNavigateToLogs()
                            else -> {}
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = stringResource(tab.titleRes)
                        )
                    },
                    label = { Text(stringResource(tab.titleRes)) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
