package com.jewishhome.app.domain.model

import java.time.LocalDateTime

/**
 * Represents a single halachic time (zman)
 */
data class Zman(
    val type: ZmanType,
    val time: LocalDateTime?,
    val hebrewName: String,
    val englishName: String,
    val description: String? = null
)

/**
 * All supported zmanim types
 */
enum class ZmanType(val hebrewName: String, val englishName: String, val order: Int) {
    ALOT_HASHACHAR("עלות השחר", "Dawn", 1),
    MISHEYAKIR("משיכיר", "Earliest Tallit", 2),
    NETZ_HACHAMA("נץ החמה", "Sunrise", 3),
    SOF_ZMAN_SHMA_MGA("סוז\"ק מג\"א", "Latest Shema (MGA)", 4),
    SOF_ZMAN_SHMA_GRA("סוז\"ק גר\"א", "Latest Shema (GRA)", 5),
    SOF_ZMAN_TEFILA_MGA("סוז\"ת מג\"א", "Latest Tefila (MGA)", 6),
    SOF_ZMAN_TEFILA_GRA("סוז\"ת גר\"א", "Latest Tefila (GRA)", 7),
    CHATZOT_HAYOM("חצות היום", "Midday", 8),
    MINCHA_GEDOLA("מנחה גדולה", "Earliest Mincha", 9),
    MINCHA_KETANA("מנחה קטנה", "Mincha Ketana", 10),
    PLAG_HAMINCHA("פלג המנחה", "Plag HaMincha", 11),
    SHKIA("שקיעה", "Sunset", 12),
    TZAIT_HAKOCHAVIM("צאת הכוכבים", "Nightfall", 13),
    TZAIT_RT("צאת ר\"ת", "Nightfall (RT)", 14),
    CHATZOT_LAYLA("חצות הלילה", "Midnight", 15);

    companion object {
        fun fromOrder(order: Int): ZmanType? = entries.find { it.order == order }
    }
}

/**
 * Daily zmanim collection
 */
data class DailyZmanim(
    val date: LocalDateTime,
    val hebrewDate: HebrewDate,
    val location: GeoLocation,
    val zmanim: List<Zman>,
    val shabbatTimes: ShabbatTimes?
)

/**
 * Shabbat-specific times
 */
data class ShabbatTimes(
    val candleLighting: LocalDateTime?,
    val shabbatEnds: LocalDateTime?,
    val parasha: String?,
    val isShabbat: Boolean,
    val isYomTov: Boolean,
    val holidayName: String? = null
)
