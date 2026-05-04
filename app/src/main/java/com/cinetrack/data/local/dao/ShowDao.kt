package com.cinetrack.data.local.dao

import androidx.room.*
import com.cinetrack.data.local.entity.AirStatus
import com.cinetrack.data.local.entity.ShowEntity
import com.cinetrack.data.local.entity.UserListType
import kotlinx.coroutines.flow.Flow

@Dao
interface ShowDao {

    @Query("SELECT * FROM shows WHERE id = :id")
    fun getById(id: Long): Flow<ShowEntity?>

    @Query("SELECT * FROM shows WHERE tmdbId = :tmdbId")
    suspend fun getByTmdbId(tmdbId: Int): ShowEntity?

    @Query("SELECT * FROM shows WHERE userListType = :listType ORDER BY addedAt DESC")
    fun getByListType(listType: UserListType): Flow<List<ShowEntity>>

    @Query("SELECT * FROM shows WHERE userListType = :listType AND airStatus = :airStatus ORDER BY addedAt DESC")
    fun getByListTypeAndAirStatus(listType: UserListType, airStatus: AirStatus): Flow<List<ShowEntity>>

    @Query("SELECT * FROM shows WHERE userListType = :listType ORDER BY nextEpisodeAirDate ASC")
    fun getWatchingByNextEpisode(listType: UserListType = UserListType.WATCHING): Flow<List<ShowEntity>>

    @Query("SELECT * FROM shows WHERE airStatus = :airStatus AND userListType = :listType ORDER BY nextEpisodeAirDate ASC")
    fun getByAirStatusAndList(airStatus: AirStatus, listType: UserListType = UserListType.WATCHING): Flow<List<ShowEntity>>

    @Query("SELECT * FROM shows WHERE isFavorite = 1 ORDER BY addedAt DESC")
    fun getFavorites(): Flow<List<ShowEntity>>

    @Query("SELECT * FROM shows WHERE userListType IS NOT NULL")
    fun getAllTracked(): Flow<List<ShowEntity>>

    @Query("SELECT COUNT(*) FROM shows WHERE userListType IS NOT NULL")
    fun getTrackedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM shows WHERE userListType = :listType")
    suspend fun getCountByListType(listType: UserListType): Int

    @Query("SELECT * FROM shows WHERE nextEpisodeAirDate IS NOT NULL AND userListType = :listType ORDER BY nextEpisodeAirDate ASC")
    fun getWithUpcomingEpisodes(listType: UserListType = UserListType.WATCHING): Flow<List<ShowEntity>>

    @Query("SELECT * FROM shows WHERE notifyEnabled = 1")
    suspend fun getNotificationEnabled(): List<ShowEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(show: ShowEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shows: List<ShowEntity>)

    @Update
    suspend fun update(show: ShowEntity)

    @Query("UPDATE shows SET userListType = :listType, addedAt = :timestamp WHERE id = :showId")
    suspend fun updateListType(showId: Long, listType: UserListType?, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE shows SET isFavorite = :isFavorite WHERE id = :showId")
    suspend fun updateFavorite(showId: Long, isFavorite: Boolean)

    @Query("UPDATE shows SET userRating = :rating WHERE id = :showId")
    suspend fun updateRating(showId: Long, rating: Double?)

    @Query("UPDATE shows SET userNotes = :notes WHERE id = :showId")
    suspend fun updateNotes(showId: Long, notes: String?)

    @Query("UPDATE shows SET airStatus = :airStatus, nextEpisodeAirDate = :nextDate, nextEpisodeNumber = :nextEpNum, nextEpisodeSeason = :nextSeason, nextEpisodeTitle = :nextTitle, lastSyncedAt = :syncTime WHERE id = :showId")
    suspend fun updateAirStatus(showId: Long, airStatus: AirStatus, nextDate: String?, nextEpNum: Int?, nextSeason: Int?, nextTitle: String?, syncTime: Long = System.currentTimeMillis())

    @Query("UPDATE shows SET notifyEnabled = :enabled WHERE id = :showId")
    suspend fun updateNotifyEnabled(showId: Long, enabled: Boolean)

    @Delete
    suspend fun delete(show: ShowEntity)

    @Query("DELETE FROM shows WHERE id = :showId")
    suspend fun deleteById(showId: Long)

    @Query("DELETE FROM shows WHERE userListType = :listType")
    suspend fun deleteByListType(listType: UserListType)

    @Query("SELECT EXISTS(SELECT 1 FROM shows WHERE tmdbId = :tmdbId AND userListType IS NOT NULL)")
    suspend fun isTracked(tmdbId: Int): Boolean

    @Query("SELECT * FROM shows WHERE userRating IS NOT NULL ORDER BY userRating DESC")
    fun getRatedShows(): kotlinx.coroutines.flow.Flow<List<com.cinetrack.data.local.entity.ShowEntity>>

    @Query("SELECT * FROM shows WHERE tmdbId = :tmdbId LIMIT 1")
    suspend fun getByTmdbId(tmdbId: Int): com.cinetrack.data.local.entity.ShowEntity?

    @Query("UPDATE shows SET userRating = :rating WHERE id = :showId")
    suspend fun updateRating(showId: Long, rating: Double?)

}
