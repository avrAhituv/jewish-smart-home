package com.jewishhome.app.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jewishhome.app.domain.model.*
import com.jewishhome.app.domain.repository.CalendarInfo
import com.jewishhome.app.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadCurrentMonth()
        loadUpcomingEvents()
        observeCalendars()
    }

    private fun loadCurrentMonth() {
        val today = LocalDate.now()
        selectMonth(today.year, today.monthValue)
    }

    private fun observeCalendars() {
        viewModelScope.launch {
            calendarRepository.getAvailableCalendars().collect { calendars ->
                _uiState.value = _uiState.value.copy(availableCalendars = calendars)
            }
        }
    }

    fun selectMonth(year: Int, month: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val monthInfo = calendarRepository.getMonthInfo(year, month)
                val holidays = calendarRepository.getJewishHolidaysForMonth(year, month)

                _uiState.value = _uiState.value.copy(
                    currentMonth = monthInfo,
                    currentMonthHolidays = holidays,
                    selectedYear = year,
                    selectedMonthNumber = month,
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

    fun selectDate(date: LocalDate) {
        viewModelScope.launch {
            try {
                val dayInfo = calendarRepository.getDayInfo(date)
                _uiState.value = _uiState.value.copy(
                    selectedDate = date,
                    selectedDayInfo = dayInfo
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun clearSelectedDate() {
        _uiState.value = _uiState.value.copy(
            selectedDate = null,
            selectedDayInfo = null
        )
    }

    fun goToPreviousMonth() {
        val currentYearMonth = YearMonth.of(_uiState.value.selectedYear, _uiState.value.selectedMonthNumber)
        val previous = currentYearMonth.minusMonths(1)
        selectMonth(previous.year, previous.monthValue)
    }

    fun goToNextMonth() {
        val currentYearMonth = YearMonth.of(_uiState.value.selectedYear, _uiState.value.selectedMonthNumber)
        val next = currentYearMonth.plusMonths(1)
        selectMonth(next.year, next.monthValue)
    }

    fun goToToday() {
        val today = LocalDate.now()
        selectMonth(today.year, today.monthValue)
        selectDate(today)
    }

    private fun loadUpcomingEvents() {
        viewModelScope.launch {
            try {
                val events = calendarRepository.getUpcomingEvents(14)
                val nextHoliday = calendarRepository.getNextJewishHoliday()
                val nextParasha = calendarRepository.getNextParasha()

                _uiState.value = _uiState.value.copy(
                    upcomingEvents = events,
                    nextHoliday = nextHoliday,
                    nextParasha = nextParasha
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun syncCalendars() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val success = calendarRepository.syncCalendars()
                if (success) {
                    loadCurrentMonth()
                    loadUpcomingEvents()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "שגיאה בסנכרון לוחות שנה"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun toggleCalendarSelection(calendarId: String) {
        val current = _uiState.value.availableCalendars
        val selectedIds = current.filter { it.isSelected }.map { it.id }.toMutableList()

        if (calendarId in selectedIds) {
            selectedIds.remove(calendarId)
        } else {
            selectedIds.add(calendarId)
        }

        viewModelScope.launch {
            calendarRepository.setCalendarsToSync(selectedIds)
            // Reload events
            loadUpcomingEvents()
            selectMonth(_uiState.value.selectedYear, _uiState.value.selectedMonthNumber)
        }
    }

    fun selectViewMode(mode: CalendarViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = mode)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class CalendarUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentMonth: MonthInfo? = null,
    val currentMonthHolidays: List<JewishHoliday> = emptyList(),
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonthNumber: Int = LocalDate.now().monthValue,
    val selectedDate: LocalDate? = null,
    val selectedDayInfo: DayInfo? = null,
    val upcomingEvents: List<CalendarEvent> = emptyList(),
    val nextHoliday: JewishHoliday? = null,
    val nextParasha: Pair<LocalDate, String>? = null,
    val availableCalendars: List<CalendarInfo> = emptyList(),
    val viewMode: CalendarViewMode = CalendarViewMode.MONTH
)

enum class CalendarViewMode {
    MONTH,
    WEEK,
    AGENDA
}
