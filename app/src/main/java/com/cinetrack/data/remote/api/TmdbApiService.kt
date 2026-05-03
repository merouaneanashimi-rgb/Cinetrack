package com.cinetrack.data.remote.api

import com.cinetrack.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    // Configuration
    @GET("configuration")
    suspend fun getConfiguration(): Response<ConfigurationDto>

    // Trending
    @GET("trending/{media_type}/{time_window}")
    suspend fun getTrending(
        @Path("media_type") mediaType: String,
        @Path("time_window") timeWindow: String,
        @Query("page") page: Int = 1
    ): Response<PagedResponse<MediaResultDto>>

    // Movies
    @GET("movie/popular")
    suspend fun getPopularMovies(@Query("page") page: Int = 1): Response<PagedResponse<MediaResultDto>>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(@Query("page") page: Int = 1): Response<PagedResponse<MediaResultDto>>

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(@Query("page") page: Int = 1): Response<PagedResponse<MediaResultDto>>

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(@Query("page") page: Int = 1): Response<PagedResponse<MediaResultDto>>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetail(@Path("movie_id") movieId: Int): Response<MovieDetailDto>

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(@Path("movie_id") movieId: Int): Response<CreditsDto>

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(@Path("movie_id") movieId: Int): Response<VideosResponseDto>

    @GET("movie/{movie_id}/similar")
    suspend fun getSimilarMovies(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int = 1
    ): Response<PagedResponse<MediaResultDto>>

    @GET("movie/{movie_id}/recommendations")
    suspend fun getMovieRecommendations(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int = 1
    ): Response<PagedResponse<MediaResultDto>>

    @GET("movie/{movie_id}/watch/providers")
    suspend fun getMovieWatchProviders(@Path("movie_id") movieId: Int): Response<WatchProvidersResponseDto>

    // TV Shows
    @GET("tv/popular")
    suspend fun getPopularTvShows(@Query("page") page: Int = 1): Response<PagedResponse<MediaResultDto>>

    @GET("tv/top_rated")
    suspend fun getTopRatedTvShows(@Query("page") page: Int = 1): Response<PagedResponse<MediaResultDto>>

    @GET("tv/airing_today")
    suspend fun getTvAiringToday(@Query("page") page: Int = 1): Response<PagedResponse<MediaResultDto>>

    @GET("tv/on_the_air")
    suspend fun getTvOnTheAir(@Query("page") page: Int = 1): Response<PagedResponse<MediaResultDto>>

    @GET("tv/{series_id}")
    suspend fun getTvShowDetail(@Path("series_id") seriesId: Int): Response<TvShowDetailDto>

    @GET("tv/{series_id}/credits")
    suspend fun getTvShowCredits(@Path("series_id") seriesId: Int): Response<CreditsDto>

    @GET("tv/{series_id}/videos")
    suspend fun getTvShowVideos(@Path("series_id") seriesId: Int): Response<VideosResponseDto>

    @GET("tv/{series_id}/similar")
    suspend fun getSimilarTvShows(
        @Path("series_id") seriesId: Int,
        @Query("page") page: Int = 1
    ): Response<PagedResponse<MediaResultDto>>

    @GET("tv/{series_id}/recommendations")
    suspend fun getTvShowRecommendations(
        @Path("series_id") seriesId: Int,
        @Query("page") page: Int = 1
    ): Response<PagedResponse<MediaResultDto>>

    @GET("tv/{series_id}/watch/providers")
    suspend fun getTvWatchProviders(@Path("series_id") seriesId: Int): Response<WatchProvidersResponseDto>

    @GET("tv/{series_id}/season/{season_number}")
    suspend fun getSeasonDetail(
        @Path("series_id") seriesId: Int,
        @Path("season_number") seasonNumber: Int
    ): Response<SeasonDetailDto>

    // People
    @GET("person/{person_id}")
    suspend fun getPersonDetail(@Path("person_id") personId: Int): Response<PersonDetailDto>

    @GET("person/{person_id}/combined_credits")
    suspend fun getPersonCombinedCredits(@Path("person_id") personId: Int): Response<CombinedCreditsDto>

    // Search
    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): Response<PagedResponse<MediaResultDto>>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): Response<PagedResponse<MediaResultDto>>

    @GET("search/tv")
    suspend fun searchTvShows(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): Response<PagedResponse<MediaResultDto>>

    @GET("search/person")
    suspend fun searchPeople(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): Response<PagedResponse<MediaResultDto>>

    // Genres
    @GET("genre/movie/list")
    suspend fun getMovieGenres(): Response<GenresResponseDto>

    @GET("genre/tv/list")
    suspend fun getTvGenres(): Response<GenresResponseDto>

    // Discover
    @GET("discover/movie")
    suspend fun discoverMovies(
        @QueryMap params: Map<String, String>,
        @Query("page") page: Int = 1
    ): Response<PagedResponse<MediaResultDto>>

    @GET("discover/tv")
    suspend fun discoverTvShows(
        @QueryMap params: Map<String, String>,
        @Query("page") page: Int = 1
    ): Response<PagedResponse<MediaResultDto>>
}
