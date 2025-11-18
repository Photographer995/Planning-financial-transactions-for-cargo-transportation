package com.example.finlogcalc.radio.ui.RadioMusic

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long, // в миллисекундах
    val uri: Uri,
    val albumArtUri: Uri? = null,
    val dateAdded: Long,
    val genre: String? = null,
    val trackNumber: Int = 0
) {
    fun getDurationFormatted(): String {
        val totalSeconds = (duration / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}

