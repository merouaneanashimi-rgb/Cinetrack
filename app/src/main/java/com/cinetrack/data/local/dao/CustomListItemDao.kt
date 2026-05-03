package com.cinetrack.data.local.dao

import androidx.room.*
import com.cinetrack.data.local.entity.CustomListItemEntity
import com.cinetrack.data.local.entity.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomListItemDao {

    @Query("SELECT * FROM custom_list_items WHERE listId = :listId ORDER BY sortIndex ASC, addedAt DESC")
    fun getByListId(listId: Long): Flow<List<CustomListItemEntity>>

    @Query("SELECT * FROM custom_list_items WHERE listId = :listId ORDER BY sortIndex ASC, addedAt DESC")
    suspend fun getByListIdSync(listId: Long): List<CustomListItemEntity>

    @Query("SELECT * FROM custom_list_items WHERE mediaId = :mediaId AND mediaType = :mediaType")
    suspend fun getByMedia(mediaId: Long, mediaType: MediaType): List<CustomListItemEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM custom_list_items WHERE listId = :listId AND mediaId = :mediaId AND mediaType = :mediaType)")
    suspend fun existsInList(listId: Long, mediaId: Long, mediaType: MediaType): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: CustomListItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<CustomListItemEntity>)

    @Query("UPDATE custom_list_items SET sortIndex = :index WHERE id = :itemId")
    suspend fun updateSortIndex(itemId: Long, index: Int)

    @Delete
    suspend fun delete(item: CustomListItemEntity)

    @Query("DELETE FROM custom_list_items WHERE listId = :listId AND mediaId = :mediaId AND mediaType = :mediaType")
    suspend fun deleteFromList(listId: Long, mediaId: Long, mediaType: MediaType)

    @Query("DELETE FROM custom_list_items WHERE listId = :listId")
    suspend fun deleteByList(listId: Long)

    @Query("SELECT COUNT(*) FROM custom_list_items WHERE listId = :listId")
    suspend fun getCountByList(listId: Long): Int
}
