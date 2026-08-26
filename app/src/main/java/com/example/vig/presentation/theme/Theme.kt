package com.example.vig.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TealBeigeColorScheme = lightColorScheme(
    primary = GlowingTeal,
    secondary = DarkTealSurface,
    tertiary = ElectricTeal,
    background = WarmBeige,
    surface = LightCream,
    onPrimary = DarkTealBase,
    onSecondary = LightCream,
    onTertiary = DarkTealBase,
    onBackground = DarkEspresso,
    onSurface = DarkEspresso,
    error = MutedRed
)

@Composable
fun VigTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TealBeigeColorScheme,
        content = content
    )
}
