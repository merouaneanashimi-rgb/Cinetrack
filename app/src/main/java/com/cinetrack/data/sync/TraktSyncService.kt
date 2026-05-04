package com.cinetrack.data.sync

import com.cinetrack.data.local.dao.EpisodeDao
import com.cinetrack.data.local.dao.MovieDao
import com.cinetrack.data.local.dao.ShowDao
import com.cinetrack.data.remote.trakt.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

data class SyncResult(
    val pushedMovies: Int = 0,
    val pulledMovies: Int = 0,
    val pushedEpisodes: Int = 0,
    val pulledEpisodes: Int = 0,
    val pushedRatings: Int = 0,
    val pulledRatings: Int = 0,
    val errors: List<String> = emptyList()
)

@Singleton
class TraktSyncService @Inject constructor(
    private val traktApi: TraktApiService,
    private val authManager: TraktAuthManager,
    private val movieDao: MovieDao,
    private val showDao: ShowDao,
    private val episodeDao: EpisodeDao
) {
    // ─── Full Bidirectional Sync ──────────────────────────────────────────────

    suspend fun fullSync(): SyncResult {
        val token = authManager.getAccessToken() ?: return SyncResult(errors = listOf("Not authenticated"))
        val bearer = authManager.getBearerToken(token)
        val errors = mutableListOf<String>()
        var result = SyncResult()

        // 1. Push local → Trakt
        result = result.copy(
            pushedMovies   = pushWatchedMovies(bearer, errors),
            pushedEpisodes = pushWatchedEpisodes(bearer, errors),
            pushedRatings  = pushRatings(bearer, errors)
        )

        // 2. Pull Trakt → local
        result = result.copy(
            pulledMovies   = pullWatchedMovies(bearer, errors),
            pulledEpisodes = pullWatchedEpisodes(bearer, errors),
            pulledRatings  = pullRatings(bearer, errors)
        )

        authManager.updateLastSync()
        return result.copy(errors = errors)
    }

    // ─── PUSH: Local → Trakt ─────────────────────────────────────────────────

    private suspend fun pushWatchedMovies(bearer: String, errors: MutableList<String>): Int {
        return try {
            val watched = movieDao.getWatched().first()
            if (watched.isEmpty()) return 0

            val syncMovies = watched.mapNotNull { movie ->
                movie.tmdbId.let { tmdb ->
                    TraktSyncMovie(
                        ids = TraktIds(tmdb = tmdb),
                        watchedAt = movie.watchedAt?.let { formatDate(it) }
                    )
                }
            }
            val response = traktApi.addMoviesToHistory(
                bearer,
                TraktSyncMoviesRequest(syncMovies)
            )
            response.body()?.added?.movies ?: 0
        } catch (e: Exception) {
            errors.add("Push movies: ${e.message}")
            0
        }
    }

    private suspend fun pushWatchedEpisodes(bearer: String, errors: MutableList<String>): Int {
        return try {
            val watchedEps = episodeDao.getWatchedEpisodes().first()
            if (watchedEps.isEmpty()) return 0

            // Group by showId → season → episodes
            val byShow = watchedEps.groupBy { it.showId }
            var total = 0

            byShow.forEach { (showId, episodes) ->
                val show = showDao.getById(showId).first() ?: return@forEach
                val tmdbId = show.tmdbId

                val seasons = episodes.groupBy { it.seasonNumber }.map { (seasonNum, eps) ->
                    TraktSyncSeason(
                        number = seasonNum,
                        episodes = eps.map { ep ->
                            TraktSyncEpisode(
                                number = ep.episodeNumber,
                                watchedAt = ep.watchedAt?.let { formatDate(it) }
                            )
                        }
                    )
                }

                val response = traktApi.addShowsToHistory(
                    bearer,
                    TraktSyncShowRequest(
                        listOf(TraktSyncShow(ids = TraktIds(tmdb = tmdbId), seasons = seasons))
                    )
                )
                total += response.body()?.added?.episodes ?: 0
            }
            total
        } catch (e: Exception) {
            errors.add("Push episodes: ${e.message}")
            0
        }
    }

    private suspend fun pushRatings(bearer: String, errors: MutableList<String>): Int {
        return try {
            val ratedMovies = movieDao.getRatedMovies().first()
            val ratedShows  = showDao.getRatedShows().first()
            if (ratedMovies.isEmpty() && ratedShows.isEmpty()) return 0

            val movieRatings = ratedMovies.mapNotNull { m ->
                m.userRating?.let { r ->
                    TraktRatingItem(ids = TraktIds(tmdb = m.tmdbId), rating = r.toInt().coerceIn(1, 10))
                }
            }
            val showRatings = ratedShows.mapNotNull { s ->
                s.userRating?.let { r ->
                    TraktRatingItem(ids = TraktIds(tmdb = s.tmdbId), rating = r.toInt().coerceIn(1, 10))
                }
            }

            val response = traktApi.addRatings(
                bearer,
                TraktRatingsRequest(
                    movies = movieRatings.ifEmpty { null },
                    shows  = showRatings.ifEmpty { null }
                )
            )
            (response.body()?.added?.movies ?: 0) + (response.body()?.added?.shows ?: 0)
        } catch (e: Exception) {
            errors.add("Push ratings: ${e.message}")
            0
        }
    }

    // ─── PULL: Trakt → Local ─────────────────────────────────────────────────

    private suspend fun pullWatchedMovies(bearer: String, errors: MutableList<String>): Int {
        return try {
            val response = traktApi.getWatchedMovies(bearer)
            if (!response.isSuccessful) return 0

            val traktMovies = response.body() ?: return 0
            var count = 0

            traktMovies.forEach { item ->
                val tmdbId = item.movie.ids.tmdb ?: return@forEach
                val local = movieDao.getByTmdbId(tmdbId) ?: return@forEach

                if (!local.isWatched) {
                    val watchedAt = item.lastWatchedAt?.let { parseDate(it) } ?: System.currentTimeMillis()
                    movieDao.markWatched(local.id, true, watchedAt)
                    count++
                }
            }
            count
        } catch (e: Exception) {
            errors.add("Pull movies: ${e.message}")
            0
        }
    }

    private suspend fun pullWatchedEpisodes(bearer: String, errors: MutableList<String>): Int {
        return try {
            val response = traktApi.getWatchedShows(bearer)
            if (!response.isSuccessful) return 0

            val traktShows = response.body() ?: return 0
            var count = 0

            traktShows.forEach { showItem ->
                val tmdbId = showItem.show.ids.tmdb ?: return@forEach
                val localShow = showDao.getByTmdbId(tmdbId) ?: return@forEach

                showItem.seasons?.forEach { traktSeason ->
                    traktSeason.episodes.forEach { traktEp ->
                        val localEp = episodeDao.getByShowSeasonEpisode(
                            localShow.id,
                            traktSeason.number,
                            traktEp.number
                        )
                        if (localEp != null && !localEp.isWatched) {
                            val watchedAt = traktEp.lastWatchedAt?.let { parseDate(it) }
                                ?: System.currentTimeMillis()
                            episodeDao.markWatched(localEp.id, true, watchedAt)
                            count++
                        }
                    }
                }
            }
            count
        } catch (e: Exception) {
            errors.add("Pull episodes: ${e.message}")
            0
        }
    }

    private suspend fun pullRatings(bearer: String, errors: MutableList<String>): Int {
        return try {
            var count = 0

            // Movies
            val moviesResp = traktApi.getRatedMovies(bearer)
            moviesResp.body()?.forEach { rated ->
                val tmdbId = rated.movie.ids.tmdb ?: return@forEach
                val local = movieDao.getByTmdbId(tmdbId) ?: return@forEach
                if (local.userRating == null) {
                    movieDao.updateRating(local.id, rated.rating.toDouble())
                    count++
                }
            }

            // Shows
            val showsResp = traktApi.getRatedShows(bearer)
            showsResp.body()?.forEach { rated ->
                val tmdbId = rated.show.ids.tmdb ?: return@forEach
                val local = showDao.getByTmdbId(tmdbId) ?: return@forEach
                if (local.userRating == null) {
                    showDao.updateRating(local.id, rated.rating.toDouble())
                    count++
                }
            }
            count
        } catch (e: Exception) {
            errors.add("Pull ratings: ${e.message}")
            0
        }
    }

    // ─── Single Item Sync (called on user action) ─────────────────────────────

    suspend fun markMovieWatchedOnTrakt(tmdbId: Int, watchedAt: Long?) {
        val token = authManager.getAccessToken() ?: return
        try {
            traktApi.addMoviesToHistory(
                authManager.getBearerToken(token),
                TraktSyncMoviesRequest(
                    listOf(TraktSyncMovie(
                        ids = TraktIds(tmdb = tmdbId),
                        watchedAt = watchedAt?.let { formatDate(it) }
                    ))
                )
            )
        } catch (_: Exception) {}
    }

    suspend fun markEpisodeWatchedOnTrakt(showTmdbId: Int, season: Int, episode: Int, watchedAt: Long?) {
        val token = authManager.getAccessToken() ?: return
        try {
            traktApi.addShowsToHistory(
                authManager.getBearerToken(token),
                TraktSyncShowRequest(
                    listOf(TraktSyncShow(
                        ids = TraktIds(tmdb = showTmdbId),
                        seasons = listOf(TraktSyncSeason(
                            number = season,
                            episodes = listOf(TraktSyncEpisode(
                                number = episode,
                                watchedAt = watchedAt?.let { formatDate(it) }
                            ))
                        ))
                    ))
                )
            )
        } catch (_: Exception) {}
    }

    suspend fun addMovieToWatchlistOnTrakt(tmdbId: Int) {
        val token = authManager.getAccessToken() ?: return
        try {
            traktApi.addMoviesToWatchlist(
                authManager.getBearerToken(token),
                TraktWatchlistMovieRequest(listOf(TraktSyncMovie(ids = TraktIds(tmdb = tmdbId))))
            )
        } catch (_: Exception) {}
    }

    suspend fun addShowToWatchlistOnTrakt(tmdbId: Int) {
        val token = authManager.getAccessToken() ?: return
        try {
            traktApi.addShowsToWatchlist(
                authManager.getBearerToken(token),
                TraktWatchlistShowRequest(listOf(TraktSyncShow(ids = TraktIds(tmdb = tmdbId))))
            )
        } catch (_: Exception) {}
    }

    suspend fun rateMovieOnTrakt(tmdbId: Int, rating: Int) {
        val token = authManager.getAccessToken() ?: return
        try {
            traktApi.addRatings(
                authManager.getBearerToken(token),
                TraktRatingsRequest(movies = listOf(TraktRatingItem(TraktIds(tmdb = tmdbId), rating.coerceIn(1, 10))))
            )
        } catch (_: Exception) {}
    }

    suspend fun rateShowOnTrakt(tmdbId: Int, rating: Int) {
        val token = authManager.getAccessToken() ?: return
        try {
            traktApi.addRatings(
                authManager.getBearerToken(token),
                TraktRatingsRequest(shows = listOf(TraktRatingItem(TraktIds(tmdb = tmdbId), rating.coerceIn(1, 10))))
            )
        } catch (_: Exception) {}
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(java.util.Date(timestamp))
    }

    private fun parseDate(dateStr: String): Long {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            try {
                val sdf2 = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                sdf2.timeZone = java.util.TimeZone.getTimeZone("UTC")
                sdf2.parse(dateStr)?.time ?: System.currentTimeMillis()
            } catch (_: Exception) { System.currentTimeMillis() }
        }
    }
}
