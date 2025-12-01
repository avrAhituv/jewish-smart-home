package com.jewishhome.app.domain.repository

import com.jewishhome.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface CalendarRepository {

    // Events
    suspend fun getEventsForDay(date: LocalDate): List<CalendarEvent>
    suspend fun getEventsForMonth(year: Int, month: Int): List<CalendarEvent>
    suspend fun getEventsForDateRange(startDate: LocalDate, endDate: LocalDate): List<CalendarEvent>
    suspend fun getUpcomingEvents(days: Int = 7): List<CalendarEvent>

    // Jewish holidays
    suspend fun getJewishHolidaysForMonth(year: Int, month: Int): List<JewishHoliday>
    suspend fun getJewishHolidaysForYear(year: Int): List<JewishHoliday>
    suspend fun getNextJewishHoliday(): JewishHoliday?

    // Day and month info
    suspend fun getDayInfo(date: LocalDate): DayInfo
    suspend fun getMonthInfo(year: Int, month: Int): MonthInfo

    // Parasha
    suspend fun getParasha(date: LocalDate): String?
    suspend fun getNextParasha(): Pair<LocalDate, String>?

    // Sync
    suspend fun syncCalendars(): Boolean
    fun getAvailableCalendars(): Flow<List<CalendarInfo>>
    suspend fun setCalendarsToSync(calendarIds: List<String>)

    // Today's info
    suspend fun getTodayEvent(): String?
}

data class CalendarInfo(
    val id: String,
    val name: String,
    val accountName: String,
    val color: Int,
    val isSelected: Boolean
)
