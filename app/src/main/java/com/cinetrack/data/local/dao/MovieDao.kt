package com.cinetrack.data.local.dao

import androidx.room.*
import com.cinetrack.data.local.entity.MovieEntity
import com.cinetrack.data.local.entity.UserListType
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies WHERE id = :id")
    fun getById(id: Long): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE tmdbId = :tmdbId")
    suspend fun getByTmdbId(tmdbId: Int): MovieEntity?

    @Query("SELECT * FROM movies WHERE userListType = :listType ORDER BY addedAt DESC")
    fun getByListType(listType: UserListType): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isFavorite = 1 ORDER BY addedAt DESC")
    fun getFavorites(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isWatched = 1 ORDER BY watchedAt DESC")
    fun getWatched(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE userListType IS NOT NULL")
    fun getAllTracked(): Flow<List<MovieEntity>>

    @Query("SELECT COUNT(*) FROM movies WHERE userListType IS NOT NULL")
    fun getTrackedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM movies WHERE isWatched = 1")
    suspend fun getWatchedCount(): Int

    @Query("SELECT * FROM movies WHERE collectionId = :collectionId AND id != :excludeId")
    suspend fun getByCollection(collectionId: Int, excludeId: Long): List<MovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: MovieEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<MovieEntity>)

    @Update
    suspend fun update(movie: MovieEntity)

    @Query("UPDATE movies SET userListType = :listType, addedAt = :timestamp WHERE id = :movieId")
    suspend fun updateListType(movieId: Long, listType: UserListType?, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE movies SET isFavorite = :isFavorite WHERE id = :movieId")
    suspend fun updateFavorite(movieId: Long, isFavorite: Boolean)

    @Query("UPDATE movies SET isWatched = :watched, watchedAt = :watchedAt WHERE id = :movieId")
    suspend fun updateWatched(movieId: Long, watched: Boolean, watchedAt: Long?)

    @Query("UPDATE movies SET userRating = :rating WHERE id = :movieId")
    suspend fun updateRating(movieId: Long, rating: Double?)

    @Query("UPDATE movies SET userNotes = :notes WHERE id = :movieId")
    suspend fun updateNotes(movieId: Long, notes: String?)

    @Delete
    suspend fun delete(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE id = :movieId")
    suspend fun deleteById(movieId: Long)

    @Query("DELETE FROM movies WHERE userListType = :listType")
    suspend fun deleteByListType(listType: UserListType)

    @Query("SELECT EXISTS(SELECT 1 FROM movies WHERE tmdbId = :tmdbId AND userListType IS NOT NULL)")
    suspend fun isTracked(tmdbId: Int): Boolean
}
