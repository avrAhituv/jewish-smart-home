package com.jewishhome.app.domain.repository

import com.jewishhome.app.domain.model.Photo
import com.jewishhome.app.domain.model.PhotoAlbum
import kotlinx.coroutines.flow.Flow

interface PhotosRepository {
    suspend fun getAllPhotos(): List<Photo>
    suspend fun getPhotosByAlbum(albumId: String): List<Photo>
    suspend fun getAlbums(): List<PhotoAlbum>
    suspend fun getPhoto(id: String): Photo?
    suspend fun getRecentPhotos(limit: Int = 50): List<Photo>

    // Screensaver photos
    fun getScreensaverPhotos(): Flow<List<Photo>>
    suspend fun setScreensaverAlbum(albumId: String?)
    fun getScreensaverAlbumId(): Flow<String?>

    // Refresh
    suspend fun refreshPhotos(): List<Photo>
}
