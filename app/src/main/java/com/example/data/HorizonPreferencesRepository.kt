package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AtmosphereTheme
import com.example.model.ClockStyle
import com.example.model.HorizonPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.horizonDataStore: DataStore<Preferences> by preferencesDataStore(name = "horizon_prefs")

class HorizonPreferencesRepository(private val context: Context) {

    private object Keys {
        val IMMERSIVE_FULLSCREEN = booleanPreferencesKey("immersive_fullscreen")
        val ATMOSPHERE_THEME = stringPreferencesKey("atmosphere_theme")
        val CLOCK_STYLE = stringPreferencesKey("clock_style")
        val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val FOCUS_MINUTES = intPreferencesKey("focus_minutes")
        val COMPLETED_SESSIONS = intPreferencesKey("completed_sessions")
    }

    val preferencesFlow: Flow<HorizonPreferences> = context.horizonDataStore.data.map { prefs ->
        HorizonPreferences(
            isImmersiveFullScreen = prefs[Keys.IMMERSIVE_FULLSCREEN] ?: true,
            atmosphereTheme = AtmosphereTheme.fromId(prefs[Keys.ATMOSPHERE_THEME]),
            clockStyle = prefs[Keys.CLOCK_STYLE]?.let { name ->
                ClockStyle.entries.firstOrNull { it.name == name }
            } ?: ClockStyle.SOLAR_RING,
            is24HourFormat = prefs[Keys.IS_24_HOUR] ?: false,
            showSeconds = prefs[Keys.SHOW_SECONDS] ?: true,
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: true,
            focusMinutes = prefs[Keys.FOCUS_MINUTES] ?: 25,
            completedFocusSessions = prefs[Keys.COMPLETED_SESSIONS] ?: 0
        )
    }

    suspend fun setImmersiveFullScreen(enabled: Boolean) {
        context.horizonDataStore.edit { it[Keys.IMMERSIVE_FULLSCREEN] = enabled }
    }

    suspend fun setAtmosphereTheme(theme: AtmosphereTheme) {
        context.horizonDataStore.edit { it[Keys.ATMOSPHERE_THEME] = theme.id }
    }

    suspend fun setClockStyle(style: ClockStyle) {
        context.horizonDataStore.edit { it[Keys.CLOCK_STYLE] = style.name }
    }

    suspend fun set24HourFormat(enabled: Boolean) {
        context.horizonDataStore.edit { it[Keys.IS_24_HOUR] = enabled }
    }

    suspend fun setShowSeconds(enabled: Boolean) {
        context.horizonDataStore.edit { it[Keys.SHOW_SECONDS] = enabled }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.horizonDataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    }

    suspend fun setFocusMinutes(minutes: Int) {
        context.horizonDataStore.edit { it[Keys.FOCUS_MINUTES] = minutes.coerceIn(1, 120) }
    }

    suspend fun incrementCompletedSessions() {
        context.horizonDataStore.edit { prefs ->
            val current = prefs[Keys.COMPLETED_SESSIONS] ?: 0
            prefs[Keys.COMPLETED_SESSIONS] = current + 1
        }
    }
}
