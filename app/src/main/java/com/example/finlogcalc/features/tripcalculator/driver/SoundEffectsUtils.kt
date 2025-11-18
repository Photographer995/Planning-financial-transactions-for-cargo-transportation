package com.example.finlogcalc.features.tripcalculator.driver

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext

/**
 * Утилиты для звуковых эффектов (опционально)
 */

enum class SoundType {
    CLICK,
    SUCCESS,
    ERROR,
    WARNING,
    NOTIFICATION
}

@Composable
fun rememberSoundEffects(enabled: Boolean = true): SoundEffects? {
    val context = LocalContext.current
    return remember(enabled) {
        if (enabled) SoundEffects(context) else null
    }
}

class SoundEffects(private val context: Context) {
    private val soundPool: SoundPool by lazy {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    private val sounds = mutableMapOf<SoundType, Int>()

    init {
        // Загрузка звуков (можно добавить реальные файлы)
        // Для демонстрации используем системные звуки
        loadSounds()
    }

    private fun loadSounds() {
        // Здесь можно загрузить реальные звуковые файлы
        // sounds[SoundType.CLICK] = soundPool.load(context, R.raw.click_sound, 1)
    }

    fun play(type: SoundType) {
        sounds[type]?.let { soundId ->
            soundPool.play(soundId, 0.5f, 0.5f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}

