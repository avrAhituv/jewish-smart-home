package com.jewishhome.app.domain.model

import android.net.Uri

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long, // in milliseconds
    val uri: Uri,
    val albumArtUri: Uri? = null,
    val source: SongSource = SongSource.LOCAL
)

enum class SongSource {
    LOCAL,      // From SD card or device storage
    STREAMING   // From external app integration
}

data class Playlist(
    val id: String,
    val name: String,
    val songs: List<Song>,
    val coverUri: Uri? = null
)

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentSong: Song? = null,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val playlist: List<Song> = emptyList(),
    val currentIndex: Int = 0,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val volume: Float = 1f
)

enum class ShuffleMode {
    OFF,
    ON
}

enum class RepeatMode {
    OFF,
    ONE,
    ALL
}

data class MusicFolder(
    val path: String,
    val name: String,
    val songCount: Int
)
