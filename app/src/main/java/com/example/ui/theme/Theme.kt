package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = EmeraldDeep,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = EmeraldContainerLight,

    secondary = WarmGoldBright,
    onSecondary = Color(0xFF2B1F00),
    secondaryContainer = GoldContainerDark,
    onSecondaryContainer = GoldContainerLight,

    tertiary = WarmGoldBright,
    onTertiary = EmeraldDeep,

    background = SurfaceDark,
    onBackground = OnSurfaceDark,

    surface = SurfaceDarkElevated,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = OnSurfaceDarkVariant,

    outline = OutlineDark,
    error = ErrorColor,
    errorContainer = ErrorContainerDark,
    onError = SurfaceDark,
    onErrorContainer = ErrorContainerLight
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = SurfaceLight,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = EmeraldDeep,

    secondary = WarmGold,
    onSecondary = SurfaceLight,
    secondaryContainer = GoldContainerLight,
    onSecondaryContainer = Color(0xFF3D2F08),

    tertiary = WarmGold,
    onTertiary = SurfaceLight,

    background = SurfaceLightDim,
    onBackground = OnSurfaceLight,

    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceLightVariant,
    onSurfaceVariant = OnSurfaceLightVariant,

    outline = OutlineLight,
    error = ErrorColor,
    errorContainer = ErrorContainerLight,
    onError = SurfaceLight,
    onErrorContainer = Color(0xFF410E0B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamicColor to ensure our custom professional theme always displays
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
