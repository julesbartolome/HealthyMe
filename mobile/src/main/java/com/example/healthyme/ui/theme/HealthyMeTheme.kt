package com.example.healthyme.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HealthyMeColors = darkColorScheme(

    primary = Color(0xFF4CAF50),

    secondary = Color(0xFF03A9F4),

    tertiary = Color(0xFFFF9800),

    background = Color(0xFF121212),

    surface = Color(0xFF1E1E1E)
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