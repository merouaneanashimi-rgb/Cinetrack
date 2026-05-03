package com.cinetrack.domain.repository

import com.cinetrack.domain.model.NotificationItem
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getRecentNotifications(limit: Int = 50): Flow<List<NotificationItem>>
    fun getUnreadNotifications(): Flow<List<NotificationItem>>
    fun getUnreadCount(): Flow<Int>
    suspend fun addNotification(notification: NotificationItem): Long
    suspend fun markAsRead(id: Long)
    suspend fun markAllAsRead()
    suspend fun clearOldNotifications(olderThanMillis: Long)
}
