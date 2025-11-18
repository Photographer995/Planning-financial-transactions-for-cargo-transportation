package com.example.finlogcalc.features.tripcalculator.driver

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext

/**
 * Утилиты для haptic feedback
 */

enum class HapticType {
    LIGHT_CLICK,    // Легкое нажатие
    MEDIUM_CLICK,   // Среднее нажатие
    HEAVY_CLICK,    // Сильное нажатие
    DOUBLE_CLICK,   // Двойное нажатие
    SUCCESS,        // Успешное действие
    ERROR,          // Ошибка
    WARNING         // Предупреждение
}

@Composable
fun rememberHapticFeedback(): HapticFeedback {
    val context = LocalContext.current
    return remember { HapticFeedback(context) }
}

class HapticFeedback(private val context: Context) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun performHaptic(type: HapticType) {
        if (vibrator == null || !vibrator!!.hasVibrator()) return

        when (type) {
            HapticType.LIGHT_CLICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(10)
                }
            }
            HapticType.MEDIUM_CLICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(20)
                }
            }
            HapticType.HEAVY_CLICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            }
            HapticType.DOUBLE_CLICK -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(
                        longArrayOf(0, 50, 50, 50),
                        -1
                    ))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 50, 50, 50), -1)
                }
            }
            HapticType.SUCCESS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(
                        longArrayOf(0, 30, 50, 30, 50, 30),
                        -1
                    ))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 30, 50, 30, 50, 30), -1)
                }
            }
            HapticType.ERROR -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(
                        longArrayOf(0, 100, 50, 100, 50, 100),
                        -1
                    ))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 100, 50, 100, 50, 100), -1)
                }
            }
            HapticType.WARNING -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(
                        longArrayOf(0, 50, 100, 50),
                        -1
                    ))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 50, 100, 50), -1)
                }
            }
        }
    }
}

/**
 * Модификатор для добавления haptic feedback при нажатии
 */
fun Modifier.hapticClickable(
    hapticFeedback: HapticFeedback,
    hapticType: HapticType = HapticType.LIGHT_CLICK,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            hapticFeedback.performHaptic(hapticType)
        }
    }

    this.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
    )
}
