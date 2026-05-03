package com.cinetrack.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinetrack.domain.model.*
import com.cinetrack.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.getSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun updateTheme(theme: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateTheme(theme.name)
        }
    }

    fun updateAccentColor(color: String) {
        viewModelScope.launch {
            settingsRepository.updateAccentColor(color)
        }
    }

    fun updateGridColumns(columns: Int) {
        viewModelScope.launch {
            settingsRepository.updateGridColumns(columns)
        }
    }

    fun updateCardStyle(style: CardStyle) {
        viewModelScope.launch {
            settingsRepository.updateCardStyle(style.name)
        }
    }

    fun updateRatingScale(scale: RatingScale) {
        viewModelScope.launch {
            settingsRepository.updateRatingScale(scale.name)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateNotificationsEnabled(enabled)
        }
    }

    fun updateQuietHours(start: Int?, end: Int?) {
        viewModelScope.launch {
            settingsRepository.updateQuietHours(start, end)
        }
    }

    fun clearImageCache() {
        viewModelScope.launch {
            settingsRepository.clearImageCache()
        }
    }

    fun exportData(): String {
        var data = ""
        viewModelScope.launch {
            data = settingsRepository.exportData()
        }
        return data
    }

    fun exportWatchHistory(): String {
        var data = ""
        viewModelScope.launch {
            data = settingsRepository.exportWatchHistoryCsv()
        }
        return data
    }
}
