package com.cinetrack.data.local.dao

import androidx.room.*
import com.cinetrack.data.local.entity.EpisodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EpisodeDao {

    @Query("SELECT * FROM episodes WHERE showId = :showId ORDER BY seasonNumber ASC, episodeNumber ASC")
    fun getByShowId(showId: Long): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE seasonId = :seasonId ORDER BY episodeNumber ASC")
    fun getBySeasonId(seasonId: Long): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE seasonId = :seasonId ORDER BY episodeNumber ASC")
    suspend fun getBySeasonIdSync(seasonId: Long): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE showId = :showId AND seasonNumber = :seasonNumber ORDER BY episodeNumber ASC")
    suspend fun getByShowAndSeasonSync(showId: Long, seasonNumber: Int): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE id = :id")
    suspend fun getById(id: Long): EpisodeEntity?

    @Query("SELECT COUNT(*) FROM episodes WHERE showId = :showId AND isWatched = 1")
    suspend fun getWatchedCountByShow(showId: Long): Int

    @Query("SELECT COUNT(*) FROM episodes WHERE showId = :showId")
    suspend fun getTotalCountByShow(showId: Long): Int

    @Query("SELECT * FROM episodes WHERE showId = :showId AND isWatched = 0 AND airDate IS NOT NULL AND airDate <= date('now') ORDER BY seasonNumber ASC, episodeNumber ASC LIMIT 1")
    suspend fun getNextUnwatched(showId: Long): EpisodeEntity?

    @Query("SELECT * FROM episodes WHERE showId = :showId AND isWatched = 1 ORDER BY watchedAt DESC LIMIT :limit")
    suspend fun getRecentlyWatchedByShow(showId: Long, limit: Int = 1): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE isWatched = 1 ORDER BY watchedAt DESC LIMIT :limit")
    fun getRecentlyWatched(limit: Int = 50): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE showId = :showId AND airDate = :date")
    suspend fun getEpisodesAiringOn(showId: Long, date: String): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE airDate = :date AND isWatched = 0")
    suspend fun getEpisodesAiringOnDate(date: String): List<EpisodeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(episode: EpisodeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(episodes: List<EpisodeEntity>)

    @Update
    suspend fun update(episode: EpisodeEntity)

    @Query("UPDATE episodes SET isWatched = :watched, watchedAt = :watchedAt WHERE id = :episodeId")
    suspend fun updateWatchedStatus(episodeId: Long, watched: Boolean, watchedAt: Long?)

    @Query("UPDATE episodes SET isWatched = 1, watchedAt = :watchedAt WHERE seasonId = :seasonId")
    suspend fun markSeasonWatched(seasonId: Long, watchedAt: Long = System.currentTimeMillis())

    @Query("UPDATE episodes SET isWatched = 0, watchedAt = NULL WHERE seasonId = :seasonId")
    suspend fun markSeasonUnwatched(seasonId: Long)

    @Query("UPDATE episodes SET isWatched = 1, watchedAt = :watchedAt WHERE showId = :showId AND seasonNumber = :seasonNumber AND episodeNumber <= :episodeNumber")
    suspend fun markUpToEpisodeWatched(showId: Long, seasonNumber: Int, episodeNumber: Int, watchedAt: Long = System.currentTimeMillis())

    @Query("UPDATE episodes SET isWatched = 1, watchedAt = :watchedAt WHERE showId = :showId AND ((seasonNumber = :seasonNumber AND episodeNumber >= :episodeNumber) OR seasonNumber > :seasonNumber)")
    suspend fun markFromEpisodeWatched(showId: Long, seasonNumber: Int, episodeNumber: Int, watchedAt: Long = System.currentTimeMillis())

    @Query("UPDATE episodes SET isWatched = 1, watchedAt = :watchedAt WHERE showId = :showId")
    suspend fun markAllShowEpisodesWatched(showId: Long, watchedAt: Long = System.currentTimeMillis())

    @Query("UPDATE episodes SET userRating = :rating WHERE id = :episodeId")
    suspend fun updateRating(episodeId: Long, rating: Double?)

    @Query("UPDATE episodes SET userNotes = :notes WHERE id = :episodeId")
    suspend fun updateNotes(episodeId: Long, notes: String?)

    @Delete
    suspend fun delete(episode: EpisodeEntity)

    @Query("DELETE FROM episodes WHERE showId = :showId")
    suspend fun deleteByShow(showId: Long)

    @androidx.room.Query("SELECT * FROM episodes WHERE isWatched = 1 ORDER BY watchedAt DESC")
    fun getWatchedEpisodes(): kotlinx.coroutines.flow.Flow<List<com.cinetrack.data.local.entity.EpisodeEntity>>

    @androidx.room.Query("SELECT * FROM episodes WHERE showId = :showId AND seasonNumber = :season AND episodeNumber = :episode LIMIT 1")
    suspend fun getByShowSeasonEpisode(showId: Long, season: Int, episode: Int): com.cinetrack.data.local.entity.EpisodeEntity?

    @androidx.room.Query("UPDATE episodes SET isWatched = :watched, watchedAt = :watchedAt WHERE id = :episodeId")
    suspend fun markWatched(episodeId: Long, watched: Boolean, watchedAt: Long?)

}
