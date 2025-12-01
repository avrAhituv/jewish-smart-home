package com.jewishhome.app.domain.repository

import com.jewishhome.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface MusicRepository {

    // Song queries
    suspend fun getAllSongs(): List<Song>
    suspend fun searchSongs(query: String): List<Song>
    suspend fun getSongsByArtist(artist: String): List<Song>
    suspend fun getSongsByAlbum(album: String): List<Song>
    suspend fun getSongById(id: String): Song?

    // Folder operations
    suspend fun getMusicFolders(): List<MusicFolder>
    suspend fun getSongsInFolder(folderPath: String): List<Song>

    // Playlist operations
    suspend fun getPlaylists(): List<Playlist>
    suspend fun getPlaylist(id: String): Playlist?
    suspend fun createPlaylist(name: String, songs: List<Song>): Playlist
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deletePlaylist(id: String)

    // Artists and Albums
    suspend fun getArtists(): List<String>
    suspend fun getAlbums(): List<String>

    // Scan media
    suspend fun scanMediaFiles(): List<Song>

    // Recently played
    suspend fun getRecentlyPlayed(): List<Song>
    suspend fun addToRecentlyPlayed(song: Song)

    // Player state persistence
    fun getPlayerState(): Flow<PlayerState>
    suspend fun savePlayerState(state: PlayerState)
}
