package com.example.finlogcalc.radio.ui.RadioMusic

import androidx.compose.ui.graphics.Color

object RadioColor {
    val BackgroundGradientStart = Color(0xFF020617) // slate-950
    val BackgroundGradientEnd = Color(0xFF0F172A) // slate-900
    
    val Cyan400 = Color(0xFF22D3EE)
    val Cyan500 = Color(0xFF06B6D4)
    val Purple500 = Color(0xFFA855F7)
    val Purple600 = Color(0xFF9333EA)
    
    val Slate900 = Color(0xFF0F172A)
    val Slate800 = Color(0xFF1E293B)
    val Slate700 = Color(0xFF334155)
    
    val White10 = Color.White.copy(alpha = 0.1f)
    val White20 = Color.White.copy(alpha = 0.2f)
    val Gray400 = Color(0xFF9CA3AF)
    val Gray500 = Color(0xFF6B7280)
    
    val Cyan500_10 = Cyan500.copy(alpha = 0.1f)
    val Purple500_10 = Purple500.copy(alpha = 0.1f)
    
    val PlayButtonGradient = listOf(Cyan500, Purple600)
}
