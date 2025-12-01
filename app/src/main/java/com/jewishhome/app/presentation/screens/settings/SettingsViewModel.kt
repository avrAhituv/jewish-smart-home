package com.jewishhome.app.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewishhome.app.data.local.preferences.UserPreferences
import com.jewishhome.app.domain.model.CandleLightingOffset
import com.jewishhome.app.domain.model.GeoLocation
import com.jewishhome.app.domain.model.ZmanimCalculationMethod
import com.jewishhome.app.domain.repository.ZmanimRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val zmanimRepository: ZmanimRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    val availableLocations = zmanimRepository.getAvailableLocations()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Collect all preferences
            launch {
                userPreferences.calculationMethod.collect { method ->
                    _uiState.value = _uiState.value.copy(calculationMethod = method)
                }
            }
            launch {
                userPreferences.candleLightingOffset.collect { offset ->
                    _uiState.value = _uiState.value.copy(candleLightingOffset = offset)
                }
            }
            launch {
                userPreferences.use24HourFormat.collect { use24Hour ->
                    _uiState.value = _uiState.value.copy(use24HourFormat = use24Hour)
                }
            }
            launch {
                userPreferences.darkMode.collect { mode ->
                    _uiState.value = _uiState.value.copy(darkMode = mode)
                }
            }
            launch {
                userPreferences.textSize.collect { size ->
                    _uiState.value = _uiState.value.copy(textSize = size)
                }
            }
            launch {
                userPreferences.pinCode.collect { pin ->
                    _uiState.value = _uiState.value.copy(pinCode = pin)
                }
            }
            launch {
                userPreferences.kioskMode.collect { mode ->
                    _uiState.value = _uiState.value.copy(kioskMode = mode)
                }
            }
            launch {
                userPreferences.screensaverEnabled.collect { enabled ->
                    _uiState.value = _uiState.value.copy(screensaverEnabled = enabled)
                }
            }
            launch {
                userPreferences.screensaverTimeout.collect { timeout ->
                    _uiState.value = _uiState.value.copy(screensaverTimeout = timeout)
                }
            }
            launch {
                userPreferences.screensaverShowClock.collect { show ->
                    _uiState.value = _uiState.value.copy(screensaverShowClock = show)
                }
            }
            launch {
                userPreferences.backgroundName.collect { name ->
                    _uiState.value = _uiState.value.copy(backgroundName = name)
                }
            }
            launch {
                userPreferences.nusach.collect { nusach ->
                    _uiState.value = _uiState.value.copy(nusach = nusach)
                }
            }
            launch {
                userPreferences.fontSize.collect { size ->
                    _uiState.value = _uiState.value.copy(fontSize = size)
                }
            }
            launch {
                zmanimRepository.getCurrentLocation().collect { location ->
                    _uiState.value = _uiState.value.copy(selectedLocation = location)
                }
            }
        }
    }

    fun setCalculationMethod(method: ZmanimCalculationMethod) {
        viewModelScope.launch {
            userPreferences.setCalculationMethod(method)
        }
    }

    fun setCandleLightingOffset(offset: CandleLightingOffset) {
        viewModelScope.launch {
            userPreferences.setCandleLightingOffset(offset)
        }
    }

    fun setUse24HourFormat(use24Hour: Boolean) {
        viewModelScope.launch {
            userPreferences.setUse24HourFormat(use24Hour)
        }
    }

    fun setDarkMode(mode: String) {
        viewModelScope.launch {
            userPreferences.setDarkMode(mode)
        }
    }

    fun setTextSize(size: String) {
        viewModelScope.launch {
            userPreferences.setTextSize(size)
        }
    }

    fun setPinCode(pin: String) {
        viewModelScope.launch {
            userPreferences.setPinCode(pin)
        }
    }

    fun setKioskMode(mode: String) {
        viewModelScope.launch {
            userPreferences.setKioskMode(mode)
        }
    }

    fun setScreensaverEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setScreensaverEnabled(enabled)
        }
    }

    fun setScreensaverTimeout(minutes: Int) {
        viewModelScope.launch {
            userPreferences.setScreensaverTimeout(minutes)
        }
    }

    fun setScreensaverShowClock(show: Boolean) {
        viewModelScope.launch {
            userPreferences.setScreensaverShowClock(show)
        }
    }

    fun setBackgroundName(name: String) {
        viewModelScope.launch {
            userPreferences.setBackgroundName(name)
        }
    }

    fun setNusach(nusach: String) {
        viewModelScope.launch {
            userPreferences.setNusach(nusach)
        }
    }

    fun setFontSize(size: Float) {
        viewModelScope.launch {
            userPreferences.setFontSize(size)
        }
    }

    fun setLocation(location: GeoLocation) {
        viewModelScope.launch {
            zmanimRepository.saveLocation(location)
        }
    }

    fun selectCategory(category: SettingsCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }
}

data class SettingsUiState(
    val selectedCategory: SettingsCategory? = null,
    val selectedLocation: GeoLocation = GeoLocation.JERUSALEM,
    val calculationMethod: ZmanimCalculationMethod = ZmanimCalculationMethod.GRA,
    val candleLightingOffset: CandleLightingOffset = CandleLightingOffset.MINUTES_20,
    val use24HourFormat: Boolean = true,
    val darkMode: String = "auto",
    val textSize: String = "medium",
    val pinCode: String = "1234",
    val kioskMode: String = "lock_task",
    val screensaverEnabled: Boolean = true,
    val screensaverTimeout: Int = 5,
    val screensaverShowClock: Boolean = true,
    val backgroundName: String = "jerusalem_gold",
    val nusach: String = "ASHKENAZ",
    val fontSize: Float = 20f
)

enum class SettingsCategory(val hebrewName: String, val icon: String) {
    LOCATION("מיקום", "location"),
    ZMANIM("זמנים", "schedule"),
    DISPLAY("תצוגה", "display"),
    SCREENSAVER("שומר מסך", "slideshow"),
    TEXTS("טקסטים", "menu_book"),
    KIOSK("מצב קיוסק", "lock")
}
