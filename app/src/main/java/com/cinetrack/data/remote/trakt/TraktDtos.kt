package com.cinetrack.data.remote.trakt

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ─── Auth ───────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class TraktTokenRequest(
    @Json(name = "code") val code: String,
    @Json(name = "client_id") val clientId: String,
    @Json(name = "client_secret") val clientSecret: String,
    @Json(name = "redirect_uri") val redirectUri: String,
    @Json(name = "grant_type") val grantType: String = "authorization_code"
)

@JsonClass(generateAdapter = true)
data class TraktRefreshRequest(
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "client_id") val clientId: String,
    @Json(name = "client_secret") val clientSecret: String,
    @Json(name = "redirect_uri") val redirectUri: String,
    @Json(name = "grant_type") val grantType: String = "refresh_token"
)

@JsonClass(generateAdapter = true)
data class TraktTokenResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Long,
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "scope") val scope: String,
    @Json(name = "created_at") val createdAt: Long
)

// ─── User Profile ────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class TraktUserProfile(
    @Json(name = "username") val username: String,
    @Json(name = "name") val name: String?,
    @Json(name = "joined_at") val joinedAt: String?,
    @Json(name = "avatar") val avatar: TraktAvatar?,
    @Json(name = "vip") val vip: Boolean = false
)

@JsonClass(generateAdapter = true)
data class TraktAvatar(
    @Json(name = "full") val full: String?
)

// ─── Media IDs ───────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class TraktIds(
    @Json(name = "trakt") val trakt: Int? = null,
    @Json(name = "slug") val slug: String? = null,
    @Json(name = "tmdb") val tmdb: Int? = null,
    @Json(name = "imdb") val imdb: String? = null,
    @Json(name = "tvdb") val tvdb: Int? = null
)

// ─── Movie ───────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class TraktMovie(
    @Json(name = "title") val title: String,
    @Json(name = "year") val year: Int?,
    @Json(name = "ids") val ids: TraktIds
)

@JsonClass(generateAdapter = true)
data class TraktMovieItem(
    @Json(name = "movie") val movie: TraktMovie,
    @Json(name = "watched_at") val watchedAt: String? = null,
    @Json(name = "listed_at") val listedAt: String? = null,
    @Json(name = "collected_at") val collectedAt: String? = null,
    @Json(name = "plays") val plays: Int = 0,
    @Json(name = "last_watched_at") val lastWatchedAt: String? = null
)

// ─── Show ────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class TraktShow(
    @Json(name = "title") val title: String,
    @Json(name = "year") val year: Int?,
    @Json(name = "ids") val ids: TraktIds
)

@JsonClass(generateAdapter = true)
data class TraktShowItem(
    @Json(name = "show") val show: TraktShow,
    @Json(name = "watched_at") val watchedAt: String? = null,
    @Json(name = "listed_at") val listedAt: String? = null,
    @Json(name = "plays") val plays: Int = 0,
    @Json(name = "last_watched_at") val lastWatchedAt: String? = null,
    @Json(name = "seasons") val seasons: List<TraktWatchedSeason>? = null
)

@JsonClass(generateAdapter = true)
data class TraktWatchedSeason(
    @Json(name = "number") val number: Int,
    @Json(name = "episodes") val episodes: List<TraktWatchedEpisode>
)

@JsonClass(generateAdapter = true)
data class TraktWatchedEpisode(
    @Json(name = "number") val number: Int,
    @Json(name = "plays") val plays: Int = 0,
    @Json(name = "last_watched_at") val lastWatchedAt: String? = null
)

// ─── Episode ─────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class TraktEpisode(
    @Json(name = "season") val season: Int,
    @Json(name = "number") val number: Int,
    @Json(name = "title") val title: String? = null,
    @Json(name = "ids") val ids: TraktIds? = null
)

@JsonClass(generateAdapter = true)
data class TraktEpisodeItem(
    @Json(name = "episode") val episode: TraktEpisode,
    @Json(name = "show") val show: TraktShow,
    @Json(name = "watched_at") val watchedAt: String? = null,
    @Json(name = "plays") val plays: Int = 0
)

// ─── Sync Requests ───────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class TraktSyncMoviesRequest(
    @Json(name = "movies") val movies: List<TraktSyncMovie>
)

@JsonClass(generateAdapter = true)
data class TraktSyncMovie(
    @Json(name = "ids") val ids: TraktIds,
    @Json(name = "watched_at") val watchedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class TraktSyncShowRequest(
    @Json(name = "shows") val shows: List<TraktSyncShow>
)

@JsonClass(generateAdapter = true)
data class TraktSyncShow(
    @Json(name = "ids") val ids: TraktIds,
    @Json(name = "seasons") val seasons: List<TraktSyncSeason>? = null
)

@JsonClass(generateAdapter = true)
data class TraktSyncSeason(
    @Json(name = "number") val number: Int,
    @Json(name = "episodes") val episodes: List<TraktSyncEpisode>
)

@JsonClass(generateAdapter = true)
data class TraktSyncEpisode(
    @Json(name = "number") val number: Int,
    @Json(name = "watched_at") val watchedAt: String? = null
)

// ─── Watchlist ────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class TraktWatchlistMovieRequest(
    @Json(name = "movies") val movies: List<TraktSyncMovie>
)

@JsonClass(generateAdapter = true)
data class TraktWatchlistShowRequest(
    @Json(name = "shows") val shows: List<TraktSyncShow>
)

// ─── Ratings ─────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class TraktRatedMovie(
    @Json(name = "movie") val movie: TraktMovie,
    @Json(name = "rating") val rating: Int,
    @Json(name = "rated_at") val ratedAt: String?
)

@JsonClass(generateAdapter = true)
data class TraktRatedShow(
    @Json(name = "show") val show: TraktShow,
    @Json(name = "rating") val rating: Int,
    @Json(name = "rated_at") val ratedAt: String?
)

@JsonClass(generateAdapter = true)
data class TraktRatingsRequest(
    @Json(name = "movies") val movies: List<TraktRatingItem>? = null,
    @Json(name = "shows") val shows: List<TraktRatingItem>? = null
)

@JsonClass(generateAdapter = true)
data class TraktRatingItem(
    @Json(name = "ids") val ids: TraktIds,
    @Json(name = "rating") val rating: Int
)

// ─── Sync Response ───────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class TraktSyncResponse(
    @Json(name = "added") val added: TraktSyncCounts? = null,
    @Json(name = "not_found") val notFound: TraktNotFound? = null
)

@JsonClass(generateAdapter = true)
data class TraktSyncCounts(
    @Json(name = "movies") val movies: Int = 0,
    @Json(name = "episodes") val episodes: Int = 0,
    @Json(name = "shows") val shows: Int = 0
)

@JsonClass(generateAdapter = true)
data class TraktNotFound(
    @Json(name = "movies") val movies: List<Any>? = null,
    @Json(name = "shows") val shows: List<Any>? = null
)

// ─── Last Activity ────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class TraktLastActivity(
    @Json(name = "movies") val movies: TraktMovieActivity?,
    @Json(name = "shows") val shows: TraktShowActivity?
)

@JsonClass(generateAdapter = true)
data class TraktMovieActivity(
    @Json(name = "watched_at") val watchedAt: String?,
    @Json(name = "rated_at") val ratedAt: String?,
    @Json(name = "watchlisted_at") val watchlistedAt: String?
)

@JsonClass(generateAdapter = true)
data class TraktShowActivity(
    @Json(name = "watched_at") val watchedAt: String?,
    @Json(name = "rated_at") val ratedAt: String?,
    @Json(name = "watchlisted_at") val watchlistedAt: String?
)
