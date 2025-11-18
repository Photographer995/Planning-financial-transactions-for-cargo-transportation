package com.example.finlogcalc.utils.chat

import androidx.annotation.DrawableRes
import com.example.finlogcalc.R

data class ChatBackground(
    val name: String,
    @DrawableRes val imageRes: Int,
    val thumbnailRes: Int
)

val chatBackgrounds = listOf(
    ChatBackground("Няко", R.drawable.nyako5, R.drawable.nyako5),
    ChatBackground("Пляж", R.drawable.nyako2, R.drawable.nyako2), // Placeholder
    ChatBackground("Космос", R.drawable.nyako3, R.drawable.nyako3), // Placeholder
    ChatBackground("Город", R.drawable.nyako4, R.drawable.nyako4) // Placeholder
)