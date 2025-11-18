package com.example.finlogcalc.features.tripcalculator.driver

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

/**
 * Deep Neon Theme - Цветовая палитра и стили для киберпанк-интерфейса
 * 
 * Создает атмосферу "Глубокого Неона" - футуристический мегаполис ночью,
 * где свет исходит изнутри элементов интерфейса.
 */
object DeepNeonTheme {
    // Основные фоновые цвета
    val BackgroundDark = Color(0xFF0A0A12) // Почти черный
    val BackgroundDeepBlue = Color(0xFF0D0D1A) // Глубокий темно-синий
    val BackgroundPurple = Color(0xFF0F0A1F) // Темно-фиолетовый
    
    // Неоновые акцентные цвета с эффектом свечения
    val NeonCyan = Color(0xFF00F5FF) // Электрический бирюзовый
    val NeonCyanBright = Color(0xFF00FFFF) // Яркий бирюзовый
    val NeonCyanDark = Color(0xFF00B8C4) // Темный бирюзовый
    
    val NeonFuchsia = Color(0xFFFF00FF) // Фуксия
    val NeonFuchsiaBright = Color(0xFFFF0AFF) // Яркая фуксия
    val NeonFuchsiaDark = Color(0xFFCC00CC) // Темная фуксия
    
    val NeonGreen = Color(0xFF00FF41) // Ярко-зеленый
    val NeonGreenBright = Color(0xFF39FF14) // Светло-зеленый
    val NeonGreenDark = Color(0xFF00CC33) // Темно-зеленый
    
    val NeonOrange = Color(0xFFFF6B00) // Оранжевый
    val NeonOrangeBright = Color(0xFFFF8C00) // Яркий оранжевый
    val NeonOrangeDark = Color(0xFFCC5500) // Темный оранжевый
    
    val NeonPurple = Color(0xFF9D00FF) // Фиолетовый
    val NeonPurpleBright = Color(0xFFB300FF) // Яркий фиолетовый
    val NeonPurpleDark = Color(0xFF7A00CC) // Темный фиолетовый
    
    val NeonBlue = Color(0xFF0066FF) // Синий
    val NeonBlueBright = Color(0xFF0080FF) // Яркий синий
    val NeonBlueDark = Color(0xFF0052CC) // Темный синий
    
    val NeonYellow = Color(0xFFFFD700) // Желтый
    val NeonYellowBright = Color(0xFFFFE500) // Яркий желтый
    
    val NeonRed = Color(0xFFFF0040) // Красный
    val NeonRedBright = Color(0xFFFF0066) // Яркий красный
    
    // Текст и границы
    val TextNeonWhite = Color(0xFFE0E0E0) // Неоновый белый
    val TextNeonGray = Color(0xFFB0B0B0) // Неоновый серый
    val TextNeonDark = Color(0xFF808080) // Темно-серый
    
    val BorderNeon = Color(0xFF00F5FF).copy(alpha = 0.3f) // Неоновая граница
    val BorderNeonBright = Color(0xFF00FFFF).copy(alpha = 0.5f) // Яркая неоновая граница
    
    // Градиенты для карточек
    val GradientNewTrip = listOf(NeonCyanDark, NeonCyan, NeonCyanBright)
    val GradientMyTrips = listOf(NeonFuchsiaDark, NeonFuchsia, NeonOrange)
    val GradientRoutes = listOf(NeonPurpleDark, NeonPurple, NeonGreenDark)
    val GradientReports = listOf(NeonGreenDark, NeonGreen, NeonYellow)
    
    // Градиенты для кнопок
    val GradientStartTrip = listOf(NeonCyanDark, NeonCyanBright)
    val GradientCreateRoute = listOf(NeonCyan, NeonGreen)
    val GradientGenerateReport = listOf(NeonCyan, NeonGreen)
    val GradientNewTripButton = listOf(NeonCyanDark, NeonCyanBright)
    
    // Фоновые градиенты
    val BackgroundGradient = listOf(BackgroundDark, BackgroundDeepBlue, BackgroundPurple)
    
    // Создает градиент с эффектом свечения
    fun createGlowGradient(colors: List<Color>): Brush {
        return Brush.linearGradient(colors)
    }
    
    // Создает вертикальный градиент
    fun createVerticalGradient(colors: List<Color>): Brush {
        return Brush.verticalGradient(colors)
    }
}

