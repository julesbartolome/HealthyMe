package com.example.healthyme.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HealthyMeColors = lightColorScheme(

    primary = Color(0xFF4CAF50),

    secondary = Color(0xFF03A9F4),

    tertiary = Color(0xFFFF9800),

    background = Color(0xFFF5F5F5),

    surface = Color.White,

    onBackground = Color(0xFF222222),

    onSurface = Color(0xFF222222),

    onPrimary = Color.White,

    onSecondary = Color.White,

    onTertiary = Color.White
)

@Composable
fun HealthyMeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HealthyMeColors,
        content = content
    )
}