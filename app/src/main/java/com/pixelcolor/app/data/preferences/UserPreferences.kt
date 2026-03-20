package com.pixelcolor.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pixel_color_settings")

class UserPreferences(private val context: Context) {

    companion object {
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val AUTO_ZOOM = booleanPreferencesKey("auto_zoom")
        val COLOR_BLIND_MODE = booleanPreferencesKey("color_blind_mode")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DAILY_STREAK = intPreferencesKey("daily_streak")
        val LAST_PLAYED_DATE = stringPreferencesKey("last_played_date")
        val TOTAL_PUZZLES_COMPLETED = intPreferencesKey("total_completed")
    }

    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { it[SOUND_ENABLED] ?: true }
    val hapticEnabled: Flow<Boolean> = context.dataStore.data.map { it[HAPTIC_ENABLED] ?: true }
    val autoZoom: Flow<Boolean> = context.dataStore.data.map { it[AUTO_ZOOM] ?: true }
    val colorBlindMode: Flow<Boolean> = context.dataStore.data.map { it[COLOR_BLIND_MODE] ?: false }
    val themeMode: Flow<String> = context.dataStore.data.map { it[THEME_MODE] ?: "system" }
    val dailyStreak: Flow<Int> = context.dataStore.data.map { it[DAILY_STREAK] ?: 0 }
    val lastPlayedDate: Flow<String> = context.dataStore.data.map { it[LAST_PLAYED_DATE] ?: "" }
    val totalCompleted: Flow<Int> = context.dataStore.data.map { it[TOTAL_PUZZLES_COMPLETED] ?: 0 }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[SOUND_ENABLED] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { it[HAPTIC_ENABLED] = enabled }
    }

    suspend fun setAutoZoom(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_ZOOM] = enabled }
    }

    suspend fun setColorBlindMode(enabled: Boolean) {
        context.dataStore.edit { it[COLOR_BLIND_MODE] = enabled }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[THEME_MODE] = mode }
    }

    suspend fun updateDailyStreak(date: String) {
        context.dataStore.edit { prefs ->
            val lastDate = prefs[LAST_PLAYED_DATE] ?: ""
            val currentStreak = prefs[DAILY_STREAK] ?: 0
            if (lastDate.isEmpty()) {
                prefs[DAILY_STREAK] = 1
            } else {
                val last = java.time.LocalDate.parse(lastDate)
                val current = java.time.LocalDate.parse(date)
                val daysDiff = java.time.temporal.ChronoUnit.DAYS.between(last, current)
                prefs[DAILY_STREAK] = when {
                    daysDiff == 1L -> currentStreak + 1
                    daysDiff == 0L -> currentStreak
                    else -> 1
                }
            }
            prefs[LAST_PLAYED_DATE] = date
        }
    }

    suspend fun incrementCompleted() {
        context.dataStore.edit { prefs ->
            val current = prefs[TOTAL_PUZZLES_COMPLETED] ?: 0
            prefs[TOTAL_PUZZLES_COMPLETED] = current + 1
        }
    }
}
