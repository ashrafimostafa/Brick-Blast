package com.example.brickblast.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF448AFF),
    onPrimary = Color.White,
    secondary = Color(0xFFFF6D00),
    onSecondary = Color.White,
    tertiary = Color(0xFF00E5FF),
    background = Color(0xFF0D1B2A),
    onBackground = Color.White,
    surface = Color(0xFF1B263B),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF253550),
    onSurfaceVariant = Color(0xFFB0BEC5)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    secondary = Color(0xFFE65100),
    onSecondary = Color.White,
    tertiary = Color(0xFF00838F),
    background = Color(0xFFF2F5F9),
    onBackground = Color(0xFF101418),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF101418),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF45525F)
)

@Composable
fun BrickBlastTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content
    )
}
