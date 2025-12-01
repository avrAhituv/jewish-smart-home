package com.jewishhome.app.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.jewishhome.app.data.local.preferences.UserPreferences
import com.jewishhome.app.domain.model.*
import com.jewishhome.app.domain.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val context: Context,
    private val userPreferences: UserPreferences
) : MusicRepository {

    private val contentResolver: ContentResolver = context.contentResolver
    private val songsCache = mutableListOf<Song>()
    private val playlistsCache = mutableListOf<Playlist>()
    private val recentlyPlayedCache = mutableListOf<Song>()
    private val playerStateFlow = MutableStateFlow(PlayerState())

    override suspend fun getAllSongs(): List<Song> = withContext(Dispatchers.IO) {
        if (songsCache.isEmpty()) {
            songsCache.addAll(scanMediaFiles())
        }
        songsCache.toList()
    }

    override suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        getAllSongs().filter { song ->
            song.title.contains(query, ignoreCase = true) ||
                    song.artist.contains(query, ignoreCase = true) ||
                    song.album.contains(query, ignoreCase = true)
        }
    }

    override suspend fun getSongsByArtist(artist: String): List<Song> = withContext(Dispatchers.IO) {
        getAllSongs().filter { it.artist.equals(artist, ignoreCase = true) }
    }

    override suspend fun getSongsByAlbum(album: String): List<Song> = withContext(Dispatchers.IO) {
        getAllSongs().filter { it.album.equals(album, ignoreCase = true) }
    }

    override suspend fun getSongById(id: String): Song? = withContext(Dispatchers.IO) {
        getAllSongs().find { it.id == id }
    }

    override suspend fun getMusicFolders(): List<MusicFolder> = withContext(Dispatchers.IO) {
        val songs = getAllSongs()
        songs.groupBy { song ->
            val path = song.uri.path ?: ""
            File(path).parent ?: ""
        }.map { (folderPath, songsInFolder) ->
            MusicFolder(
                path = folderPath,
                name = File(folderPath).name,
                songCount = songsInFolder.size
            )
        }.sortedBy { it.name }
    }

    override suspend fun getSongsInFolder(folderPath: String): List<Song> = withContext(Dispatchers.IO) {
        getAllSongs().filter { song ->
            val path = song.uri.path ?: ""
            File(path).parent == folderPath
        }
    }

    override suspend fun getPlaylists(): List<Playlist> = withContext(Dispatchers.IO) {
        playlistsCache.toList()
    }

    override suspend fun getPlaylist(id: String): Playlist? = withContext(Dispatchers.IO) {
        playlistsCache.find { it.id == id }
    }

    override suspend fun createPlaylist(name: String, songs: List<Song>): Playlist = withContext(Dispatchers.IO) {
        val playlist = Playlist(
            id = System.currentTimeMillis().toString(),
            name = name,
            songs = songs,
            coverUri = songs.firstOrNull()?.albumArtUri
        )
        playlistsCache.add(playlist)
        playlist
    }

    override suspend fun updatePlaylist(playlist: Playlist) = withContext(Dispatchers.IO) {
        val index = playlistsCache.indexOfFirst { it.id == playlist.id }
        if (index != -1) {
            playlistsCache[index] = playlist
        }
    }

    override suspend fun deletePlaylist(id: String) = withContext(Dispatchers.IO) {
        playlistsCache.removeAll { it.id == id }
    }

    override suspend fun getArtists(): List<String> = withContext(Dispatchers.IO) {
        getAllSongs()
            .map { it.artist }
            .distinct()
            .filter { it.isNotBlank() && it != "<unknown>" }
            .sorted()
    }

    override suspend fun getAlbums(): List<String> = withContext(Dispatchers.IO) {
        getAllSongs()
            .map { it.album }
            .distinct()
            .filter { it.isNotBlank() && it != "<unknown>" }
            .sorted()
    }

    override suspend fun scanMediaFiles(): List<Song> = withContext(Dispatchers.IO) {
        val songs = mutableListOf<Song>()

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
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATA
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Unknown"
                val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                val album = cursor.getString(albumColumn) ?: "Unknown Album"
                val duration = cursor.getLong(durationColumn)
                val albumId = cursor.getLong(albumIdColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )

                songs.add(
                    Song(
                        id = id.toString(),
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        uri = contentUri,
                        albumArtUri = albumArtUri,
                        source = SongSource.LOCAL
                    )
                )
            }
        }

        songsCache.clear()
        songsCache.addAll(songs)
        songs
    }

    override suspend fun getRecentlyPlayed(): List<Song> = withContext(Dispatchers.IO) {
        recentlyPlayedCache.toList()
    }

    override suspend fun addToRecentlyPlayed(song: Song) = withContext(Dispatchers.IO) {
        recentlyPlayedCache.removeAll { it.id == song.id }
        recentlyPlayedCache.add(0, song)
        // Keep only last 50 songs
        while (recentlyPlayedCache.size > 50) {
            recentlyPlayedCache.removeLast()
        }
    }

    override fun getPlayerState(): Flow<PlayerState> = playerStateFlow

    override suspend fun savePlayerState(state: PlayerState) {
        playerStateFlow.value = state
    }
}
