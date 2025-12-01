package com.jewishhome.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.jewishhome.app.domain.model.CandleLightingOffset
import com.jewishhome.app.domain.model.ZmanimCalculationMethod
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        // Location
        private val LOCATION_NAME = stringPreferencesKey("location_name")

        // Zmanim settings
        private val CALCULATION_METHOD = stringPreferencesKey("calculation_method")
        private val CANDLE_LIGHTING_OFFSET = intPreferencesKey("candle_lighting_offset")
        private val SHOW_SECONDS = booleanPreferencesKey("show_seconds")

        // Display settings
        private val USE_24_HOUR_FORMAT = booleanPreferencesKey("use_24_hour_format")
        private val DARK_MODE = stringPreferencesKey("dark_mode") // "light", "dark", "auto"
        private val TEXT_SIZE = stringPreferencesKey("text_size") // "small", "medium", "large"

        // Kiosk settings
        private val PIN_CODE = stringPreferencesKey("pin_code")
        private val KIOSK_MODE = stringPreferencesKey("kiosk_mode") // "lock_task", "device_owner"

        // Screensaver settings
        private val SCREENSAVER_ENABLED = booleanPreferencesKey("screensaver_enabled")
        private val SCREENSAVER_TIMEOUT = intPreferencesKey("screensaver_timeout") // minutes
        private val SCREENSAVER_SHOW_CLOCK = booleanPreferencesKey("screensaver_show_clock")

        // Background settings
        private val BACKGROUND_NAME = stringPreferencesKey("background_name")

        // Nusach
        private val NUSACH = stringPreferencesKey("nusach") // "ashkenaz", "sfard", "edot_hamizrach"

        // Font Size for texts
        private val FONT_SIZE = floatPreferencesKey("font_size")
    }

    // Location
    val locationName: Flow<String> = dataStore.data.map { preferences ->
        preferences[LOCATION_NAME] ?: "Jerusalem"
    }

    suspend fun setLocationName(name: String) {
        dataStore.edit { preferences ->
            preferences[LOCATION_NAME] = name
        }
    }

    // Calculation Method
    val calculationMethod: Flow<ZmanimCalculationMethod> = dataStore.data.map { preferences ->
        when (preferences[CALCULATION_METHOD]) {
            "MGA" -> ZmanimCalculationMethod.MGA
            "BAAL_HATANYA" -> ZmanimCalculationMethod.BAAL_HATANYA
            else -> ZmanimCalculationMethod.GRA
        }
    }

    suspend fun setCalculationMethod(method: ZmanimCalculationMethod) {
        dataStore.edit { preferences ->
            preferences[CALCULATION_METHOD] = method.name
        }
    }

    // Candle Lighting Offset
    val candleLightingOffset: Flow<CandleLightingOffset> = dataStore.data.map { preferences ->
        val minutes = preferences[CANDLE_LIGHTING_OFFSET] ?: 20
        CandleLightingOffset.entries.find { it.minutes == minutes }
            ?: CandleLightingOffset.MINUTES_20
    }

    suspend fun setCandleLightingOffset(offset: CandleLightingOffset) {
        dataStore.edit { preferences ->
            preferences[CANDLE_LIGHTING_OFFSET] = offset.minutes
        }
    }

    // 24 Hour Format
    val use24HourFormat: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[USE_24_HOUR_FORMAT] ?: true
    }

    suspend fun setUse24HourFormat(use24Hour: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_24_HOUR_FORMAT] = use24Hour
        }
    }

    // Dark Mode
    val darkMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[DARK_MODE] ?: "auto"
    }

    suspend fun setDarkMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[DARK_MODE] = mode
        }
    }

    // Text Size
    val textSize: Flow<String> = dataStore.data.map { preferences ->
        preferences[TEXT_SIZE] ?: "medium"
    }

    suspend fun setTextSize(size: String) {
        dataStore.edit { preferences ->
            preferences[TEXT_SIZE] = size
        }
    }

    // PIN Code
    val pinCode: Flow<String> = dataStore.data.map { preferences ->
        preferences[PIN_CODE] ?: "1234"
    }

    suspend fun setPinCode(pin: String) {
        dataStore.edit { preferences ->
            preferences[PIN_CODE] = pin
        }
    }

    // Kiosk Mode
    val kioskMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[KIOSK_MODE] ?: "lock_task"
    }

    suspend fun setKioskMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[KIOSK_MODE] = mode
        }
    }

    // Screensaver
    val screensaverEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SCREENSAVER_ENABLED] ?: true
    }

    suspend fun setScreensaverEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SCREENSAVER_ENABLED] = enabled
        }
    }

    val screensaverTimeout: Flow<Int> = dataStore.data.map { preferences ->
        preferences[SCREENSAVER_TIMEOUT] ?: 5
    }

    suspend fun setScreensaverTimeout(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[SCREENSAVER_TIMEOUT] = minutes
        }
    }

    val screensaverShowClock: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SCREENSAVER_SHOW_CLOCK] ?: true
    }

    suspend fun setScreensaverShowClock(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SCREENSAVER_SHOW_CLOCK] = show
        }
    }

    // Background
    val backgroundName: Flow<String> = dataStore.data.map { preferences ->
        preferences[BACKGROUND_NAME] ?: "jerusalem_gold"
    }

    suspend fun setBackgroundName(name: String) {
        dataStore.edit { preferences ->
            preferences[BACKGROUND_NAME] = name
        }
    }

    // Nusach
    val nusach: Flow<String> = dataStore.data.map { preferences ->
        preferences[NUSACH] ?: "ASHKENAZ"
    }

    suspend fun setNusach(nusach: String) {
        dataStore.edit { preferences ->
            preferences[NUSACH] = nusach
        }
    }

    // Font Size
    val fontSize: Flow<Float> = dataStore.data.map { preferences ->
        preferences[FONT_SIZE] ?: 20f
    }

    suspend fun setFontSize(size: Float) {
        dataStore.edit { preferences ->
            preferences[FONT_SIZE] = size
        }
    }
}
