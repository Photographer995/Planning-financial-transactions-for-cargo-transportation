package com.example.finlogcalc.features.tripcalculator.driver

import android.provider.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

/**
 * Адаптивная яркость для темной темы Deep Neon
 */
@Composable
fun rememberAdaptiveBrightness(): AdaptiveBrightnessController {
    val context = LocalContext.current
    return remember { AdaptiveBrightnessController(context) }
}

class AdaptiveBrightnessController(private val context: android.content.Context) {
    private val systemBrightness: Int
        get() {
            return try {
                Settings.System.getInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS
                )
            } catch (e: Exception) {
                128 // Средняя яркость по умолчанию
            }
        }
    
    /**
     * Вычисляет адаптивную яркость цвета на основе системной яркости экрана
     */
    fun adaptColorBrightness(baseColor: Color, minAlpha: Float = 0.3f, maxAlpha: Float = 1f): Color {
        val brightness = systemBrightness / 255f
        val alpha = minAlpha + (maxAlpha - minAlpha) * brightness
        return baseColor.copy(alpha = alpha)
    }
    
    /**
     * Получает коэффициент яркости (0.0 - 1.0)
     */
    fun getBrightnessFactor(): Float {
        return systemBrightness / 255f
    }
}

/**
 * Адаптирует цвет неона в зависимости от яркости экрана
 */
@Composable
fun rememberAdaptiveNeonColor(
    baseColor: Color,
    minAlpha: Float = 0.4f,
    maxAlpha: Float = 1f
): Color {
    val adaptiveBrightness = rememberAdaptiveBrightness()
    val brightnessFactor = remember { adaptiveBrightness.getBrightnessFactor() }
    
    val alpha = minAlpha + (maxAlpha - minAlpha) * brightnessFactor
    return baseColor.copy(alpha = alpha)
}

