package com.azzahid.hof.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WarmAmber,
    secondary = SoftOrange,
    tertiary = CozyBrown,
    background = WarmBlack,
    surface = DarkSurface,
    onPrimary = WarmBlack,
    onSecondary = WarmBlack,
    onTertiary = CreamWhite,
    onBackground = WarmAmberLight,
    onSurface = CreamWhite,
    surfaceVariant = CozyBrownLight,
    onSurfaceVariant = CozyBrownDeep,
    primaryContainer = WarmAmberDeep,
    onPrimaryContainer = WarmAmberLight,
    secondaryContainer = SoftOrangeDeep,
    onSecondaryContainer = SoftOrangeLight,
    tertiaryContainer = CozyBrownDeep,
    onTertiaryContainer = CozyBrownLight,
    error = SoftOrange,
    onError = CreamWhite
)

private val LightColorScheme = lightColorScheme(
    primary = WarmAmberDeep,
    secondary = SoftOrangeDeep,
    tertiary = CozyBrownDeep,
    background = WarmSurface,
    surface = CreamWhite,
    onPrimary = CreamWhite,
    onSecondary = CreamWhite,
    onTertiary = CreamWhite,
    onBackground = WarmBlack,
    onSurface = WarmBlack,
    surfaceVariant = WarmAmberLight,
    onSurfaceVariant = WarmAmberDeep,
    primaryContainer = WarmAmberLight,
    onPrimaryContainer = WarmAmberDeep,
    secondaryContainer = SoftOrangeLight,
    onSecondaryContainer = SoftOrangeDeep,
    tertiaryContainer = CozyBrownLight,
    onTertiaryContainer = CozyBrownDeep,
    error = SoftOrangeDeep,
    onError = CreamWhite
)

@Composable
fun HTTPOnFireTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}