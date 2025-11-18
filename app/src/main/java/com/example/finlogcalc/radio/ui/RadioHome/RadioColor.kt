package com.example.finlogcalc.radio.ui.RadioHome

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFCCC2DC)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

object NeonColorsRadio {
    val BackgroundGradientStart = Color(0xFF0a0a12) // Dark background
    val BackgroundDark = Color(0xFF10101A)
    
    // Cyan colors
    val Cyan400 = Color(0xFF22D3EE)
    val Cyan500 = Color(0xFF06B6D4)
    val Cyan600 = Color(0xFF0891B2)
    
    // Purple colors
    val Purple500 = Color(0xFFA855F7)
    val Purple600 = Color(0xFF9333EA)
    
    // Pink colors
    val Pink500 = Color(0xFFEC4899)
    val Pink600 = Color(0xFFDB2777)
    
    // Orange colors
    val Orange500 = Color(0xFFF97316)
    val Orange600 = Color(0xFFEA580C)
    val Red600 = Color(0xFFDC2626)
    
    // Green colors
    val Green500 = Color(0xFF22C55E)
    val Emerald600 = Color(0xFF059669)
    
    // Violet colors
    val Violet600 = Color(0xFF7C3AED)
    val Fuchsia600 = Color(0xFFC026D3)
    
    // Amber colors
    val Amber500 = Color(0xFFF59E0B)
    
    // Semi-transparent variants
    val Cyan500_10 = Cyan500.copy(alpha = 0.1f)
    val Purple500_10 = Purple500.copy(alpha = 0.1f)
    val Pink500_20 = Pink500.copy(alpha = 0.2f)
    
    val Slate900 = Color(0xFF0F172A)
    val Gray300 = Color(0xFFD1D5DB)
    val Gray400 = Color(0xFF9CA3AF)
    
    // Gradient pairs for favorite cards
    val gradientPairs = listOf(
        Pair(Pink600, Purple600),      // Pink to Purple
        Pair(Cyan500, Cyan600),        // Cyan to Blue
        Pair(Orange500, Red600),       // Orange to Red
        Pair(Green500, Emerald600),    // Green to Emerald
        Pair(Violet600, Fuchsia600),   // Violet to Fuchsia
        Pair(Amber500, Orange600)      // Amber to Orange
    )
}
