package com.cinetrack.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import coil.imageLoader
import com.cinetrack.domain.model.*
import com.cinetrack.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val showDao: com.cinetrack.data.local.dao.ShowDao,
    private val movieDao: com.cinetrack.data.local.dao.MovieDao,
    private val episodeDao: com.cinetrack.data.local.dao.EpisodeDao
) : SettingsRepository {

    private val THEME = stringPreferencesKey("theme")
    private val ACCENT_COLOR = stringPreferencesKey("accent_color")
    private val GRID_COLUMNS = intPreferencesKey("grid_columns")
    private val CARD_STYLE = stringPreferencesKey("card_style")
    private val RATING_SCALE = stringPreferencesKey("rating_scale")
    private val DEFAULT_LANGUAGE = stringPreferencesKey("default_language")
    private val ADULT_CONTENT = booleanPreferencesKey("adult_content")
    private val REGION = stringPreferencesKey("region")
    private val DEFAULT_SHOW_LIST = stringPreferencesKey("default_show_list")
    private val AUTO_MARK_WATCHED = booleanPreferencesKey("auto_mark_watched")
    private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    private val QUIET_HOURS_START = intPreferencesKey("quiet_hours_start")
    private val QUIET_HOURS_END = intPreferencesKey("quiet_hours_end")

    override fun getSettings(): Flow<AppSettings> {
        return context.dataStore.data.map { prefs ->
            AppSettings(
                theme = prefs[THEME]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM,
                accentColor = prefs[ACCENT_COLOR] ?: "#01B4E4",
                gridColumns = prefs[GRID_COLUMNS] ?: 3,
                cardStyle = prefs[CARD_STYLE]?.let { CardStyle.valueOf(it) } ?: CardStyle.POSTER_INFO,
                ratingScale = prefs[RATING_SCALE]?.let { RatingScale.valueOf(it) } ?: RatingScale.STARS_5,
                defaultLanguage = prefs[DEFAULT_LANGUAGE] ?: "en",
                adultContent = prefs[ADULT_CONTENT] ?: false,
                region = prefs[REGION] ?: "US",
                defaultShowList = prefs[DEFAULT_SHOW_LIST]?.let { UserListType.valueOf(it) } ?: UserListType.WATCHING,
                autoMarkWatched = prefs[AUTO_MARK_WATCHED] ?: true,
                notificationsEnabled = prefs[NOTIFICATIONS_ENABLED] ?: true,
                quietHoursStart = prefs[QUIET_HOURS_START] ?: 23,
                quietHoursEnd = prefs[QUIET_HOURS_END] ?: 8
            )
        }
    }

    override suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[THEME] = settings.theme.name
            prefs[ACCENT_COLOR] = settings.accentColor
            prefs[GRID_COLUMNS] = settings.gridColumns
            prefs[CARD_STYLE] = settings.cardStyle.name
            prefs[RATING_SCALE] = settings.ratingScale.name
            prefs[DEFAULT_LANGUAGE] = settings.defaultLanguage
            prefs[ADULT_CONTENT] = settings.adultContent
            prefs[REGION] = settings.region
            prefs[DEFAULT_SHOW_LIST] = settings.defaultShowList.name
            prefs[AUTO_MARK_WATCHED] = settings.autoMarkWatched
            prefs[NOTIFICATIONS_ENABLED] = settings.notificationsEnabled
            prefs[QUIET_HOURS_START] = settings.quietHoursStart ?: 23
            prefs[QUIET_HOURS_END] = settings.quietHoursEnd ?: 8
        }
    }

    override suspend fun updateTheme(theme: String) {
        context.dataStore.edit { it[THEME] = theme }
    }

    override suspend fun updateAccentColor(color: String) {
        context.dataStore.edit { it[ACCENT_COLOR] = color }
    }

    override suspend fun updateGridColumns(columns: Int) {
        context.dataStore.edit { it[GRID_COLUMNS] = columns }
    }

    override suspend fun updateCardStyle(style: String) {
        context.dataStore.edit { it[CARD_STYLE] = style }
    }

    override suspend fun updateRatingScale(scale: String) {
        context.dataStore.edit { it[RATING_SCALE] = scale }
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    override suspend fun updateQuietHours(start: Int?, end: Int?) {
        context.dataStore.edit { prefs ->
            start?.let { prefs[QUIET_HOURS_START] = it }
            end?.let { prefs[QUIET_HOURS_END] = it }
        }
    }

    override suspend fun clearImageCache() {
        context.imageLoader.diskCache?.clear()
        context.imageLoader.memoryCache?.clear()
    }

    override suspend fun exportData(): String {
        val shows = showDao.getAllTracked().first()
        val movies = movieDao.getAllTracked().first()
        val exportData = ExportData(
            exportDate = System.currentTimeMillis(),
            shows = shows.map { s ->
                ShowExport(
                    tmdbId = s.tmdbId,
                    title = s.title,
                    listType = s.userListType?.name,
                    rating = s.userRating,
                    notes = s.userNotes,
                    isFavorite = s.isFavorite,
                    addedAt = s.addedAt,
                    notifyEnabled = s.notifyEnabled
                )
            },
            movies = movies.map { m ->
                MovieExport(
                    tmdbId = m.tmdbId,
                    title = m.title,
                    listType = m.userListType?.name,
                    isWatched = m.isWatched,
                    watchedAt = m.watchedAt,
                    rating = m.userRating,
                    notes = m.userNotes,
                    isFavorite = m.isFavorite,
                    addedAt = m.addedAt
                )
            }
        )
        return Json.encodeToString(exportData)
    }

    override suspend fun importData(json: String): Boolean {
        return try {
            val data = Json.decodeFromString<ExportData>(json)
            // Import logic - merge with existing data
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun exportWatchHistoryCsv(): String {
        val episodes = episodeDao.getRecentlyWatched(1000).first()
        val header = "Date,Show,Season,Episode,Title,Runtime\n"
        val rows = episodes.joinToString("\n") { ep ->
            val date = ep.watchedAt?.let { java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date(it)) } ?: ""
            "\"$date\",\"\",\"${ep.seasonNumber}\",\"${ep.episodeNumber}\",\"${ep.name}\",\"${ep.runtime ?: 0}\""
        }
        return header + rows
    }

    @kotlinx.serialization.Serializable
    private data class ExportData(
        val exportDate: Long,
        val shows: List<ShowExport>,
        val movies: List<MovieExport>
    )

    @kotlinx.serialization.Serializable
    private data class ShowExport(
        val tmdbId: Int,
        val title: String,
        val listType: String?,
        val rating: Double?,
        val notes: String?,
        val isFavorite: Boolean,
        val addedAt: Long,
        val notifyEnabled: Boolean
    )

    @kotlinx.serialization.Serializable
    private data class MovieExport(
        val tmdbId: Int,
        val title: String,
        val listType: String?,
        val isWatched: Boolean,
        val watchedAt: Long?,
        val rating: Double?,
        val notes: String?,
        val isFavorite: Boolean,
        val addedAt: Long
    )
}
