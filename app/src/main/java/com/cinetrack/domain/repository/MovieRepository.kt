package com.cinetrack.domain.repository

import com.cinetrack.domain.model.*
import com.cinetrack.util.Result
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getMovieById(id: Long): Flow<Movie?>
    suspend fun getMovieByTmdbId(tmdbId: Int): Movie?
    fun getMoviesByListType(listType: UserListType): Flow<List<Movie>>
    fun getFavoriteMovies(): Flow<List<Movie>>
    fun getWatchedMovies(): Flow<List<Movie>>
    fun getAllTrackedMovies(): Flow<List<Movie>>
    fun getTrackedMovieCount(): Flow<Int>
    suspend fun addMovie(movie: Movie, listType: UserListType): Long
    suspend fun updateMovieListType(movieId: Long, listType: UserListType?)
    suspend fun updateMovieFavorite(movieId: Long, isFavorite: Boolean)
    suspend fun updateMovieWatched(movieId: Long, watched: Boolean)
    suspend fun updateMovieRating(movieId: Long, rating: Double?)
    suspend fun updateMovieNotes(movieId: Long, notes: String?)
    suspend fun removeMovie(movieId: Long)
    suspend fun syncMovieFromApi(tmdbId: Int): Result<Movie>
    suspend fun getCollectionMovies(collectionId: Int, excludeId: Long): List<Movie>
    suspend fun getWatchedMovieCount(): Int
    suspend fun isMovieTracked(tmdbId: Int): Boolean
}
