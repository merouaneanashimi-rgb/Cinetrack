package com.cinetrack.data.local.dao

import androidx.room.*
import com.cinetrack.data.local.entity.SeasonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SeasonDao {

    @Query("SELECT * FROM seasons WHERE showId = :showId ORDER BY seasonNumber ASC")
    fun getByShowId(showId: Long): Flow<List<SeasonEntity>>

    @Query("SELECT * FROM seasons WHERE showId = :showId ORDER BY seasonNumber ASC")
    suspend fun getByShowIdSync(showId: Long): List<SeasonEntity>

    @Query("SELECT * FROM seasons WHERE id = :id")
    suspend fun getById(id: Long): SeasonEntity?

    @Query("SELECT * FROM seasons WHERE showId = :showId AND seasonNumber = :seasonNumber")
    suspend fun getByShowAndNumber(showId: Long, seasonNumber: Int): SeasonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(season: SeasonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(seasons: List<SeasonEntity>)

    @Update
    suspend fun update(season: SeasonEntity)

    @Query("UPDATE seasons SET isWatched = :watched WHERE id = :seasonId")
    suspend fun updateWatchedStatus(seasonId: Long, watched: Boolean)

    @Delete
    suspend fun delete(season: SeasonEntity)

    @Query("DELETE FROM seasons WHERE showId = :showId")
    suspend fun deleteByShow(showId: Long)
}
