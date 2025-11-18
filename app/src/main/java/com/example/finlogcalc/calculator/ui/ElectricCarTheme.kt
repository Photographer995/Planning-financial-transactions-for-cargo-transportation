package com.example.finlogcalc.calculator.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ElectricGreen100 = Color(0xFFE8F5E9)
val ElectricGreen400 = Color(0xFF66BB6A)
val ElectricGreen700 = Color(0xFF5ADC5B)
val ElectricGreenBackground = Color(0xFFEDF7EE)
val ElectricGreenCard = Color(0xFFDCEADF)
val ElectricGreenBorder = Color(0xFF9CCC9C)
val ElectricGreenInputBackground = Color(0xFFF0FBF0)
val ElectricGreenTextPrimary = Color(0xFF1B5E20)
val ElectricGreenTextSecondary = Color(0xFF4CAF50)
val White = Color(0xFFFFFFFF)
val DarkGreen = Color(0xFF003300)

private val LightElectricCarColorScheme = lightColorScheme(
    primary = ElectricGreen400,
    onPrimary = White,
    primaryContainer = ElectricGreen100,
    onPrimaryContainer = ElectricGreen700,
    secondary = ElectricGreen700,
    onSecondary = White,
    secondaryContainer = ElectricGreenBackground,
    onSecondaryContainer = ElectricGreenTextPrimary,
    tertiary = ElectricGreenBorder,
    onTertiary = White,
    tertiaryContainer = ElectricGreenCard,
    onTertiaryContainer = ElectricGreenTextPrimary,
    error = Color(0xFFB00020),
    onError = White,
    background = ElectricGreenBackground,
    onBackground = ElectricGreenTextPrimary,
    surface = ElectricGreenCard,
    onSurface = ElectricGreenTextPrimary,
    surfaceVariant = ElectricGreenInputBackground,
    onSurfaceVariant = ElectricGreenTextPrimary,
    outline = ElectricGreenBorder,
    outlineVariant = ElectricGreenTextSecondary,
)

private val DarkElectricCarColorScheme = darkColorScheme(
    primary = ElectricGreen400,
    onPrimary = DarkGreen,
    primaryContainer = ElectricGreen700,
    onPrimaryContainer = ElectricGreen100,
    secondary = ElectricGreen400,
    onSecondary = DarkGreen,
    secondaryContainer = DarkGreen,
    onSecondaryContainer = ElectricGreen100,
    tertiary = ElectricGreenBorder,
    onTertiary = DarkGreen,
    tertiaryContainer = DarkGreen,
    onTertiaryContainer = ElectricGreen100,
    error = Color(0xFFCF6679),
    onError = DarkGreen,
    background = DarkGreen,
    onBackground = White,
    surface = DarkGreen,
    onSurface = White,
    surfaceVariant = DarkGreen,
    onSurfaceVariant = White,
    outline = ElectricGreenBorder,
    outlineVariant = ElectricGreenTextSecondary,
)

@Composable
fun ElectricCarCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkElectricCarColorScheme
    } else {
        LightElectricCarColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = MaterialTheme.typography,
        content = content
    )
}

// Custom colors for specific UI elements, similar to FuelCalculatorTheme
val InputBackgroundGreen @Composable get() = if (isSystemInDarkTheme()) ElectricGreen400 else ElectricGreenInputBackground
val CardGreen @Composable get() = if (isSystemInDarkTheme()) DarkGreen else ElectricGreenCard
val BorderGreen @Composable get() = if (isSystemInDarkTheme()) ElectricGreen400 else ElectricGreenBorder
val TextPrimaryGreen @Composable get() = if (isSystemInDarkTheme()) White else ElectricGreenTextPrimary
val TextSecondaryGreen @Composable get() = if (isSystemInDarkTheme()) ElectricGreen100 else ElectricGreenTextSecondary

