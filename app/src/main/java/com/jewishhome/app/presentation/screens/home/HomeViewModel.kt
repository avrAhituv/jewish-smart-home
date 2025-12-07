package com.jewishhome.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewishhome.app.domain.model.GeoLocation
import com.jewishhome.app.domain.repository.ZmanimRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val currentTime: String = "",
    val hebrewDate: String = "",
    val gregorianDate: String = "",
    val nextZman: String = "",
    val nextZmanTime: String = "",
    val timeUntilNextZman: String = "",
    val todayEvent: String? = null,
    val isPlaying: Boolean = false,
    val currentSongTitle: String = "",
    val currentArtist: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val zmanimRepository: ZmanimRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val timeFormatterShort = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")

    private var currentLocation: GeoLocation = GeoLocation.JERUSALEM

    init {
        android.util.Log.d("HomeViewModel", "HomeViewModel initialized")
        loadLocation()
        startClock()
        startZmanimRefresh()
    }

    private fun loadLocation() {
        viewModelScope.launch {
            currentLocation = zmanimRepository.getCurrentLocation().first()
            loadZmanimData()
        }
    }

    private fun startClock() {
        viewModelScope.launch {
            while (true) {
                // Use Israel time zone with proper offset
                val now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Jerusalem"))
                    .toLocalDateTime()
                _uiState.update { state ->
                    state.copy(
                        currentTime = now.format(timeFormatter),
                        gregorianDate = now.format(dateFormatter)
                    )
                }
                delay(1000)
            }
        }
    }

    private fun startZmanimRefresh() {
        viewModelScope.launch {
            while (true) {
                // Refresh zmanim every 5 minutes
                delay(5 * 60 * 1000)
                loadZmanimData()
            }
        }
    }

    private fun loadZmanimData() {
        android.util.Log.d("HomeViewModel", "loadZmanimData started")
        viewModelScope.launch {
            try {
                // Get Hebrew date
                android.util.Log.d("HomeViewModel", "Getting Hebrew date...")
                val hebrewDate = zmanimRepository.getHebrewDate(java.time.LocalDate.now())
                android.util.Log.d("HomeViewModel", "Hebrew date: ${hebrewDate.toFullHebrewString()}")

                // Get next zman
                android.util.Log.d("HomeViewModel", "Getting next zman for location: ${currentLocation.hebrewName}")
                val nextZman = zmanimRepository.getNextZman(currentLocation)
                android.util.Log.d("HomeViewModel", "Next zman: ${nextZman?.hebrewName}")

                // Use Israel time zone with proper offset
                val now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Jerusalem"))
                    .toLocalDateTime()
                val timeUntil = nextZman?.time?.let { zmanTime ->
                    val duration = Duration.between(now, zmanTime)
                    val hours = duration.toHours()
                    val minutes = duration.toMinutes() % 60
                    if (hours > 0) {
                        "(בעוד ${hours}:${String.format("%02d", minutes)})"
                    } else {
                        "(בעוד $minutes דקות)"
                    }
                } ?: ""

                android.util.Log.d("HomeViewModel", "Updating UI state...")
                _uiState.update { state ->
                    state.copy(
                        hebrewDate = hebrewDate.toFullHebrewString(),
                        nextZman = nextZman?.hebrewName ?: "",
                        nextZmanTime = nextZman?.time?.format(timeFormatterShort) ?: "",
                        timeUntilNextZman = timeUntil
                    )
                }
                android.util.Log.d("HomeViewModel", "UI state updated successfully")
            } catch (e: Exception) {
                // Log the error for debugging
                android.util.Log.e("HomeViewModel", "Error loading zmanim data", e)
                // Try to at least show the date even if zmanim fail
                try {
                    val hebrewDate = zmanimRepository.getHebrewDate(java.time.LocalDate.now())
                    _uiState.update { state ->
                        state.copy(
                            hebrewDate = hebrewDate.toFullHebrewString()
                        )
                    }
                } catch (dateError: Exception) {
                    android.util.Log.e("HomeViewModel", "Error loading hebrew date", dateError)
                    _uiState.update { state ->
                        state.copy(
                            hebrewDate = "תאריך עברי"
                        )
                    }
                }
            }
        }
    }

    fun refreshData() {
        loadZmanimData()
    }

    fun togglePlayPause() {
        _uiState.update { state ->
            state.copy(isPlaying = !state.isPlaying)
        }
    }

    fun playNext() {
        // TODO: Implement with music service
    }
}
