package com.azzahid.hof.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Cyan,
    onPrimary = Obsidian,
    primaryContainer = CyanSubtle,
    onPrimaryContainer = Cyan,
    secondary = Violet,
    onSecondary = Obsidian,
    secondaryContainer = VioletSubtle,
    onSecondaryContainer = Violet,
    tertiary = Amber,
    onTertiary = Obsidian,
    tertiaryContainer = AmberSubtle,
    onTertiaryContainer = Amber,
    error = SoftRed,
    onError = PureWhite,
    errorContainer = SoftRedSubtle,
    onErrorContainer = SoftRed,
    background = Obsidian,
    onBackground = SoftWhite,
    surface = DarkGray,
    onSurface = SoftWhite,
    surfaceVariant = MidGray,
    onSurfaceVariant = MutedGray,
    outline = SubtleGray,
    outlineVariant = SubtleGray,
    surfaceContainerLowest = Obsidian,
    surfaceContainerLow = DarkGray,
    surfaceContainer = MidGray,
    surfaceContainerHigh = SubtleGray,
    surfaceContainerHighest = MutedGray
)

private val LightColorScheme = lightColorScheme(
    primary = CyanDark,
    onPrimary = PureWhite,
    primaryContainer = Cyan.copy(alpha = 0.12f),
    onPrimaryContainer = CyanDark,
    secondary = VioletDark,
    onSecondary = PureWhite,
    secondaryContainer = Violet.copy(alpha = 0.12f),
    onSecondaryContainer = VioletDark,
    tertiary = AmberDark,
    onTertiary = PureWhite,
    tertiaryContainer = Amber.copy(alpha = 0.12f),
    onTertiaryContainer = AmberDark,
    error = SoftRedDark,
    onError = PureWhite,
    errorContainer = SoftRed.copy(alpha = 0.12f),
    onErrorContainer = SoftRedDark,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutline,
    surfaceContainerLowest = LightSurface,
    surfaceContainerLow = LightBackground,
    surfaceContainer = LightSurfaceVariant,
    surfaceContainerHigh = LightOutline,
    surfaceContainerHighest = LightOnSurfaceVariant
)

@Composable
fun HTTPOnFireTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
