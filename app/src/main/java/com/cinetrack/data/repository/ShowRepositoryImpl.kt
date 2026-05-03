package com.cinetrack.data.repository

import com.cinetrack.data.local.dao.*
import com.cinetrack.data.local.entity.*
import com.cinetrack.data.remote.api.TmdbApiService
import com.cinetrack.data.remote.dto.*
import com.cinetrack.domain.model.*
import com.cinetrack.domain.repository.ShowRepository
import com.cinetrack.util.Result
import com.cinetrack.util.safeApiCall
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowRepositoryImpl @Inject constructor(
    private val showDao: ShowDao,
    private val seasonDao: SeasonDao,
    private val episodeDao: EpisodeDao,
    private val api: TmdbApiService
) : ShowRepository {

    override fun getShowById(id: Long): Flow<Show?> {
        return showDao.getById(id).map { it?.toDomainModel() }
    }

    override suspend fun getShowByTmdbId(tmdbId: Int): Show? {
        return showDao.getByTmdbId(tmdbId)?.toDomainModel()
    }

    override fun getShowsByListType(listType: UserListType): Flow<List<Show>> {
        return showDao.getByListType(listType).map { list -> list.map { it.toDomainModel() } }
    }

    override fun getShowsByListTypeAndAirStatus(listType: UserListType, airStatus: AirStatus): Flow<List<Show>> {
        return showDao.getByListTypeAndAirStatus(listType, airStatus).map { list -> list.map { it.toDomainModel() } }
    }

    override fun getWatchingShowsByNextEpisode(): Flow<List<Show>> {
        return showDao.getWatchingByNextEpisode().map { list -> list.map { it.toDomainModel() } }
    }

    override fun getShowsByAirStatus(airStatus: AirStatus): Flow<List<Show>> {
        return showDao.getByAirStatusAndList(airStatus, com.cinetrack.data.local.entity.UserListType.WATCHING)
            .map { list -> list.map { it.toDomainModel() } }
    }

    override fun getFavoriteShows(): Flow<List<Show>> {
        return showDao.getFavorites().map { list -> list.map { it.toDomainModel() } }
    }

    override fun getAllTrackedShows(): Flow<List<Show>> {
        return showDao.getAllTracked().map { list -> list.map { it.toDomainModel() } }
    }

    override fun getTrackedShowCount(): Flow<Int> {
        return showDao.getTrackedCount()
    }

    override suspend fun addShow(show: Show, listType: UserListType): Long {
        val entity = show.toEntity().copy(userListType = listType, addedAt = System.currentTimeMillis())
        val id = showDao.insert(entity)
        
        // Fetch and store seasons/episodes
        try {
            val response = api.getTvShowDetail(show.tmdbId)
            if (response.isSuccessful) {
                response.body()?.let { detail ->
                    detail.seasons?.forEach { season ->
                        syncSeason(show.tmdbId, season.seasonNumber, id)
                    }
                    updateAirStatus(id, detail)
                }
            }
        } catch (e: Exception) {
            // Offline - will sync later
        }
        
        return id
    }

    override suspend fun updateShowListType(showId: Long, listType: UserListType?) {
        showDao.updateListType(showId, listType?.let { com.cinetrack.data.local.entity.UserListType.valueOf(it.name) })
    }

    override suspend fun updateShowFavorite(showId: Long, isFavorite: Boolean) {
        showDao.updateFavorite(showId, isFavorite)
    }

    override suspend fun updateShowRating(showId: Long, rating: Double?) {
        showDao.updateRating(showId, rating)
    }

    override suspend fun updateShowNotes(showId: Long, notes: String?) {
        showDao.updateNotes(showId, notes)
    }

    override suspend fun removeShow(showId: Long) {
        episodeDao.deleteByShow(showId)
        seasonDao.deleteByShow(showId)
        showDao.deleteById(showId)
    }

    override suspend fun syncShowFromApi(tmdbId: Int): Result<Show> {
        return safeApiCall {
            val response = api.getTvShowDetail(tmdbId)
            if (response.isSuccessful) {
                val detail = response.body() ?: throw Exception("Empty response")
                val existing = showDao.getByTmdbId(tmdbId)
                val entity = detail.toShowEntity(existing)
                val showId = showDao.insert(entity)
                
                // Sync seasons
                detail.seasons?.forEach { season ->
                    syncSeason(tmdbId, season.seasonNumber, showId)
                }
                
                entity.copy(id = showId).toDomainModel()
            } else {
                throw Exception("API Error: ${response.code()}")
            }
        }
    }

    override suspend fun refreshShowAirStatus(showId: Long) {
        val show = showDao.getById(showId).first() ?: return
        try {
            val response = api.getTvShowDetail(show.tmdbId)
            if (response.isSuccessful) {
                response.body()?.let { detail ->
                    updateAirStatus(showId, detail)
                }
            }
        } catch (e: Exception) {
            // Network error - use cached data
        }
    }

    override suspend fun toggleNotify(showId: Long, enabled: Boolean) {
        showDao.updateNotifyEnabled(showId, enabled)
    }

    private suspend fun updateAirStatus(showId: Long, detail: TvShowDetailDto) {
        val airStatus = computeAirStatus(detail.status, detail.nextEpisodeToAir != null)
        showDao.updateAirStatus(
            showId = showId,
            airStatus = com.cinetrack.data.local.entity.AirStatus.valueOf(airStatus.name),
            nextDate = detail.nextEpisodeToAir?.airDate,
            nextEpNum = detail.nextEpisodeToAir?.episodeNumber,
            nextSeason = detail.nextEpisodeToAir?.seasonNumber,
            nextTitle = detail.nextEpisodeToAir?.name
        )
    }

    private suspend fun syncSeason(showTmdbId: Int, seasonNumber: Int, showId: Long) {
        try {
            val response = api.getSeasonDetail(showTmdbId, seasonNumber)
            if (response.isSuccessful) {
                response.body()?.let { seasonDetail ->
                    val seasonEntity = SeasonEntity(
                        showId = showId,
                        tmdbId = seasonDetail.id,
                        seasonNumber = seasonDetail.seasonNumber,
                        name = seasonDetail.name ?: "Season $seasonNumber",
                        posterPath = seasonDetail.posterPath,
                        episodeCount = seasonDetail.episodes?.size ?: 0,
                        airDate = seasonDetail.airDate,
                        overview = seasonDetail.overview
                    )
                    val seasonId = seasonDao.insert(seasonEntity)

                    seasonDetail.episodes?.map { ep ->
                        EpisodeEntity(
                            showId = showId,
                            seasonId = seasonId,
                            tmdbId = ep.id,
                            episodeNumber = ep.episodeNumber,
                            seasonNumber = ep.seasonNumber,
                            name = ep.name,
                            overview = ep.overview,
                            airDate = ep.airDate,
                            runtime = ep.runtime,
                            stillPath = ep.stillPath,
                            guestStarsJson = ep.guestStars?.let { guestStars ->
                                guestStars.joinToString(",") { it.name ?: "" }
                            }
                        )
                    }?.let { episodeDao.insertAll(it) }
                }
            }
        } catch (e: Exception) {
            // Season sync failed - will retry later
        }
    }

    override fun getSeasonsByShow(showId: Long): Flow<List<Season>> {
        return seasonDao.getByShowId(showId).map { list -> list.map { it.toDomainModel() } }
    }

    override fun getEpisodesBySeason(seasonId: Long): Flow<List<Episode>> {
        return episodeDao.getBySeasonId(seasonId).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun getEpisodesByShowAndSeason(showId: Long, seasonNumber: Int): List<Episode> {
        return episodeDao.getByShowAndSeasonSync(showId, seasonNumber).map { it.toDomainModel() }
    }

    override suspend fun updateEpisodeWatched(episodeId: Long, watched: Boolean) {
        val watchedAt = if (watched) System.currentTimeMillis() else null
        episodeDao.updateWatchedStatus(episodeId, watched, watchedAt)
    }

    override suspend fun markSeasonWatched(seasonId: Long, watched: Boolean) {
        if (watched) {
            episodeDao.markSeasonWatched(seasonId)
        } else {
            episodeDao.markSeasonUnwatched(seasonId)
        }
    }

    override suspend fun markUpToEpisodeWatched(showId: Long, seasonNumber: Int, episodeNumber: Int) {
        episodeDao.markUpToEpisodeWatched(showId, seasonNumber, episodeNumber)
    }

    override suspend fun markFromEpisodeWatched(showId: Long, seasonNumber: Int, episodeNumber: Int) {
        episodeDao.markFromEpisodeWatched(showId, seasonNumber, episodeNumber)
    }

    override suspend fun markAllShowEpisodesWatched(showId: Long) {
        episodeDao.markAllShowEpisodesWatched(showId)
    }

    override suspend fun updateEpisodeRating(episodeId: Long, rating: Double?) {
        episodeDao.updateRating(episodeId, rating)
    }

    override suspend fun updateEpisodeNotes(episodeId: Long, notes: String?) {
        episodeDao.updateNotes(episodeId, notes)
    }

    override suspend fun getWatchedEpisodeCount(showId: Long): Int {
        return episodeDao.getWatchedCountByShow(showId)
    }

    override suspend fun getTotalEpisodeCount(showId: Long): Int {
        return episodeDao.getTotalCountByShow(showId)
    }

    override suspend fun getNextUnwatchedEpisode(showId: Long): Episode? {
        return episodeDao.getNextUnwatched(showId)?.toDomainModel()
    }

    override fun getRecentlyWatchedEpisodes(limit: Int): Flow<List<Episode>> {
        return episodeDao.getRecentlyWatched(limit).map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun getNotificationEnabledShows(): List<Show> {
        return showDao.getNotificationEnabled().map { it.toDomainModel() }
    }

    override suspend fun isShowTracked(tmdbId: Int): Boolean {
        return showDao.isTracked(tmdbId)
    }

    // Mappers
    private fun ShowEntity.toDomainModel() = Show(
        id = id,
        tmdbId = tmdbId,
        traktId = traktId,
        title = title,
        originalTitle = originalTitle,
        posterPath = posterPath,
        backdropPath = backdropPath,
        firstAirDate = firstAirDate,
        lastAirDate = lastAirDate,
        status = status,
        airStatus = com.cinetrack.domain.model.AirStatus.valueOf(airStatus.name),
        nextEpisodeAirDate = nextEpisodeAirDate,
        nextEpisodeNumber = nextEpisodeNumber,
        nextEpisodeSeason = nextEpisodeSeason,
        nextEpisodeTitle = nextEpisodeTitle,
        genres = genresJson?.split(",") ?: emptyList(),
        networks = networksJson?.split(",") ?: emptyList(),
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        overview = overview,
        numberOfSeasons = numberOfSeasons,
        numberOfEpisodes = numberOfEpisodes,
        episodeRuntime = episodeRuntime,
        userListType = userListType?.let { com.cinetrack.domain.model.UserListType.valueOf(it.name) },
        addedAt = addedAt,
        userRating = userRating,
        userNotes = userNotes,
        isFavorite = isFavorite,
        lastSyncedAt = lastSyncedAt,
        notifyEnabled = notifyEnabled
    )

    private fun Show.toEntity() = ShowEntity(
        id = id,
        tmdbId = tmdbId,
        traktId = traktId,
        title = title,
        originalTitle = originalTitle,
        posterPath = posterPath,
        backdropPath = backdropPath,
        firstAirDate = firstAirDate,
        lastAirDate = lastAirDate,
        status = status,
        airStatus = com.cinetrack.data.local.entity.AirStatus.valueOf(airStatus.name),
        nextEpisodeAirDate = nextEpisodeAirDate,
        nextEpisodeNumber = nextEpisodeNumber,
        nextEpisodeSeason = nextEpisodeSeason,
        nextEpisodeTitle = nextEpisodeTitle,
        genresJson = genres.joinToString(","),
        networksJson = networks.joinToString(","),
        voteAverage = voteAverage,
        voteCount = voteCount,
        popularity = popularity,
        overview = overview,
        numberOfSeasons = numberOfSeasons,
        numberOfEpisodes = numberOfEpisodes,
        episodeRuntime = episodeRuntime,
        userListType = userListType?.let { com.cinetrack.data.local.entity.UserListType.valueOf(it.name) },
        addedAt = addedAt,
        userRating = userRating,
        userNotes = userNotes,
        isFavorite = isFavorite,
        lastSyncedAt = lastSyncedAt,
        notifyEnabled = notifyEnabled
    )

    private fun SeasonEntity.toDomainModel() = Season(
        id = id,
        showId = showId,
        tmdbId = tmdbId,
        seasonNumber = seasonNumber,
        name = name,
        posterPath = posterPath,
        episodeCount = episodeCount,
        airDate = airDate,
        overview = overview,
        isWatched = isWatched
    )

    private fun EpisodeEntity.toDomainModel() = Episode(
        id = id,
        showId = showId,
        seasonId = seasonId,
        tmdbId = tmdbId,
        episodeNumber = episodeNumber,
        seasonNumber = seasonNumber,
        name = name,
        overview = overview,
        airDate = airDate,
        runtime = runtime,
        stillPath = stillPath,
        guestStars = guestStarsJson?.split(",") ?: emptyList(),
        isWatched = isWatched,
        watchedAt = watchedAt,
        userRating = userRating,
        userNotes = userNotes
    )

    private fun TvShowDetailDto.toShowEntity(existing: ShowEntity?): ShowEntity {
        val computedAirStatus = computeAirStatus(status, nextEpisodeToAir != null)
        return ShowEntity(
            id = existing?.id ?: 0,
            tmdbId = id,
            title = name,
            originalTitle = originalName,
            posterPath = posterPath,
            backdropPath = backdropPath,
            firstAirDate = firstAirDate,
            lastAirDate = lastAirDate,
            status = status,
            airStatus = com.cinetrack.data.local.entity.AirStatus.valueOf(computedAirStatus.name),
            nextEpisodeAirDate = nextEpisodeToAir?.airDate,
            nextEpisodeNumber = nextEpisodeToAir?.episodeNumber,
            nextEpisodeSeason = nextEpisodeToAir?.seasonNumber,
            nextEpisodeTitle = nextEpisodeToAir?.name,
            genresJson = genres?.joinToString(",") { it.name },
            networksJson = networks?.joinToString(",") { it.name },
            voteAverage = voteAverage,
            voteCount = voteCount,
            popularity = popularity,
            overview = overview,
            numberOfSeasons = numberOfSeasons,
            numberOfEpisodes = numberOfEpisodes,
            episodeRuntime = episodeRunTime?.firstOrNull() ?: 0,
            userListType = existing?.userListType,
            addedAt = existing?.addedAt ?: System.currentTimeMillis(),
            userRating = existing?.userRating,
            userNotes = existing?.userNotes,
            isFavorite = existing?.isFavorite ?: false,
            lastSyncedAt = System.currentTimeMillis(),
            notifyEnabled = existing?.notifyEnabled ?: false
        )
    }

    private fun computeAirStatus(status: String?, hasNextEpisode: Boolean): com.cinetrack.domain.model.AirStatus {
        return when (status) {
            "Returning Series" -> if (hasNextEpisode) com.cinetrack.domain.model.AirStatus.RETURNING else com.cinetrack.domain.model.AirStatus.HIATUS
            "In Production" -> com.cinetrack.domain.model.AirStatus.IN_PRODUCTION
            "Planned", "Pilot" -> com.cinetrack.domain.model.AirStatus.UPCOMING
            "Ended" -> com.cinetrack.domain.model.AirStatus.ENDED
            "Canceled" -> com.cinetrack.domain.model.AirStatus.CANCELED
            else -> com.cinetrack.domain.model.AirStatus.UPCOMING
        }
    }
}
