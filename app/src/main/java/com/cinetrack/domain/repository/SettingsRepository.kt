package com.cinetrack.domain.repository

import com.cinetrack.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateSettings(settings: AppSettings)
    suspend fun updateTheme(theme: String)
    suspend fun updateAccentColor(color: String)
    suspend fun updateGridColumns(columns: Int)
    suspend fun updateCardStyle(style: String)
    suspend fun updateRatingScale(scale: String)
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    suspend fun updateQuietHours(start: Int?, end: Int?)
    suspend fun clearImageCache()
    suspend fun exportData(): String
    suspend fun importData(json: String): Boolean
    suspend fun exportWatchHistoryCsv(): String
}
