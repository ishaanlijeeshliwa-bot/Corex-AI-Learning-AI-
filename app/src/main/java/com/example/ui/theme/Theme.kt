package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = HighDensityPrimary,
    secondary = HighDensitySecondary,
    tertiary = HighDensityTertiary,
    background = HighDensityBg,
    surface = HighDensitySurface,
    surfaceVariant = HighDensitySurfaceVariant,
    onPrimary = HighDensityTertiary, // High density contrast
    onSecondary = HighDensityText,
    onBackground = HighDensityText,
    onSurface = HighDensityText,
    primaryContainer = HighDensityPrimaryContainer,
    onPrimaryContainer = HighDensityOnPrimaryContainer,
    secondaryContainer = HighDensitySecondary,
    onSecondaryContainer = HighDensityText,
    outline = HighDensityMutedText,
    error = Color(0xFFEF4444)
)

private val LightColorScheme = darkColorScheme( // For High Density theme, we maintain active dark mode
    primary = HighDensityPrimary,
    secondary = HighDensitySecondary,
    tertiary = HighDensityTertiary,
    background = HighDensityBg,
    surface = HighDensitySurface,
    surfaceVariant = HighDensitySurfaceVariant,
    onPrimary = HighDensityTertiary,
    onSecondary = HighDensityText,
    onBackground = HighDensityText,
    onSurface = HighDensityText,
    primaryContainer = HighDensityPrimaryContainer,
    onPrimaryContainer = HighDensityOnPrimaryContainer,
    secondaryContainer = HighDensitySecondary,
    onSecondaryContainer = HighDensityText,
    outline = HighDensityMutedText,
    error = Color(0xFFEF4444)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Enforce our curated brand identity and prevent wallpaper override
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
