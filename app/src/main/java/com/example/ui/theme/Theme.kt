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
    primary = MintSecondary,
    secondary = SageContainer,
    tertiary = SoftGoldAccent,
    background = BotanicalBackgroundDark,
    surface = BotanicalSurfaceDark,
    onPrimary = EmeraldGreenDark,
    onSecondary = BotanicalOnSurfaceDark,
    onBackground = BotanicalOnSurfaceDark,
    onSurface = BotanicalOnSurfaceDark,
    surfaceVariant = Color(0xFF22352B)
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreenPrimary,
    secondary = MintSecondary,
    tertiary = TerracottaAccent,
    background = BotanicalBackgroundLight,
    surface = BotanicalSurfaceLight,
    onPrimary = Color.White,
    onSecondary = BotanicalOnSurface,
    onBackground = BotanicalOnSurface,
    onSurface = BotanicalOnSurface,
    primaryContainer = SageContainer,
    onPrimaryContainer = EmeraldGreenDark,
    surfaceVariant = Color(0xFFE5EFE8)
)

@Composable
fun PaudPanditTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set false to preserve rich brand green identity
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

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    PaudPanditTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

