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
        loadLocation()
        startClock()
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
                val now = LocalDateTime.now()
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

    private fun loadZmanimData() {
        viewModelScope.launch {
            try {
                // Get Hebrew date
                val hebrewDate = zmanimRepository.getHebrewDate(java.time.LocalDate.now())

                // Get next zman
                val nextZman = zmanimRepository.getNextZman(currentLocation)

                val now = LocalDateTime.now()
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

                _uiState.update { state ->
                    state.copy(
                        hebrewDate = hebrewDate.toFullHebrewString(),
                        nextZman = nextZman?.hebrewName ?: "",
                        nextZmanTime = nextZman?.time?.format(timeFormatterShort) ?: "",
                        timeUntilNextZman = timeUntil
                    )
                }
            } catch (e: Exception) {
                // Fallback to placeholder data on error
                _uiState.update { state ->
                    state.copy(
                        hebrewDate = "שגיאה בטעינת התאריך"
                    )
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
