package com.jewishhome.app.presentation.screens.screensaver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewishhome.app.data.local.preferences.UserPreferences
import com.jewishhome.app.domain.model.Photo
import com.jewishhome.app.domain.model.ZmanInfo
import com.jewishhome.app.domain.repository.PhotosRepository
import com.jewishhome.app.domain.repository.ZmanimRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ScreensaverViewModel @Inject constructor(
    private val photosRepository: PhotosRepository,
    private val zmanimRepository: ZmanimRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreensaverUiState())
    val uiState: StateFlow<ScreensaverUiState> = _uiState.asStateFlow()

    private var photoIndex = 0
    private var photos = emptyList<Photo>()

    init {
        loadPhotos()
        startClock()
        loadHebrewDate()
        loadNextZman()
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            userPreferences.screensaverShowClock.collect { showClock ->
                _uiState.value = _uiState.value.copy(showClock = showClock)
            }
        }
    }

    private fun loadPhotos() {
        viewModelScope.launch {
            photosRepository.getScreensaverPhotos().collect { screensaverPhotos ->
                photos = screensaverPhotos.shuffled()
                if (photos.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        currentPhoto = photos[0],
                        hasPhotos = true
                    )
                    startPhotoRotation()
                } else {
                    _uiState.value = _uiState.value.copy(hasPhotos = false)
                }
            }
        }
    }

    private fun startPhotoRotation() {
        viewModelScope.launch {
            while (true) {
                delay(PHOTO_ROTATION_INTERVAL)
                if (photos.isNotEmpty()) {
                    photoIndex = (photoIndex + 1) % photos.size
                    _uiState.value = _uiState.value.copy(
                        currentPhoto = photos[photoIndex],
                        photoTransition = !_uiState.value.photoTransition
                    )
                }
            }
        }
    }

    private fun startClock() {
        viewModelScope.launch {
            while (true) {
                // Use Israel time zone
                val now = LocalDateTime.now(java.time.ZoneId.of("Asia/Jerusalem"))
                val time24 = now.format(DateTimeFormatter.ofPattern("HH:mm"))
                val time12 = now.format(DateTimeFormatter.ofPattern("h:mm a"))
                val seconds = now.format(DateTimeFormatter.ofPattern(":ss"))

                _uiState.value = _uiState.value.copy(
                    currentTime = time24,
                    currentSeconds = seconds
                )
                delay(1000)
            }
        }
    }

    private fun loadHebrewDate() {
        viewModelScope.launch {
            try {
                val hebrewDate = zmanimRepository.getHebrewDate()
                _uiState.value = _uiState.value.copy(hebrewDate = hebrewDate)
            } catch (e: Exception) {
                // Use fallback
            }
        }
    }

    private fun loadNextZman() {
        viewModelScope.launch {
            try {
                val nextZman = zmanimRepository.getNextZman()
                _uiState.value = _uiState.value.copy(nextZman = nextZman)
            } catch (e: Exception) {
                // Continue without next zman
            }
        }

        // Refresh every minute
        viewModelScope.launch {
            while (true) {
                delay(60000)
                try {
                    val nextZman = zmanimRepository.getNextZman()
                    val hebrewDate = zmanimRepository.getHebrewDate()
                    _uiState.value = _uiState.value.copy(
                        nextZman = nextZman,
                        hebrewDate = hebrewDate
                    )
                } catch (e: Exception) {
                    // Continue
                }
            }
        }
    }

    companion object {
        private const val PHOTO_ROTATION_INTERVAL = 10000L // 10 seconds
    }
}

data class ScreensaverUiState(
    val currentTime: String = "",
    val currentSeconds: String = "",
    val hebrewDate: String = "",
    val nextZman: ZmanInfo? = null,
    val currentPhoto: Photo? = null,
    val hasPhotos: Boolean = false,
    val showClock: Boolean = true,
    val photoTransition: Boolean = false
)
