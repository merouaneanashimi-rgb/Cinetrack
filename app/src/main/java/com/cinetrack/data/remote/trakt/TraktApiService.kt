package com.cinetrack.data.remote.trakt

import retrofit2.Response
import retrofit2.http.*

interface TraktApiService {

    // ─── Auth ─────────────────────────────────────────────────────────────────

    @POST("oauth/token")
    suspend fun exchangeCode(@Body request: TraktTokenRequest): Response<TraktTokenResponse>

    @POST("oauth/token")
    suspend fun refreshToken(@Body request: TraktRefreshRequest): Response<TraktTokenResponse>

    @POST("oauth/revoke")
    suspend fun revokeToken(@Body body: Map<String, String>): Response<Unit>

    // ─── User ─────────────────────────────────────────────────────────────────

    @GET("users/me")
    suspend fun getUserProfile(@Header("Authorization") token: String): Response<TraktUserProfile>

    @GET("sync/last_activities")
    suspend fun getLastActivity(@Header("Authorization") token: String): Response<TraktLastActivity>

    // ─── Watched History ──────────────────────────────────────────────────────

    @GET("sync/watched/movies")
    suspend fun getWatchedMovies(@Header("Authorization") token: String): Response<List<TraktMovieItem>>

    @GET("sync/watched/shows")
    suspend fun getWatchedShows(@Header("Authorization") token: String): Response<List<TraktShowItem>>

    @GET("sync/history/episodes")
    suspend fun getEpisodeHistory(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100
    ): Response<List<TraktEpisodeItem>>

    // ─── Add to History ───────────────────────────────────────────────────────

    @POST("sync/history")
    suspend fun addMoviesToHistory(
        @Header("Authorization") token: String,
        @Body request: TraktSyncMoviesRequest
    ): Response<TraktSyncResponse>

    @POST("sync/history")
    suspend fun addShowsToHistory(
        @Header("Authorization") token: String,
        @Body request: TraktSyncShowRequest
    ): Response<TraktSyncResponse>

    // ─── Remove from History ──────────────────────────────────────────────────

    @POST("sync/history/remove")
    suspend fun removeMoviesFromHistory(
        @Header("Authorization") token: String,
        @Body request: TraktSyncMoviesRequest
    ): Response<TraktSyncResponse>

    @POST("sync/history/remove")
    suspend fun removeShowsFromHistory(
        @Header("Authorization") token: String,
        @Body request: TraktSyncShowRequest
    ): Response<TraktSyncResponse>

    // ─── Watchlist ────────────────────────────────────────────────────────────

    @GET("sync/watchlist/movies")
    suspend fun getWatchlistMovies(@Header("Authorization") token: String): Response<List<TraktMovieItem>>

    @GET("sync/watchlist/shows")
    suspend fun getWatchlistShows(@Header("Authorization") token: String): Response<List<TraktShowItem>>

    @POST("sync/watchlist")
    suspend fun addMoviesToWatchlist(
        @Header("Authorization") token: String,
        @Body request: TraktWatchlistMovieRequest
    ): Response<TraktSyncResponse>

    @POST("sync/watchlist")
    suspend fun addShowsToWatchlist(
        @Header("Authorization") token: String,
        @Body request: TraktWatchlistShowRequest
    ): Response<TraktSyncResponse>

    @POST("sync/watchlist/remove")
    suspend fun removeMoviesFromWatchlist(
        @Header("Authorization") token: String,
        @Body request: TraktWatchlistMovieRequest
    ): Response<TraktSyncResponse>

    @POST("sync/watchlist/remove")
    suspend fun removeShowsFromWatchlist(
        @Header("Authorization") token: String,
        @Body request: TraktWatchlistShowRequest
    ): Response<TraktSyncResponse>

    // ─── Ratings ──────────────────────────────────────────────────────────────

    @GET("sync/ratings/movies")
    suspend fun getRatedMovies(@Header("Authorization") token: String): Response<List<TraktRatedMovie>>

    @GET("sync/ratings/shows")
    suspend fun getRatedShows(@Header("Authorization") token: String): Response<List<TraktRatedShow>>

    @POST("sync/ratings")
    suspend fun addRatings(
        @Header("Authorization") token: String,
        @Body request: TraktRatingsRequest
    ): Response<TraktSyncResponse>

    @POST("sync/ratings/remove")
    suspend fun removeRatings(
        @Header("Authorization") token: String,
        @Body request: TraktRatingsRequest
    ): Response<TraktSyncResponse>
}
