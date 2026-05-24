package com.expertconnect.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * ExpertConnect Material 3 dark color scheme.
 * Designed for premium, modern aesthetics with purple/teal accent.
 */
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryPurple,
    onPrimary = TextPrimary,
    primaryContainer = PrimaryPurpleDark,
    onPrimaryContainer = PrimaryPurpleLight,

    secondary = SecondaryTeal,
    onSecondary = BackgroundDark,
    secondaryContainer = SecondaryTealDark,
    onSecondaryContainer = SecondaryTealLight,

    tertiary = AccentAmber,
    onTertiary = BackgroundDark,

    background = BackgroundDark,
    onBackground = TextPrimary,

    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,

    outline = TextTertiary,
    error = StatusCancelled,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = TextPrimary,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = PrimaryPurpleDark,

    secondary = SecondaryTealDark,
    onSecondary = TextPrimary,

    background = BackgroundLight,
    onBackground = TextPrimaryLight,

    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,

    error = StatusCancelled,
)

/**
 * Main app theme composable.
 * Always uses dark mode for the premium aesthetic.
 */
@Composable
fun ExpertConnectTheme(
    darkTheme: Boolean = true,  // Default to dark for premium look
    dynamicColor: Boolean = false,  // Disabled to preserve brand colors
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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundDark.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ExpertConnectTypography,
        content = content
    )
}
