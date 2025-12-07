package com.jewishhome.app.presentation.screens.zmanim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewishhome.app.domain.model.*
import com.jewishhome.app.domain.repository.ZmanimRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ZmanimUiState(
    val location: String = "ירושלים",
    val hebrewDate: String = "",
    val zmanim: List<ZmanItem> = emptyList(),
    val candleLighting: String = "",
    val shabbatEnds: String = "",
    val parasha: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val availableLocations: List<GeoLocation> = emptyList()
)

@HiltViewModel
class ZmanimViewModel @Inject constructor(
    private val zmanimRepository: ZmanimRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ZmanimUiState())
    val uiState: StateFlow<ZmanimUiState> = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private var currentLocation: GeoLocation = GeoLocation.JERUSALEM

    init {
        loadAvailableLocations()
        observeLocation()
    }

    private fun loadAvailableLocations() {
        _uiState.update { state ->
            state.copy(availableLocations = zmanimRepository.getAvailableLocations())
        }
    }

    private fun observeLocation() {
        viewModelScope.launch {
            zmanimRepository.getCurrentLocation()
                .collect { location ->
                    currentLocation = location
                    _uiState.update { it.copy(location = location.hebrewName) }
                    loadZmanim()
                }
        }
    }

    fun loadZmanim() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val today = LocalDate.now()
                val now = LocalDateTime.now()

                // Get daily zmanim
                val dailyZmanim = zmanimRepository.getDailyZmanim(
                    date = today,
                    location = currentLocation
                )

                // Get Shabbat times
                val shabbatTimes = zmanimRepository.getShabbatTimes(
                    date = today,
                    location = currentLocation
                )

                // Convert to display items
                val displayItems = dailyZmanim.zmanim.map { zman ->
                    val isPassed = zman.time?.isBefore(now) == true
                    val isNext = !isPassed && dailyZmanim.zmanim
                        .filter { it.time?.isAfter(now) == true }
                        .minByOrNull { it.time!! }?.type == zman.type

                    ZmanItem(
                        name = zman.hebrewName,
                        time = zman.time?.format(timeFormatter) ?: "--:--",
                        isPassed = isPassed,
                        isCurrent = isNext
                    )
                }

                _uiState.update { state ->
                    state.copy(
                        hebrewDate = dailyZmanim.hebrewDate.toFullHebrewString(),
                        zmanim = displayItems,
                        candleLighting = formatShabbatTime("יום שישי", shabbatTimes.candleLighting),
                        shabbatEnds = formatShabbatTime("מוצ\"ש", shabbatTimes.shabbatEnds),
                        parasha = shabbatTimes.parasha ?: "",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("ZmanimViewModel", "Error loading zmanim", e)
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = "שגיאה בטעינת הזמנים: ${e.message}"
                    )
                }
            }
        }
    }

    private fun formatShabbatTime(prefix: String, time: LocalDateTime?): String {
        return time?.let { "$prefix ${it.format(timeFormatter)}" } ?: ""
    }

    fun refreshZmanim() {
        loadZmanim()
    }

    fun setLocation(location: GeoLocation) {
        viewModelScope.launch {
            zmanimRepository.saveLocation(location)
        }
    }
}
