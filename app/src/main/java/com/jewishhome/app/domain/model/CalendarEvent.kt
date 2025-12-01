package com.jewishhome.app.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

data class CalendarEvent(
    val id: String,
    val title: String,
    val description: String?,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val isAllDay: Boolean,
    val location: String?,
    val calendarId: String,
    val calendarName: String,
    val color: Int,
    val eventType: EventType = EventType.REGULAR
)

enum class EventType {
    REGULAR,        // Regular calendar event
    JEWISH_HOLIDAY, // Jewish holiday (Hebcal)
    SHABBAT,        // Shabbat
    BIRTHDAY,       // Birthday
    ANNIVERSARY,    // Anniversary
    REMINDER        // Reminder
}

data class JewishHoliday(
    val date: LocalDate,
    val hebrewDate: HebrewDate,
    val name: String,
    val nameHebrew: String,
    val category: HolidayCategory,
    val yomTov: Boolean,
    val candleLighting: String?,
    val havdalah: String?
)

enum class HolidayCategory {
    MAJOR,      // Major holidays (Pesach, Sukkot, etc.)
    MINOR,      // Minor holidays (Chanukah, Purim, etc.)
    FAST,       // Fast days
    ROSH_CHODESH, // New month
    SHABBAT,    // Shabbat
    OTHER       // Other
}

data class DayInfo(
    val gregorianDate: LocalDate,
    val hebrewDate: HebrewDate,
    val events: List<CalendarEvent>,
    val holidays: List<JewishHoliday>,
    val parasha: String?,
    val isShabbat: Boolean,
    val isYomTov: Boolean,
    val candleLighting: String?,
    val havdalah: String?
)

data class MonthInfo(
    val gregorianMonth: Int,
    val gregorianYear: Int,
    val hebrewMonth: HebrewMonth,
    val hebrewYear: Int,
    val days: List<DayInfo>
)
