package com.cinetrack.domain.repository

import com.cinetrack.domain.model.*
import com.cinetrack.util.Result
import kotlinx.coroutines.flow.Flow

interface ShowRepository {
    fun getShowById(id: Long): Flow<Show?>
    suspend fun getShowByTmdbId(tmdbId: Int): Show?
    fun getShowsByListType(listType: UserListType): Flow<List<Show>>
    fun getShowsByListTypeAndAirStatus(listType: UserListType, airStatus: AirStatus): Flow<List<Show>>
    fun getWatchingShowsByNextEpisode(): Flow<List<Show>>
    fun getShowsByAirStatus(airStatus: AirStatus): Flow<List<Show>>
    fun getFavoriteShows(): Flow<List<Show>>
    fun getAllTrackedShows(): Flow<List<Show>>
    fun getTrackedShowCount(): Flow<Int>
    suspend fun addShow(show: Show, listType: UserListType): Long
    suspend fun updateShowListType(showId: Long, listType: UserListType?)
    suspend fun updateShowFavorite(showId: Long, isFavorite: Boolean)
    suspend fun updateShowRating(showId: Long, rating: Double?)
    suspend fun updateShowNotes(showId: Long, notes: String?)
    suspend fun removeShow(showId: Long)
    suspend fun syncShowFromApi(tmdbId: Int): Result<Show>
    suspend fun refreshShowAirStatus(showId: Long)
    suspend fun toggleNotify(showId: Long, enabled: Boolean)

    // Seasons & Episodes
    fun getSeasonsByShow(showId: Long): Flow<List<Season>>
    fun getEpisodesBySeason(seasonId: Long): Flow<List<Episode>>
    suspend fun getEpisodesByShowAndSeason(showId: Long, seasonNumber: Int): List<Episode>
    suspend fun updateEpisodeWatched(episodeId: Long, watched: Boolean)
    suspend fun markSeasonWatched(seasonId: Long, watched: Boolean)
    suspend fun markUpToEpisodeWatched(showId: Long, seasonNumber: Int, episodeNumber: Int)
    suspend fun markFromEpisodeWatched(showId: Long, seasonNumber: Int, episodeNumber: Int)
    suspend fun markAllShowEpisodesWatched(showId: Long)
    suspend fun updateEpisodeRating(episodeId: Long, rating: Double?)
    suspend fun updateEpisodeNotes(episodeId: Long, notes: String?)
    suspend fun getWatchedEpisodeCount(showId: Long): Int
    suspend fun getTotalEpisodeCount(showId: Long): Int
    suspend fun getNextUnwatchedEpisode(showId: Long): Episode?
    fun getRecentlyWatchedEpisodes(limit: Int): Flow<List<Episode>>
    fun getAllWatchedEpisodes(): Flow<List<Episode>>
    suspend fun getNotificationEnabledShows(): List<Show>
    suspend fun isShowTracked(tmdbId: Int): Boolean
}
