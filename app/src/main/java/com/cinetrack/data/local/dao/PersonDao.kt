package com.cinetrack.data.local.dao

import androidx.room.*
import com.cinetrack.data.local.entity.PersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Query("SELECT * FROM people WHERE id = :id")
    fun getById(id: Long): Flow<PersonEntity?>

    @Query("SELECT * FROM people WHERE tmdbId = :tmdbId")
    suspend fun getByTmdbId(tmdbId: Int): PersonEntity?

    @Query("SELECT * FROM people WHERE isFollowed = 1 ORDER BY followedAt DESC")
    fun getFollowed(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM people WHERE isFollowed = 1")
    suspend fun getFollowedSync(): List<PersonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(person: PersonEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(people: List<PersonEntity>)

    @Update
    suspend fun update(person: PersonEntity)

    @Query("UPDATE people SET isFollowed = :followed, followedAt = :followedAt, lastKnownCreditsJson = :creditsJson WHERE id = :personId")
    suspend fun updateFollowStatus(personId: Long, followed: Boolean, followedAt: Long?, creditsJson: String?)

    @Query("UPDATE people SET lastKnownCreditsJson = :creditsJson WHERE id = :personId")
    suspend fun updateCredits(personId: Long, creditsJson: String?)

    @Delete
    suspend fun delete(person: PersonEntity)
}
