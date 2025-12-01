package com.jewishhome.app.domain.repository

import com.jewishhome.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for zmanim calculations
 */
interface ZmanimRepository {

    /**
     * Get daily zmanim for a specific date and location
     */
    suspend fun getDailyZmanim(
        date: LocalDate,
        location: GeoLocation,
        calculationMethod: ZmanimCalculationMethod = ZmanimCalculationMethod.GRA
    ): DailyZmanim

    /**
     * Get Hebrew date for a specific date
     */
    suspend fun getHebrewDate(date: LocalDate): HebrewDate

    /**
     * Get Shabbat times for the current/upcoming Shabbat
     */
    suspend fun getShabbatTimes(
        date: LocalDate,
        location: GeoLocation,
        candleLightingOffset: CandleLightingOffset = CandleLightingOffset.MINUTES_20
    ): ShabbatTimes

    /**
     * Get the next upcoming zman from now
     */
    suspend fun getNextZman(
        location: GeoLocation,
        calculationMethod: ZmanimCalculationMethod = ZmanimCalculationMethod.GRA
    ): Zman?

    /**
     * Get parasha for the upcoming Shabbat
     */
    suspend fun getParasha(date: LocalDate): String?

    /**
     * Get current location from saved preferences
     */
    fun getCurrentLocation(): Flow<GeoLocation>

    /**
     * Save location to preferences
     */
    suspend fun saveLocation(location: GeoLocation)

    /**
     * Get all available locations
     */
    fun getAvailableLocations(): List<GeoLocation>

    /**
     * Get Hebrew date string for today (convenience method)
     */
    suspend fun getHebrewDate(): String

    /**
     * Get next zman as ZmanInfo for display (convenience method)
     */
    suspend fun getNextZman(): ZmanInfo?
}
