package com.jewishhome.app.domain.model

import java.util.TimeZone

/**
 * Represents a geographic location for zmanim calculations
 */
data class GeoLocation(
    val name: String,
    val hebrewName: String,
    val latitude: Double,
    val longitude: Double,
    val elevation: Double = 0.0,
    val timeZone: TimeZone = TimeZone.getDefault()
) {
    companion object {
        // Predefined locations in Israel
        val JERUSALEM = GeoLocation(
            name = "Jerusalem",
            hebrewName = "ירושלים",
            latitude = 31.7683,
            longitude = 35.2137,
            elevation = 800.0,
            timeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        )

        val TEL_AVIV = GeoLocation(
            name = "Tel Aviv",
            hebrewName = "תל אביב",
            latitude = 32.0853,
            longitude = 34.7818,
            elevation = 5.0,
            timeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        )

        val HAIFA = GeoLocation(
            name = "Haifa",
            hebrewName = "חיפה",
            latitude = 32.7940,
            longitude = 34.9896,
            elevation = 100.0,
            timeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        )

        val BEER_SHEVA = GeoLocation(
            name = "Beer Sheva",
            hebrewName = "באר שבע",
            latitude = 31.2518,
            longitude = 34.7913,
            elevation = 280.0,
            timeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        )

        val BNEI_BRAK = GeoLocation(
            name = "Bnei Brak",
            hebrewName = "בני ברק",
            latitude = 32.0833,
            longitude = 34.8333,
            elevation = 30.0,
            timeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        )

        val PETAH_TIKVA = GeoLocation(
            name = "Petah Tikva",
            hebrewName = "פתח תקווה",
            latitude = 32.0867,
            longitude = 34.8867,
            elevation = 50.0,
            timeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        )

        val ASHDOD = GeoLocation(
            name = "Ashdod",
            hebrewName = "אשדוד",
            latitude = 31.8044,
            longitude = 34.6553,
            elevation = 20.0,
            timeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        )

        val NETANYA = GeoLocation(
            name = "Netanya",
            hebrewName = "נתניה",
            latitude = 32.3286,
            longitude = 34.8569,
            elevation = 35.0,
            timeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        )

        val EILAT = GeoLocation(
            name = "Eilat",
            hebrewName = "אילת",
            latitude = 29.5569,
            longitude = 34.9517,
            elevation = 15.0,
            timeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        )

        val TZFAT = GeoLocation(
            name = "Tzfat",
            hebrewName = "צפת",
            latitude = 32.9646,
            longitude = 35.4960,
            elevation = 900.0,
            timeZone = TimeZone.getTimeZone("Asia/Jerusalem")
        )

        // Major world cities
        val NEW_YORK = GeoLocation(
            name = "New York",
            hebrewName = "ניו יורק",
            latitude = 40.7128,
            longitude = -74.0060,
            elevation = 10.0,
            timeZone = TimeZone.getTimeZone("America/New_York")
        )

        val LOS_ANGELES = GeoLocation(
            name = "Los Angeles",
            hebrewName = "לוס אנג׳לס",
            latitude = 34.0522,
            longitude = -118.2437,
            elevation = 71.0,
            timeZone = TimeZone.getTimeZone("America/Los_Angeles")
        )

        val LONDON = GeoLocation(
            name = "London",
            hebrewName = "לונדון",
            latitude = 51.5074,
            longitude = -0.1278,
            elevation = 11.0,
            timeZone = TimeZone.getTimeZone("Europe/London")
        )

        val PARIS = GeoLocation(
            name = "Paris",
            hebrewName = "פריז",
            latitude = 48.8566,
            longitude = 2.3522,
            elevation = 35.0,
            timeZone = TimeZone.getTimeZone("Europe/Paris")
        )

        /**
         * All predefined locations
         */
        val ALL_LOCATIONS = listOf(
            JERUSALEM, TEL_AVIV, HAIFA, BEER_SHEVA, BNEI_BRAK,
            PETAH_TIKVA, ASHDOD, NETANYA, EILAT, TZFAT,
            NEW_YORK, LOS_ANGELES, LONDON, PARIS
        )

        /**
         * Israel locations only
         */
        val ISRAEL_LOCATIONS = listOf(
            JERUSALEM, TEL_AVIV, HAIFA, BEER_SHEVA, BNEI_BRAK,
            PETAH_TIKVA, ASHDOD, NETANYA, EILAT, TZFAT
        )
    }
}

/**
 * Calculation method for zmanim
 */
enum class ZmanimCalculationMethod(val hebrewName: String, val description: String) {
    GRA("גר״א", "הגאון רבי אליהו מווילנא"),
    MGA("מג״א", "מגן אברהם"),
    BAAL_HATANYA("בעל התניא", "שיטת בעל התניא")
}

/**
 * Candle lighting time offset (minutes before sunset)
 */
enum class CandleLightingOffset(val minutes: Int, val description: String) {
    MINUTES_18(18, "18 דקות"),
    MINUTES_20(20, "20 דקות"),
    MINUTES_22(22, "22 דקות"),
    MINUTES_30(30, "30 דקות"),
    MINUTES_40(40, "40 דקות - ירושלים")
}
