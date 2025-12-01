package com.jewishhome.app.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeUiState(
    val currentTime: String = "",
    val hebrewDate: String = "כ\"ח כסלו תשפ\"ה",
    val gregorianDate: String = "",
    val nextZman: String = "שקיעה",
    val nextZmanTime: String = "16:42",
    val todayEvent: String? = null,
    val isPlaying: Boolean = false,
    val currentSongTitle: String = "",
    val currentArtist: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    // TODO: Inject repositories
    // private val zmanimRepository: ZmanimRepository,
    // private val calendarRepository: CalendarRepository,
    // private val musicRepository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")

    init {
        startClock()
        loadInitialData()
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

    private fun loadInitialData() {
        viewModelScope.launch {
            // TODO: Load from repositories
            // For now, use placeholder data
            _uiState.update { state ->
                state.copy(
                    hebrewDate = "יום שני, כ\"ח כסלו תשפ\"ה",
                    nextZman = "שקיעה",
                    nextZmanTime = "16:42 (בעוד 2:10)"
                )
            }
        }
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
