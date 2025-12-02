package com.jewishhome.app.data.repository

import android.content.ContentResolver
import android.content.Context
import android.provider.CalendarContract
import com.jewishhome.app.data.local.preferences.UserPreferences
import com.jewishhome.app.domain.model.*
import com.jewishhome.app.domain.repository.CalendarInfo
import com.jewishhome.app.domain.repository.CalendarRepository
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepositoryImpl @Inject constructor(
    private val context: Context,
    private val userPreferences: UserPreferences
) : CalendarRepository {

    private val contentResolver: ContentResolver = context.contentResolver
    private val calendarsFlow = MutableStateFlow<List<CalendarInfo>>(emptyList())
    private val hebrewFormatter = HebrewDateFormatter().apply {
        isHebrewFormat = true
    }

    override suspend fun getEventsForDay(date: LocalDate): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val startOfDay = date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        queryEvents(startOfDay, endOfDay)
    }

    override suspend fun getEventsForMonth(year: Int, month: Int): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1)

        val startMillis = startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        queryEvents(startMillis, endMillis)
    }

    override suspend fun getEventsForDateRange(startDate: LocalDate, endDate: LocalDate): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val startMillis = startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        queryEvents(startMillis, endMillis)
    }

    override suspend fun getUpcomingEvents(days: Int): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val endDate = today.plusDays(days.toLong())
        getEventsForDateRange(today, endDate)
    }

    private fun queryEvents(startMillis: Long, endMillis: Long): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.CALENDAR_DISPLAY_NAME,
            CalendarContract.Events.CALENDAR_COLOR
        )

        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} < ?"
        val selectionArgs = arrayOf(startMillis.toString(), endMillis.toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
            val descColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION)
            val startColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
            val endColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND)
            val allDayColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events.ALL_DAY)
            val locationColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events.EVENT_LOCATION)
            val calIdColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events.CALENDAR_ID)
            val calNameColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events.CALENDAR_DISPLAY_NAME)
            val colorColumn = cursor.getColumnIndexOrThrow(CalendarContract.Events.CALENDAR_COLOR)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn).toString()
                val title = cursor.getString(titleColumn) ?: "ללא כותרת"
                val description = cursor.getString(descColumn)
                val startTime = cursor.getLong(startColumn)
                val endTime = cursor.getLong(endColumn)
                val allDay = cursor.getInt(allDayColumn) == 1
                val location = cursor.getString(locationColumn)
                val calendarId = cursor.getLong(calIdColumn).toString()
                val calendarName = cursor.getString(calNameColumn) ?: ""
                val color = cursor.getInt(colorColumn)

                events.add(
                    CalendarEvent(
                        id = id,
                        title = title,
                        description = description,
                        startTime = LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(startTime),
                            ZoneId.systemDefault()
                        ),
                        endTime = LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(endTime),
                            ZoneId.systemDefault()
                        ),
                        isAllDay = allDay,
                        location = location,
                        calendarId = calendarId,
                        calendarName = calendarName,
                        color = color
                    )
                )
            }
        }

        return events
    }

    override suspend fun getJewishHolidaysForMonth(year: Int, month: Int): List<JewishHoliday> = withContext(Dispatchers.IO) {
        val holidays = mutableListOf<JewishHoliday>()
        val startDate = LocalDate.of(year, month, 1)
        val daysInMonth = startDate.lengthOfMonth()

        for (day in 1..daysInMonth) {
            val date = LocalDate.of(year, month, day)
            val jewishCal = JewishCalendar(date.year, date.monthValue, date.dayOfMonth)

            if (jewishCal.isYomTov || jewishCal.isChanukah || jewishCal.isPurim ||
                jewishCal.isRoshChodesh || jewishCal.isTaanis || jewishCal.isErevYomTov) {

                val hebrewDate = HebrewDate(
                    day = jewishCal.jewishDayOfMonth,
                    month = HebrewMonth.entries.getOrNull(jewishCal.jewishMonth - 1) ?: HebrewMonth.NISAN,
                    year = jewishCal.jewishYear,
                    dayOfWeek = HebrewDayOfWeek.fromDayOfWeek(date.dayOfWeek)
                )

                val holidayName = getHolidayName(jewishCal)
                val category = getHolidayCategory(jewishCal)

                holidays.add(
                    JewishHoliday(
                        date = date,
                        hebrewDate = hebrewDate,
                        name = holidayName,
                        nameHebrew = hebrewFormatter.formatYomTov(jewishCal) ?: holidayName,
                        category = category,
                        yomTov = jewishCal.isYomTov,
                        candleLighting = null,
                        havdalah = null
                    )
                )
            }
        }

        holidays
    }

    override suspend fun getJewishHolidaysForYear(year: Int): List<JewishHoliday> = withContext(Dispatchers.IO) {
        val holidays = mutableListOf<JewishHoliday>()
        for (month in 1..12) {
            holidays.addAll(getJewishHolidaysForMonth(year, month))
        }
        holidays
    }

    override suspend fun getNextJewishHoliday(): JewishHoliday? = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val endDate = today.plusMonths(3)

        var currentDate = today
        while (currentDate.isBefore(endDate)) {
            val jewishCal = JewishCalendar(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth)

            if (jewishCal.isYomTov || jewishCal.isChanukah || jewishCal.isPurim) {
                val hebrewDate = HebrewDate(
                    day = jewishCal.jewishDayOfMonth,
                    month = HebrewMonth.entries.getOrNull(jewishCal.jewishMonth - 1) ?: HebrewMonth.NISAN,
                    year = jewishCal.jewishYear,
                    dayOfWeek = HebrewDayOfWeek.fromDayOfWeek(currentDate.dayOfWeek)
                )

                return@withContext JewishHoliday(
                    date = currentDate,
                    hebrewDate = hebrewDate,
                    name = getHolidayName(jewishCal),
                    nameHebrew = hebrewFormatter.formatYomTov(jewishCal) ?: getHolidayName(jewishCal),
                    category = getHolidayCategory(jewishCal),
                    yomTov = jewishCal.isYomTov,
                    candleLighting = null,
                    havdalah = null
                )
            }
            currentDate = currentDate.plusDays(1)
        }
        null
    }

    override suspend fun getDayInfo(date: LocalDate): DayInfo = withContext(Dispatchers.IO) {
        val jewishCal = JewishCalendar(date.year, date.monthValue, date.dayOfMonth)

        val hebrewDate = HebrewDate(
            day = jewishCal.jewishDayOfMonth,
            month = HebrewMonth.entries.getOrNull(jewishCal.jewishMonth - 1) ?: HebrewMonth.NISAN,
            year = jewishCal.jewishYear,
            dayOfWeek = HebrewDayOfWeek.fromDayOfWeek(date.dayOfWeek)
        )

        val events = getEventsForDay(date)
        val holidays = if (jewishCal.isYomTov || jewishCal.isChanukah || jewishCal.isPurim ||
            jewishCal.isRoshChodesh || jewishCal.isTaanis) {
            listOf(
                JewishHoliday(
                    date = date,
                    hebrewDate = hebrewDate,
                    name = getHolidayName(jewishCal),
                    nameHebrew = hebrewFormatter.formatYomTov(jewishCal) ?: getHolidayName(jewishCal),
                    category = getHolidayCategory(jewishCal),
                    yomTov = jewishCal.isYomTov,
                    candleLighting = null,
                    havdalah = null
                )
            )
        } else emptyList()

        val isShabbat = date.dayOfWeek.value == 6 // Saturday
        val parasha = if (isShabbat) hebrewFormatter.formatParsha(jewishCal) else null

        DayInfo(
            gregorianDate = date,
            hebrewDate = hebrewDate,
            events = events,
            holidays = holidays,
            parasha = parasha,
            isShabbat = isShabbat,
            isYomTov = jewishCal.isYomTov,
            candleLighting = null,
            havdalah = null
        )
    }

    override suspend fun getMonthInfo(year: Int, month: Int): MonthInfo = withContext(Dispatchers.IO) {
        val startDate = LocalDate.of(year, month, 1)
        val daysInMonth = startDate.lengthOfMonth()

        // Get Hebrew month from middle of the month
        val midDate = LocalDate.of(year, month, 15)
        val jewishCal = JewishCalendar(midDate.year, midDate.monthValue, midDate.dayOfMonth)

        val days = (1..daysInMonth).map { day ->
            getDayInfo(LocalDate.of(year, month, day))
        }

        MonthInfo(
            gregorianMonth = month,
            gregorianYear = year,
            hebrewMonth = HebrewMonth.entries.getOrNull(jewishCal.jewishMonth - 1) ?: HebrewMonth.NISAN,
            hebrewYear = jewishCal.jewishYear,
            days = days
        )
    }

    override suspend fun getParasha(date: LocalDate): String? = withContext(Dispatchers.IO) {
        val jewishCal = JewishCalendar(date.year, date.monthValue, date.dayOfMonth)
        hebrewFormatter.formatParsha(jewishCal)
    }

    override suspend fun getNextParasha(): Pair<LocalDate, String>? = withContext(Dispatchers.IO) {
        var currentDate = LocalDate.now()
        // Find next Saturday
        while (currentDate.dayOfWeek.value != 6) {
            currentDate = currentDate.plusDays(1)
        }

        val jewishCal = JewishCalendar(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth)
        val parasha = hebrewFormatter.formatParsha(jewishCal)

        if (parasha != null) {
            Pair(currentDate, parasha)
        } else null
    }

    override suspend fun syncCalendars(): Boolean = withContext(Dispatchers.IO) {
        try {
            val calendars = mutableListOf<CalendarInfo>()

            val projection = arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.CALENDAR_COLOR
            )

            contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                null,
                null,
                "${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                val accountColumn = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
                val colorColumn = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_COLOR)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn).toString()
                    val name = cursor.getString(nameColumn) ?: "ללא שם"
                    val account = cursor.getString(accountColumn) ?: ""
                    val color = cursor.getInt(colorColumn)

                    calendars.add(
                        CalendarInfo(
                            id = id,
                            name = name,
                            accountName = account,
                            color = color,
                            isSelected = true
                        )
                    )
                }
            }

            calendarsFlow.value = calendars
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getAvailableCalendars(): Flow<List<CalendarInfo>> = calendarsFlow

    override suspend fun setCalendarsToSync(calendarIds: List<String>) {
        val current = calendarsFlow.value
        calendarsFlow.value = current.map { cal ->
            cal.copy(isSelected = cal.id in calendarIds)
        }
    }

    override suspend fun getTodayEvent(): String? = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        val jewishCal = JewishCalendar(today.year, today.monthValue, today.dayOfMonth)

        // Check for Jewish holiday
        if (jewishCal.isYomTov || jewishCal.isChanukah || jewishCal.isPurim ||
            jewishCal.isRoshChodesh || jewishCal.isTaanis) {
            return@withContext getHolidayName(jewishCal)
        }

        // Check for regular calendar event
        val events = getEventsForDay(today)
        events.firstOrNull()?.title
    }

    private fun getHolidayName(jewishCal: JewishCalendar): String {
        return when {
            jewishCal.isPesach -> "פסח"
            jewishCal.isShavuos -> "שבועות"
            jewishCal.isRoshHashana -> "ראש השנה"
            jewishCal.isYomKippur -> "יום כיפור"
            jewishCal.isSukkos -> "סוכות"
            jewishCal.isSimchasTorah -> "שמחת תורה"
            jewishCal.isShminiAtzeres -> "שמיני עצרת"
            jewishCal.isChanukah -> "חנוכה"
            jewishCal.isPurim -> "פורים"
            jewishCal.isRoshChodesh -> "ראש חודש"
            jewishCal.isTaanis -> getTaanisName(jewishCal)
            else -> hebrewFormatter.formatYomTov(jewishCal) ?: "חג"
        }
    }

    private fun getTaanisName(jewishCal: JewishCalendar): String {
        // Return generic name for fast days - the formatter will provide the specific name
        return hebrewFormatter.formatYomTov(jewishCal) ?: "תענית"
    }

    private fun getHolidayCategory(jewishCal: JewishCalendar): HolidayCategory {
        return when {
            jewishCal.isYomTov -> HolidayCategory.MAJOR
            jewishCal.isChanukah || jewishCal.isPurim -> HolidayCategory.MINOR
            jewishCal.isTaanis -> HolidayCategory.FAST
            jewishCal.isRoshChodesh -> HolidayCategory.ROSH_CHODESH
            else -> HolidayCategory.OTHER
        }
    }
}
