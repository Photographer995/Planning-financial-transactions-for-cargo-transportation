package com.example.finlogcalc.radio.ui.RadioMusic

import android.content.ComponentName
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.finlogcalc.radio.service.RadioPlayerService
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

class MusicViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext

    private var mediaControllerFuture: ListenableFuture<MediaController>
    private val mediaController: MediaController?
        get() = if (mediaControllerFuture.isDone) mediaControllerFuture.get() else null

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _filteredSongs = MutableStateFlow<List<Song>>(emptyList())
    val filteredSongs: StateFlow<List<Song>> = _filteredSongs.asStateFlow()

    private val _currentPlayingSong = MutableStateFlow<Song?>(null)
    val currentPlayingSong: StateFlow<Song?> = _currentPlayingSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPosition = MutableStateFlow(0L)
    val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(MusicTab.SONGS)
    val selectedTab: StateFlow<MusicTab> = _selectedTab.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            updateCurrentSong()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateCurrentSong()
        }
    }

    enum class MusicTab {
        SONGS, PLAYLISTS, FOLDERS
    }

    enum class SortType {
        TITLE, ARTIST, ALBUM, DATE_ADDED, DURATION
    }

    private var currentSortType = SortType.TITLE

    init {
        val sessionToken = SessionToken(appContext, ComponentName(appContext, RadioPlayerService::class.java))
        mediaControllerFuture = MediaController.Builder(appContext, sessionToken).buildAsync()
        mediaControllerFuture.addListener({
            mediaController?.addListener(playerListener)
            if (mediaController != null) {
                observePlaybackPosition()
            }
        }, MoreExecutors.directExecutor())

        loadSongs()
    }

    fun loadSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            val songsList = withContext(Dispatchers.IO) {
                scanForAudioFiles()
            }
            _songs.value = songsList
            _filteredSongs.value = songsList
            _isLoading.value = false
        }
    }

    private fun scanForAudioFiles(): List<Song> {
        val songsList = mutableListOf<Song>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.GENRE,
            MediaStore.Audio.Media.TRACK
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val cursor: Cursor? = appContext.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val genreColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE)
            val trackColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn) ?: "Unknown Title"
                val artist = it.getString(artistColumn) ?: "Unknown Artist"
                val album = it.getString(albumColumn) ?: "Unknown Album"
                val duration = it.getLong(durationColumn)
                val dateAdded = it.getLong(dateAddedColumn)
                val genre = it.getString(genreColumn)
                val track = it.getInt(trackColumn)

                val contentUri = Uri.withAppendedPath(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id.toString()
                )

                val albumArtUri = Uri.parse("content://media/external/audio/albumart/$id")

                val song = Song(
                    id = id,
                    title = title,
                    artist = artist,
                    album = album,
                    duration = duration,
                    uri = contentUri,
                    albumArtUri = albumArtUri,
                    dateAdded = dateAdded,
                    genre = genre,
                    trackNumber = track
                )

                songsList.add(song)
            }
        }

        return songsList
    }

    fun playSong(song: Song) {
        _currentPlayingSong.value = song
        mediaController?.apply {
            val mediaItem = MediaItem.Builder()
                .setUri(song.uri)
                .setMediaId(song.uri.toString())
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .build()
                )
                .build()
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun playShuffle() {
        val songsList = _filteredSongs.value
        if (songsList.isNotEmpty()) {
            val shuffledList = songsList.shuffled()
            val firstSong = shuffledList.first()
            _currentPlayingSong.value = firstSong
            
            mediaController?.let { controller ->
                val mediaItems = shuffledList.map { song ->
                    MediaItem.Builder()
                        .setUri(song.uri)
                        .setMediaId(song.uri.toString())
                        .setMediaMetadata(
                            androidx.media3.common.MediaMetadata.Builder()
                                .setTitle(song.title)
                                .setArtist(song.artist)
                                .setAlbumTitle(song.album)
                                .build()
                        )
                        .build()
                }
                controller.setMediaItems(mediaItems)
                controller.prepare()
                controller.play()
            }
        }
    }

    fun pauseMusic() {
        mediaController?.pause()
    }

    fun resumeMusic() {
        mediaController?.play()
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            pauseMusic()
        } else {
            resumeMusic()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        filterSongs(query)
    }

    private fun filterSongs(query: String) {
        viewModelScope.launch {
            val filtered = if (query.isBlank()) {
                _songs.value
            } else {
                _songs.value.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true) ||
                    it.album.contains(query, ignoreCase = true)
                }
            }
            _filteredSongs.value = filtered
        }
    }

    fun setSelectedTab(tab: MusicTab) {
        _selectedTab.value = tab
    }

    fun sortSongs(sortType: SortType) {
        currentSortType = sortType
        viewModelScope.launch {
            val sorted = when (sortType) {
                SortType.TITLE -> _filteredSongs.value.sortedBy { it.title }
                SortType.ARTIST -> _filteredSongs.value.sortedBy { it.artist }
                SortType.ALBUM -> _filteredSongs.value.sortedBy { it.album }
                SortType.DATE_ADDED -> _filteredSongs.value.sortedByDescending { it.dateAdded }
                SortType.DURATION -> _filteredSongs.value.sortedBy { it.duration }
            }
            _filteredSongs.value = sorted
        }
    }

    fun playNextSong() {
        mediaController?.seekToNextMediaItem()
        mediaController?.play()
    }

    fun playPreviousSong() {
        mediaController?.seekToPreviousMediaItem()
        mediaController?.play()
    }
    
    fun seekToPosition(progress: Float) {
        mediaController?.let { controller ->
            val duration = controller.duration
            if (duration != C.TIME_UNSET) {
                val newPosition = (progress * duration).toLong()
                controller.seekTo(newPosition)
            }
        }
    }

    private fun observePlaybackPosition() {
        viewModelScope.launch {
            while (true) {
                mediaController?.let { controller ->
                    val position = controller.currentPosition
                    _playbackPosition.value = position
                }
                kotlinx.coroutines.delay(100)
            }
        }
    }

    private fun updateCurrentSong() {
        val currentMediaItem = mediaController?.currentMediaItem
        currentMediaItem?.let { mediaItem ->
            val mediaId = mediaItem.mediaId
            val song = _songs.value.find { 
                it.uri.toString() == mediaId
            }
            _currentPlayingSong.value = song
        }
    }

    override fun onCleared() {
        mediaController?.removeListener(playerListener)
        MediaController.releaseFuture(mediaControllerFuture)
        super.onCleared()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MusicViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MusicViewModel(context.applicationContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}