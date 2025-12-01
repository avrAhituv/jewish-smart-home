package com.jewishhome.app.domain.model

/**
 * Represents a Hebrew calendar date
 */
data class HebrewDate(
    val day: Int,
    val month: HebrewMonth,
    val year: Int,
    val dayOfWeek: HebrewDayOfWeek,
    val isLeapYear: Boolean = false
) {
    /**
     * Returns formatted Hebrew date string
     * e.g., "כ״ח כסלו תשפ״ה"
     */
    fun toHebrewString(): String {
        return "${formatHebrewDay(day)} ${month.hebrewName} ${formatHebrewYear(year)}"
    }

    /**
     * Returns formatted Hebrew date with day of week
     * e.g., "יום שני, כ״ח כסלו תשפ״ה"
     */
    fun toFullHebrewString(): String {
        return "${dayOfWeek.hebrewName}, ${toHebrewString()}"
    }

    companion object {
        private val hebrewNumerals = mapOf(
            1 to "א", 2 to "ב", 3 to "ג", 4 to "ד", 5 to "ה",
            6 to "ו", 7 to "ז", 8 to "ח", 9 to "ט", 10 to "י",
            11 to "י״א", 12 to "י״ב", 13 to "י״ג", 14 to "י״ד", 15 to "ט״ו",
            16 to "ט״ז", 17 to "י״ז", 18 to "י״ח", 19 to "י״ט", 20 to "כ",
            21 to "כ״א", 22 to "כ״ב", 23 to "כ״ג", 24 to "כ״ד", 25 to "כ״ה",
            26 to "כ״ו", 27 to "כ״ז", 28 to "כ״ח", 29 to "כ״ט", 30 to "ל"
        )

        fun formatHebrewDay(day: Int): String = hebrewNumerals[day] ?: day.toString()

        fun formatHebrewYear(year: Int): String {
            // Convert year to Hebrew numerals (simplified for common years)
            val thousands = year / 1000
            val hundreds = (year % 1000) / 100
            val tens = (year % 100) / 10
            val ones = year % 10

            val hundredsMap = mapOf(1 to "ק", 2 to "ר", 3 to "ש", 4 to "ת", 5 to "תק", 6 to "תר", 7 to "תש", 8 to "תת")
            val tensMap = mapOf(1 to "י", 2 to "כ", 3 to "ל", 4 to "מ", 5 to "נ", 6 to "ס", 7 to "ע", 8 to "פ", 9 to "צ")
            val onesMap = mapOf(1 to "א", 2 to "ב", 3 to "ג", 4 to "ד", 5 to "ה", 6 to "ו", 7 to "ז", 8 to "ח", 9 to "ט")

            val result = StringBuilder()
            if (hundreds > 0) result.append(hundredsMap[hundreds] ?: "")
            if (tens > 0) result.append(tensMap[tens] ?: "")
            if (ones > 0) result.append(onesMap[ones] ?: "")

            // Add geresh/gershayim
            return if (result.length > 1) {
                result.insert(result.length - 1, "״").toString()
            } else {
                result.append("׳").toString()
            }
        }
    }
}

/**
 * Hebrew months
 */
enum class HebrewMonth(val hebrewName: String, val englishName: String, val order: Int) {
    TISHREI("תשרי", "Tishrei", 1),
    CHESHVAN("חשון", "Cheshvan", 2),
    KISLEV("כסלו", "Kislev", 3),
    TEVET("טבת", "Tevet", 4),
    SHEVAT("שבט", "Shevat", 5),
    ADAR("אדר", "Adar", 6),
    ADAR_I("אדר א׳", "Adar I", 6),
    ADAR_II("אדר ב׳", "Adar II", 7),
    NISAN("ניסן", "Nisan", 7),
    IYAR("אייר", "Iyar", 8),
    SIVAN("סיון", "Sivan", 9),
    TAMMUZ("תמוז", "Tammuz", 10),
    AV("אב", "Av", 11),
    ELUL("אלול", "Elul", 12);

    companion object {
        fun fromKosherJava(month: Int, isLeapYear: Boolean): HebrewMonth {
            return when (month) {
                7 -> TISHREI
                8 -> CHESHVAN
                9 -> KISLEV
                10 -> TEVET
                11 -> SHEVAT
                12 -> if (isLeapYear) ADAR_I else ADAR
                13 -> ADAR_II
                1 -> NISAN
                2 -> IYAR
                3 -> SIVAN
                4 -> TAMMUZ
                5 -> AV
                6 -> ELUL
                else -> TISHREI
            }
        }
    }
}

/**
 * Hebrew days of week
 */
enum class HebrewDayOfWeek(val hebrewName: String, val englishName: String, val dayNumber: Int) {
    SUNDAY("יום ראשון", "Sunday", 1),
    MONDAY("יום שני", "Monday", 2),
    TUESDAY("יום שלישי", "Tuesday", 3),
    WEDNESDAY("יום רביעי", "Wednesday", 4),
    THURSDAY("יום חמישי", "Thursday", 5),
    FRIDAY("יום שישי", "Friday", 6),
    SHABBAT("שבת", "Shabbat", 7);

    companion object {
        fun fromDayOfWeek(dayOfWeek: java.time.DayOfWeek): HebrewDayOfWeek {
            return when (dayOfWeek) {
                java.time.DayOfWeek.SUNDAY -> SUNDAY
                java.time.DayOfWeek.MONDAY -> MONDAY
                java.time.DayOfWeek.TUESDAY -> TUESDAY
                java.time.DayOfWeek.WEDNESDAY -> WEDNESDAY
                java.time.DayOfWeek.THURSDAY -> THURSDAY
                java.time.DayOfWeek.FRIDAY -> FRIDAY
                java.time.DayOfWeek.SATURDAY -> SHABBAT
            }
        }
    }
}
