package com.example.finlogcalc.calculator.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// --- Electric Car Theme Colors ---
val ElectricGreenDark = Color(0xFF1B5E20)
val ElectricGreenPrimary = Color(0xFF3D9970)
val ElectricGreenLight = Color(0xFF90C695)
val ElectricBackground = Color(0xFFF5F7F5)
val ElectricCardBackground = Color(0xFFFFFFFF)
val ElectricTextPrimary = Color(0xFF2E4636)
val ElectricTextSecondary = Color(0xFF5A7863)
val ElectricBorder = Color(0xFFDCE2DC)

private val LightColorScheme = lightColorScheme(
    primary = ElectricGreenPrimary,
    secondary = ElectricGreenLight,
    background = ElectricBackground,
    surface = ElectricCardBackground,
    onPrimary = Color.White,
    onSecondary = ElectricTextPrimary,
    onBackground = ElectricTextPrimary,
    onSurface = ElectricTextPrimary
)

@Composable
fun ElectricCarCalculatorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
