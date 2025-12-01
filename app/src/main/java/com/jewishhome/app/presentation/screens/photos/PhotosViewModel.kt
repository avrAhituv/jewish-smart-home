package com.jewishhome.app.presentation.screens.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewishhome.app.domain.model.Photo
import com.jewishhome.app.domain.model.PhotoAlbum
import com.jewishhome.app.domain.repository.PhotosRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val photosRepository: PhotosRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotosUiState())
    val uiState: StateFlow<PhotosUiState> = _uiState.asStateFlow()

    init {
        loadAlbums()
        observeScreensaverAlbum()
    }

    private fun observeScreensaverAlbum() {
        viewModelScope.launch {
            photosRepository.getScreensaverAlbumId().collect { albumId ->
                _uiState.value = _uiState.value.copy(screensaverAlbumId = albumId)
            }
        }
    }

    fun loadAlbums() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val albums = photosRepository.getAlbums()
                _uiState.value = _uiState.value.copy(
                    albums = albums,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "שגיאה בטעינת אלבומים"
                )
            }
        }
    }

    fun selectAlbum(album: PhotoAlbum) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedAlbum = album,
                isLoading = true,
                viewMode = PhotosViewMode.GRID
            )
            try {
                val photos = photosRepository.getPhotosByAlbum(album.id)
                _uiState.value = _uiState.value.copy(
                    currentPhotos = photos,
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

    fun showAllPhotos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedAlbum = null,
                isLoading = true,
                viewMode = PhotosViewMode.GRID
            )
            try {
                val photos = photosRepository.getAllPhotos()
                _uiState.value = _uiState.value.copy(
                    currentPhotos = photos,
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

    fun selectPhoto(photo: Photo) {
        val index = _uiState.value.currentPhotos.indexOf(photo)
        _uiState.value = _uiState.value.copy(
            selectedPhoto = photo,
            selectedPhotoIndex = index,
            viewMode = PhotosViewMode.DETAIL
        )
    }

    fun clearSelectedPhoto() {
        _uiState.value = _uiState.value.copy(
            selectedPhoto = null,
            selectedPhotoIndex = -1,
            viewMode = PhotosViewMode.GRID
        )
    }

    fun goToAlbums() {
        _uiState.value = _uiState.value.copy(
            selectedAlbum = null,
            currentPhotos = emptyList(),
            selectedPhoto = null,
            viewMode = PhotosViewMode.ALBUMS
        )
    }

    fun navigateToPreviousPhoto() {
        val currentIndex = _uiState.value.selectedPhotoIndex
        if (currentIndex > 0) {
            val newPhoto = _uiState.value.currentPhotos[currentIndex - 1]
            _uiState.value = _uiState.value.copy(
                selectedPhoto = newPhoto,
                selectedPhotoIndex = currentIndex - 1
            )
        }
    }

    fun navigateToNextPhoto() {
        val currentIndex = _uiState.value.selectedPhotoIndex
        val photos = _uiState.value.currentPhotos
        if (currentIndex < photos.size - 1) {
            val newPhoto = photos[currentIndex + 1]
            _uiState.value = _uiState.value.copy(
                selectedPhoto = newPhoto,
                selectedPhotoIndex = currentIndex + 1
            )
        }
    }

    fun setAsScreensaverAlbum(albumId: String?) {
        viewModelScope.launch {
            photosRepository.setScreensaverAlbum(albumId)
        }
    }

    fun toggleScreensaverAlbum(album: PhotoAlbum) {
        val currentId = _uiState.value.screensaverAlbumId
        if (currentId == album.id) {
            setAsScreensaverAlbum(null) // Remove selection
        } else {
            setAsScreensaverAlbum(album.id)
        }
    }

    fun refreshPhotos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            try {
                photosRepository.refreshPhotos()
                loadAlbums()

                // Reload current album if one is selected
                _uiState.value.selectedAlbum?.let { album ->
                    val photos = photosRepository.getPhotosByAlbum(album.id)
                    _uiState.value = _uiState.value.copy(currentPhotos = photos)
                }

                _uiState.value = _uiState.value.copy(isRefreshing = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = e.message
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class PhotosUiState(
    val albums: List<PhotoAlbum> = emptyList(),
    val currentPhotos: List<Photo> = emptyList(),
    val selectedAlbum: PhotoAlbum? = null,
    val selectedPhoto: Photo? = null,
    val selectedPhotoIndex: Int = -1,
    val screensaverAlbumId: String? = null,
    val viewMode: PhotosViewMode = PhotosViewMode.ALBUMS,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null
)

enum class PhotosViewMode {
    ALBUMS,
    GRID,
    DETAIL
}
