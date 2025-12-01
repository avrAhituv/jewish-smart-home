package com.jewishhome.app.presentation.screens.zmanim

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ZmanimUiState(
    val location: String = "ירושלים",
    val hebrewDate: String = "יום שני כ\"ח כסלו תשפ\"ה",
    val zmanim: List<ZmanItem> = emptyList(),
    val candleLighting: String = "יום שישי 16:22",
    val shabbatEnds: String = "מוצ\"ש 17:32",
    val parasha: String = "מקץ",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ZmanimViewModel @Inject constructor(
    // TODO: Inject ZmanimRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ZmanimUiState())
    val uiState: StateFlow<ZmanimUiState> = _uiState.asStateFlow()

    init {
        loadZmanim()
    }

    private fun loadZmanim() {
        viewModelScope.launch {
            // TODO: Load from repository using KosherJava
            // For now, use placeholder data
            _uiState.value = _uiState.value.copy(
                zmanim = listOf(
                    ZmanItem("עלות השחר", "05:12", isPassed = true),
                    ZmanItem("נץ החמה", "06:32", isPassed = true),
                    ZmanItem("סוז\"ק מג\"א", "08:47", isPassed = true),
                    ZmanItem("סוז\"ק גר\"א", "09:23", isPassed = true),
                    ZmanItem("סוז\"ת", "10:14", isPassed = true),
                    ZmanItem("חצות", "11:37", isPassed = true),
                    ZmanItem("מנחה גדולה", "12:04", isPassed = true),
                    ZmanItem("פלג המנחה", "15:51", isPassed = false),
                    ZmanItem("שקיעה", "16:42", isPassed = false, isCurrent = true),
                    ZmanItem("צאת הכוכבים", "17:12", isPassed = false),
                    ZmanItem("צאת ר\"ת", "17:54", isPassed = false)
                )
            )
        }
    }

    fun refreshZmanim() {
        loadZmanim()
    }

    fun setLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location)
        loadZmanim()
    }
}
