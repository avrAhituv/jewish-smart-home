package com.jewishhome.app.domain.model

import android.net.Uri

data class Photo(
    val id: String,
    val uri: Uri,
    val name: String,
    val dateAdded: Long,
    val dateTaken: Long?,
    val width: Int,
    val height: Int,
    val size: Long,
    val bucketId: String?,
    val bucketName: String?
)

data class PhotoAlbum(
    val id: String,
    val name: String,
    val coverUri: Uri?,
    val photoCount: Int
)
