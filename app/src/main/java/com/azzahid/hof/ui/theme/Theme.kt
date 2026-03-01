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
    onSurfaceVariant = SecondaryText,
    outline = OutlineGray,
    outlineVariant = SubtleGray,
    surfaceContainerLowest = Obsidian,
    surfaceContainerLow = DarkGray,
    surfaceContainer = MidGray,
    surfaceContainerHigh = SubtleGray,
    surfaceContainerHighest = CardGray
)

private val LightColorScheme = lightColorScheme(
    primary = CyanDark,
    onPrimary = PureWhite,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = CyanDark,
    secondary = VioletDark,
    onSecondary = PureWhite,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = VioletDark,
    tertiary = AmberDark,
    onTertiary = PureWhite,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = AmberDark,
    error = SoftRedDark,
    onError = PureWhite,
    errorContainer = LightErrorContainer,
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
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest
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
