package com.example.finlogcalc.radio.ui.viewmodel

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.finlogcalc.radio.data.RadioStation
import com.example.finlogcalc.radio.service.RadioPlayerService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RadioPlayerViewModel(context: Context) : ViewModel() {

    private var mediaControllerFuture: ListenableFuture<MediaController>
    private val mediaController: MediaController?
        get() = if (mediaControllerFuture.isDone) mediaControllerFuture.get() else null

    private val _currentPlayingStation = MutableStateFlow<RadioStation?>(null)
    val currentPlayingStation: StateFlow<RadioStation?> = _currentPlayingStation

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    // Metadata support
    data class TrackMetadata(
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val genre: String? = null
    )
    
    private val _currentMetadata = MutableStateFlow<TrackMetadata?>(null)
    val currentMetadata: StateFlow<TrackMetadata?> = _currentMetadata

    // Sleep timer support
    private val _sleepRemainingMs = MutableStateFlow<Long?>(null)
    val sleepRemainingMs: StateFlow<Long?> = _sleepRemainingMs
    private var sleepJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
        
        override fun onMediaMetadataChanged(metadata: androidx.media3.common.MediaMetadata) {
            _currentMetadata.value = TrackMetadata(
                title = metadata.title?.toString(),
                artist = metadata.artist?.toString(),
                album = metadata.albumTitle?.toString(),
                genre = metadata.genre?.toString()
            )
        }
    }

    init {
        val sessionToken = SessionToken(context, ComponentName(context, RadioPlayerService::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture.addListener({
            mediaController?.addListener(playerListener)
        }, MoreExecutors.directExecutor())
    }

    fun playRadio(radioStation: RadioStation) {
        _currentPlayingStation.value = radioStation
        mediaController?.apply {
            setMediaItem(MediaItem.fromUri(radioStation.url))
            prepare()
            play()
        }
    }

    fun pauseRadio() {
        mediaController?.pause()
    }

    fun stopRadio() {
        mediaController?.stop()
        _currentPlayingStation.value = null
    }

    fun startSleepTimer(minutes: Int) {
        if (minutes <= 0) {
            cancelSleepTimer()
            return
        }
        sleepJob?.cancel()
        val durationMs = minutes * 60_000L
        val endAt = System.currentTimeMillis() + durationMs
        _sleepRemainingMs.value = durationMs
        sleepJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                val remaining = endAt - System.currentTimeMillis()
                if (remaining <= 0) {
                    _sleepRemainingMs.value = null
                    pauseRadio()
                    break
                } else {
                    _sleepRemainingMs.value = remaining
                }
                delay(1000)
            }
        }
    }

    fun cancelSleepTimer() {
        sleepJob?.cancel()
        sleepJob = null
        _sleepRemainingMs.value = null
    }

    override fun onCleared() {
        mediaController?.removeListener(playerListener)
        MediaController.releaseFuture(mediaControllerFuture)
        sleepJob?.cancel()
        super.onCleared()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RadioPlayerViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RadioPlayerViewModel(context.applicationContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}