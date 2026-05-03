package com.cinetrack.data.local.dao

import androidx.room.*
import com.cinetrack.data.local.entity.CustomListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomListDao {

    @Query("SELECT * FROM custom_lists ORDER BY sortOrder ASC, createdAt DESC")
    fun getAll(): Flow<List<CustomListEntity>>

    @Query("SELECT * FROM custom_lists WHERE id = :id")
    fun getById(id: Long): Flow<CustomListEntity?>

    @Query("SELECT * FROM custom_lists WHERE id = :id")
    suspend fun getByIdSync(id: Long): CustomListEntity?

    @Query("SELECT COUNT(*) FROM custom_lists")
    suspend fun getCount(): Int

    @Insert
    suspend fun insert(list: CustomListEntity): Long

    @Update
    suspend fun update(list: CustomListEntity)

    @Query("UPDATE custom_lists SET sortOrder = :order WHERE id = :listId")
    suspend fun updateSortOrder(listId: Long, order: Int)

    @Delete
    suspend fun delete(list: CustomListEntity)

    @Query("DELETE FROM custom_lists WHERE id = :listId")
    suspend fun deleteById(listId: Long)
}
