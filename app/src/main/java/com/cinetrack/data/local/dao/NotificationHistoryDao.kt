package com.cinetrack.data.local.dao

import androidx.room.*
import com.cinetrack.data.local.entity.NotificationHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationHistoryDao {

    @Query("SELECT * FROM notifications_history ORDER BY createdAt DESC LIMIT :limit")
    fun getRecent(limit: Int = 50): Flow<List<NotificationHistoryEntity>>

    @Query("SELECT * FROM notifications_history WHERE isRead = 0 ORDER BY createdAt DESC")
    fun getUnread(): Flow<List<NotificationHistoryEntity>>

    @Query("SELECT COUNT(*) FROM notifications_history WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert
    suspend fun insert(notification: NotificationHistoryEntity): Long

    @Query("UPDATE notifications_history SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications_history SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications_history WHERE createdAt < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long)

    @Delete
    suspend fun delete(notification: NotificationHistoryEntity)
}
