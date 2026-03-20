package com.pixelcolor.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pixelcolor.app.data.preferences.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val autoZoom: Boolean = true,
    val colorBlindMode: Boolean = false,
    val themeMode: String = "system"
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = UserPreferences(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.soundEnabled,
                prefs.hapticEnabled,
                prefs.autoZoom,
                prefs.colorBlindMode,
                prefs.themeMode
            ) { sound, haptic, zoom, colorBlind, theme ->
                SettingsUiState(sound, haptic, zoom, colorBlind, theme)
            }.collect { _uiState.value = it }
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setSoundEnabled(enabled) }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setHapticEnabled(enabled) }
    }

    fun setAutoZoom(enabled: Boolean) {
        viewModelScope.launch { prefs.setAutoZoom(enabled) }
    }

    fun setColorBlindMode(enabled: Boolean) {
        viewModelScope.launch { prefs.setColorBlindMode(enabled) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { prefs.setThemeMode(mode) }
    }
}
