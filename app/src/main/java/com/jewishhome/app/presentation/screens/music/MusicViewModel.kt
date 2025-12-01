package com.jewishhome.app.presentation.screens.music

import android.content.ComponentName
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.jewishhome.app.data.service.MusicPlaybackService
import com.jewishhome.app.domain.model.*
import com.jewishhome.app.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    init {
        initializeMediaController()
        loadSongs()
        startProgressUpdates()
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                mediaController = controllerFuture?.get()
                mediaController?.addListener(playerListener)
                updatePlayerState()
            },
            MoreExecutors.directExecutor()
        )
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.let { item ->
                val currentSong = _uiState.value.songs.find { it.id == item.mediaId }
                    ?: _uiState.value.currentQueue.find { it.id == item.mediaId }
                _uiState.value = _uiState.value.copy(
                    currentSong = currentSong,
                    currentIndex = mediaController?.currentMediaItemIndex ?: 0
                )
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _uiState.value = _uiState.value.copy(
                isLoading = playbackState == Player.STATE_BUFFERING
            )
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _uiState.value = _uiState.value.copy(
                shuffleMode = if (shuffleModeEnabled) ShuffleMode.ON else ShuffleMode.OFF
            )
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _uiState.value = _uiState.value.copy(
                repeatMode = when (repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
            )
        }
    }

    private fun updatePlayerState() {
        mediaController?.let { controller ->
            val currentMediaItem = controller.currentMediaItem
            val currentSong = currentMediaItem?.let { item ->
                _uiState.value.songs.find { it.id == item.mediaId }
            }

            _uiState.value = _uiState.value.copy(
                isPlaying = controller.isPlaying,
                currentSong = currentSong,
                currentPosition = controller.currentPosition,
                duration = controller.duration.coerceAtLeast(0),
                currentIndex = controller.currentMediaItemIndex,
                shuffleMode = if (controller.shuffleModeEnabled) ShuffleMode.ON else ShuffleMode.OFF,
                repeatMode = when (controller.repeatMode) {
                    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                    else -> RepeatMode.OFF
                }
            )
        }
    }

    private fun startProgressUpdates() {
        viewModelScope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    if (controller.isPlaying) {
                        _uiState.value = _uiState.value.copy(
                            currentPosition = controller.currentPosition,
                            duration = controller.duration.coerceAtLeast(0)
                        )
                    }
                }
                delay(1000)
            }
        }
    }

    private fun loadSongs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val songs = musicRepository.getAllSongs()
                val artists = musicRepository.getArtists()
                val albums = musicRepository.getAlbums()
                val playlists = musicRepository.getPlaylists()
                val recentlyPlayed = musicRepository.getRecentlyPlayed()

                _uiState.value = _uiState.value.copy(
                    songs = songs,
                    filteredSongs = songs,
                    artists = artists,
                    albums = albums,
                    playlists = playlists,
                    recentlyPlayed = recentlyPlayed,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun refreshSongs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val songs = musicRepository.scanMediaFiles()
                _uiState.value = _uiState.value.copy(
                    songs = songs,
                    filteredSongs = filterSongs(songs, _uiState.value.searchQuery),
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun playSong(song: Song) {
        viewModelScope.launch {
            musicRepository.addToRecentlyPlayed(song)
            val recentlyPlayed = musicRepository.getRecentlyPlayed()
            _uiState.value = _uiState.value.copy(recentlyPlayed = recentlyPlayed)
        }

        val mediaItem = createMediaItem(song)
        mediaController?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return

        viewModelScope.launch {
            songs.getOrNull(startIndex)?.let { song ->
                musicRepository.addToRecentlyPlayed(song)
                val recentlyPlayed = musicRepository.getRecentlyPlayed()
                _uiState.value = _uiState.value.copy(recentlyPlayed = recentlyPlayed)
            }
        }

        val mediaItems = songs.map { createMediaItem(it) }
        mediaController?.apply {
            setMediaItems(mediaItems, startIndex, 0)
            prepare()
            play()
        }

        _uiState.value = _uiState.value.copy(currentQueue = songs)
    }

    private fun createMediaItem(song: Song): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(song.albumArtUri)
            .build()

        return MediaItem.Builder()
            .setUri(song.uri)
            .setMediaMetadata(metadata)
            .setMediaId(song.id)
            .setRequestMetadata(
                MediaItem.RequestMetadata.Builder()
                    .setMediaUri(song.uri)
                    .build()
            )
            .build()
    }

    fun togglePlayPause() {
        mediaController?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }

    fun skipToNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipToPrevious() {
        mediaController?.seekToPreviousMediaItem()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    fun toggleShuffle() {
        mediaController?.let {
            it.shuffleModeEnabled = !it.shuffleModeEnabled
        }
    }

    fun toggleRepeatMode() {
        mediaController?.let { controller ->
            controller.repeatMode = when (controller.repeatMode) {
                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                else -> Player.REPEAT_MODE_OFF
            }
        }
    }

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredSongs = filterSongs(_uiState.value.songs, query)
        )
    }

    private fun filterSongs(songs: List<Song>, query: String): List<Song> {
        if (query.isBlank()) return songs
        return songs.filter { song ->
            song.title.contains(query, ignoreCase = true) ||
                    song.artist.contains(query, ignoreCase = true) ||
                    song.album.contains(query, ignoreCase = true)
        }
    }

    fun selectTab(tab: MusicTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun selectArtist(artist: String) {
        viewModelScope.launch {
            val songs = musicRepository.getSongsByArtist(artist)
            _uiState.value = _uiState.value.copy(
                filteredSongs = songs,
                selectedTab = MusicTab.SONGS
            )
        }
    }

    fun selectAlbum(album: String) {
        viewModelScope.launch {
            val songs = musicRepository.getSongsByAlbum(album)
            _uiState.value = _uiState.value.copy(
                filteredSongs = songs,
                selectedTab = MusicTab.SONGS
            )
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            val playlist = musicRepository.createPlaylist(name, emptyList())
            val playlists = musicRepository.getPlaylists()
            _uiState.value = _uiState.value.copy(playlists = playlists)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}

data class MusicUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val songs: List<Song> = emptyList(),
    val filteredSongs: List<Song> = emptyList(),
    val artists: List<String> = emptyList(),
    val albums: List<String> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val recentlyPlayed: List<Song> = emptyList(),
    val currentQueue: List<Song> = emptyList(),

    // Player state
    val isPlaying: Boolean = false,
    val currentSong: Song? = null,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val currentIndex: Int = 0,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val repeatMode: RepeatMode = RepeatMode.OFF,

    // UI state
    val selectedTab: MusicTab = MusicTab.SONGS,
    val searchQuery: String = ""
)

enum class MusicTab {
    SONGS,
    ARTISTS,
    ALBUMS,
    PLAYLISTS,
    RECENT
}
