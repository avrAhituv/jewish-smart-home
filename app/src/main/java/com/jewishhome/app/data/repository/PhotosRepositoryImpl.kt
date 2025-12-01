package com.jewishhome.app.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.jewishhome.app.data.local.preferences.UserPreferences
import com.jewishhome.app.domain.model.Photo
import com.jewishhome.app.domain.model.PhotoAlbum
import com.jewishhome.app.domain.repository.PhotosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotosRepositoryImpl @Inject constructor(
    private val context: Context,
    private val userPreferences: UserPreferences
) : PhotosRepository {

    private val contentResolver: ContentResolver = context.contentResolver
    private val photosCache = mutableListOf<Photo>()
    private val screensaverAlbumIdFlow = MutableStateFlow<String?>(null)

    override suspend fun getAllPhotos(): List<Photo> = withContext(Dispatchers.IO) {
        if (photosCache.isEmpty()) {
            refreshPhotos()
        }
        photosCache.toList()
    }

    override suspend fun getPhotosByAlbum(albumId: String): List<Photo> = withContext(Dispatchers.IO) {
        getAllPhotos().filter { it.bucketId == albumId }
    }

    override suspend fun getAlbums(): List<PhotoAlbum> = withContext(Dispatchers.IO) {
        val photos = getAllPhotos()
        photos.groupBy { it.bucketId to it.bucketName }
            .map { (bucket, albumPhotos) ->
                PhotoAlbum(
                    id = bucket.first ?: "unknown",
                    name = bucket.second ?: "Unknown",
                    coverUri = albumPhotos.firstOrNull()?.uri,
                    photoCount = albumPhotos.size
                )
            }
            .sortedByDescending { it.photoCount }
    }

    override suspend fun getPhoto(id: String): Photo? = withContext(Dispatchers.IO) {
        getAllPhotos().find { it.id == id }
    }

    override suspend fun getRecentPhotos(limit: Int): List<Photo> = withContext(Dispatchers.IO) {
        getAllPhotos()
            .sortedByDescending { it.dateAdded }
            .take(limit)
    }

    override fun getScreensaverPhotos(): Flow<List<Photo>> {
        return screensaverAlbumIdFlow.map { albumId ->
            if (albumId != null) {
                photosCache.filter { it.bucketId == albumId }
            } else {
                photosCache.toList()
            }
        }
    }

    override suspend fun setScreensaverAlbum(albumId: String?) {
        screensaverAlbumIdFlow.value = albumId
    }

    override fun getScreensaverAlbumId(): Flow<String?> = screensaverAlbumIdFlow

    override suspend fun refreshPhotos(): List<Photo> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<Photo>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(
            collection,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
            val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
            val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
            val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn) ?: ""
                val dateAdded = cursor.getLong(dateAddedColumn)
                val dateTaken = cursor.getLong(dateTakenColumn).takeIf { it > 0 }
                val width = cursor.getInt(widthColumn)
                val height = cursor.getInt(heightColumn)
                val size = cursor.getLong(sizeColumn)
                val bucketId = cursor.getString(bucketIdColumn)
                val bucketName = cursor.getString(bucketNameColumn)

                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                photos.add(
                    Photo(
                        id = id.toString(),
                        uri = contentUri,
                        name = name,
                        dateAdded = dateAdded * 1000, // Convert to milliseconds
                        dateTaken = dateTaken?.times(1000),
                        width = width,
                        height = height,
                        size = size,
                        bucketId = bucketId,
                        bucketName = bucketName
                    )
                )
            }
        }

        photosCache.clear()
        photosCache.addAll(photos)
        photos
    }
}
