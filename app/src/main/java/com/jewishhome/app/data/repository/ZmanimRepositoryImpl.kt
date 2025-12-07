package com.jewishhome.app.data.repository

import com.jewishhome.app.data.local.preferences.UserPreferences
import com.jewishhome.app.domain.model.*
import com.jewishhome.app.domain.repository.ZmanimRepository
import com.kosherjava.zmanim.ZmanimCalendar
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar
import com.kosherjava.zmanim.util.GeoLocation as KosherGeoLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ZmanimRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences
) : ZmanimRepository {

    private val hebrewDateFormatter = HebrewDateFormatter().apply {
        isHebrewFormat = true
    }

    override suspend fun getDailyZmanim(
        date: LocalDate,
        location: GeoLocation,
        calculationMethod: ZmanimCalculationMethod
    ): DailyZmanim = withContext(Dispatchers.Default) {
        val zmanimCalendar = createZmanimCalendar(date, location)
        val jewishCalendar = createJewishCalendar(date)
        val hebrewDate = convertToHebrewDate(jewishCalendar)

        val zmanim = buildZmanimList(zmanimCalendar, calculationMethod)
        val shabbatTimes = if (isShabbatOrErev(date)) {
            getShabbatTimes(date, location, CandleLightingOffset.MINUTES_20)
        } else null

        DailyZmanim(
            date = date.atStartOfDay(),
            hebrewDate = hebrewDate,
            location = location,
            zmanim = zmanim,
            shabbatTimes = shabbatTimes
        )
    }

    override suspend fun getHebrewDate(date: LocalDate): HebrewDate = withContext(Dispatchers.Default) {
        val jewishCalendar = createJewishCalendar(date)
        convertToHebrewDate(jewishCalendar)
    }

    override suspend fun getShabbatTimes(
        date: LocalDate,
        location: GeoLocation,
        candleLightingOffset: CandleLightingOffset
    ): ShabbatTimes = withContext(Dispatchers.Default) {
        // Find the Friday
        var fridayDate = date
        while (fridayDate.dayOfWeek != DayOfWeek.FRIDAY) {
            fridayDate = if (fridayDate.dayOfWeek == DayOfWeek.SATURDAY) {
                fridayDate.minusDays(1)
            } else {
                fridayDate.plusDays(1)
            }
        }

        val saturdayDate = fridayDate.plusDays(1)

        val fridayCalendar = createZmanimCalendar(fridayDate, location)
        val saturdayCalendar = createZmanimCalendar(saturdayDate, location)
        val jewishCalendar = createJewishCalendar(saturdayDate)

        val sunset = fridayCalendar.sunset
        val candleLighting = sunset?.let {
            Calendar.getInstance().apply {
                time = it
                add(Calendar.MINUTE, -candleLightingOffset.minutes)
            }.time
        }

        val shabbatEnds = saturdayCalendar.tzais72

        ShabbatTimes(
            candleLighting = candleLighting?.toLocalDateTime(),
            shabbatEnds = shabbatEnds?.toLocalDateTime(),
            parasha = getParashaName(jewishCalendar),
            isShabbat = date.dayOfWeek == DayOfWeek.SATURDAY,
            isYomTov = jewishCalendar.isYomTov,
            holidayName = if (jewishCalendar.isYomTov) jewishCalendar.yomTovIndex.toString() else null
        )
    }

    override suspend fun getNextZman(
        location: GeoLocation,
        calculationMethod: ZmanimCalculationMethod
    ): Zman? = withContext(Dispatchers.Default) {
        // Use device's current time
        val now = LocalDateTime.now()
        val today = LocalDate.now()
        val zmanimCalendar = createZmanimCalendar(today, location)
        val zmanim = buildZmanimList(zmanimCalendar, calculationMethod)

        zmanim.firstOrNull { zman ->
            zman.time?.isAfter(now) == true
        }
    }

    override suspend fun getParasha(date: LocalDate): String? = withContext(Dispatchers.Default) {
        val jewishCalendar = createJewishCalendar(date)
        getParashaName(jewishCalendar)
    }

    override fun getCurrentLocation(): Flow<GeoLocation> {
        return userPreferences.locationName.map { name ->
            GeoLocation.ALL_LOCATIONS.find { it.name == name }
                ?: GeoLocation.JERUSALEM
        }
    }

    override suspend fun saveLocation(location: GeoLocation) {
        userPreferences.setLocationName(location.name)
    }

    override fun getAvailableLocations(): List<GeoLocation> = GeoLocation.ALL_LOCATIONS

    override suspend fun getHebrewDate(): String = withContext(Dispatchers.Default) {
        val jewishCalendar = createJewishCalendar(LocalDate.now())
        val hebrewDate = convertToHebrewDate(jewishCalendar)
        hebrewDate.toHebrewString()
    }

    override suspend fun getNextZman(): ZmanInfo? = withContext(Dispatchers.Default) {
        val location = GeoLocation.JERUSALEM // Default for now
        val zman = getNextZman(location, ZmanimCalculationMethod.GRA)
        zman?.let { z ->
            z.time?.let { time ->
                ZmanInfo(
                    name = z.hebrewName,
                    timeFormatted = String.format("%02d:%02d", time.hour, time.minute),
                    time = time
                )
            }
        }
    }

    // Helper functions

    private fun createZmanimCalendar(date: LocalDate, location: GeoLocation): ZmanimCalendar {
        val kosherLocation = KosherGeoLocation(
            location.name,
            location.latitude,
            location.longitude,
            location.elevation,
            location.timeZone
        )

        return ZmanimCalendar(kosherLocation).apply {
            calendar.set(date.year, date.monthValue - 1, date.dayOfMonth)
        }
    }

    private fun createJewishCalendar(date: LocalDate): JewishCalendar {
        // JewishCalendar expects: (gregorianYear, gregorianMonth (1-based), gregorianDayOfMonth)
        return JewishCalendar().apply {
            setGregorianDate(date.year, date.monthValue - 1, date.dayOfMonth)
        }
    }

    private fun convertToHebrewDate(jewishCalendar: JewishCalendar): HebrewDate {
        // KosherJava uses 1=Sunday, 7=Saturday
        // Java DayOfWeek uses 1=Monday, 7=Sunday
        // So we need to convert: KosherJava 1-7 -> Java 7,1,2,3,4,5,6
        val javaDayOfWeek = when (jewishCalendar.dayOfWeek) {
            1 -> java.time.DayOfWeek.SUNDAY
            2 -> java.time.DayOfWeek.MONDAY
            3 -> java.time.DayOfWeek.TUESDAY
            4 -> java.time.DayOfWeek.WEDNESDAY
            5 -> java.time.DayOfWeek.THURSDAY
            6 -> java.time.DayOfWeek.FRIDAY
            7 -> java.time.DayOfWeek.SATURDAY
            else -> java.time.DayOfWeek.SUNDAY
        }
        
        return HebrewDate(
            day = jewishCalendar.jewishDayOfMonth,
            month = HebrewMonth.fromKosherJava(jewishCalendar.jewishMonth, jewishCalendar.isJewishLeapYear),
            year = jewishCalendar.jewishYear,
            dayOfWeek = HebrewDayOfWeek.fromDayOfWeek(javaDayOfWeek),
            isLeapYear = jewishCalendar.isJewishLeapYear
        )
    }

    private fun buildZmanimList(
        calendar: ZmanimCalendar,
        method: ZmanimCalculationMethod
    ): List<Zman> {
        return listOfNotNull(
            createZman(ZmanType.ALOT_HASHACHAR, calendar.alos72),
            createZman(ZmanType.MISHEYAKIR, calendar.alos72), // Using alos72 as approximation
            createZman(ZmanType.NETZ_HACHAMA, calendar.sunrise),
            when (method) {
                ZmanimCalculationMethod.MGA -> createZman(ZmanType.SOF_ZMAN_SHMA_MGA, calendar.sofZmanShmaMGA)
                else -> null
            },
            createZman(ZmanType.SOF_ZMAN_SHMA_GRA, calendar.sofZmanShmaGRA),
            when (method) {
                ZmanimCalculationMethod.MGA -> createZman(ZmanType.SOF_ZMAN_TEFILA_MGA, calendar.sofZmanTfilaMGA)
                else -> null
            },
            createZman(ZmanType.SOF_ZMAN_TEFILA_GRA, calendar.sofZmanTfilaGRA),
            createZman(ZmanType.CHATZOT_HAYOM, calendar.chatzos),
            createZman(ZmanType.MINCHA_GEDOLA, calendar.minchaGedola),
            createZman(ZmanType.MINCHA_KETANA, calendar.minchaKetana),
            createZman(ZmanType.PLAG_HAMINCHA, calendar.plagHamincha),
            createZman(ZmanType.SHKIA, calendar.sunset),
            createZman(ZmanType.TZAIT_HAKOCHAVIM, calendar.tzais),
            createZman(ZmanType.TZAIT_RT, calendar.tzais72)
        ).sortedBy { it.type.order }
    }

    private fun createZman(type: ZmanType, time: Date?): Zman? {
        return time?.let {
            Zman(
                type = type,
                time = it.toLocalDateTime(),
                hebrewName = type.hebrewName,
                englishName = type.englishName
            )
        }
    }

    private fun getParashaName(jewishCalendar: JewishCalendar): String? {
        val formatter = HebrewDateFormatter().apply {
            isHebrewFormat = true
        }
        return try {
            formatter.formatParsha(jewishCalendar)?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }

    private fun isShabbatOrErev(date: LocalDate): Boolean {
        return date.dayOfWeek == DayOfWeek.FRIDAY || date.dayOfWeek == DayOfWeek.SATURDAY
    }

    private fun Date.toLocalDateTime(): LocalDateTime {
        // Use device's system time zone
        return this.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
    }
}
