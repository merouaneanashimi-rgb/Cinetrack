package com.cinetrack.data.repository

import com.cinetrack.data.local.dao.NotificationHistoryDao
import com.cinetrack.data.local.entity.NotificationHistoryEntity
import com.cinetrack.domain.model.NotificationItem
import com.cinetrack.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationHistoryDao: NotificationHistoryDao
) : NotificationRepository {

    override fun getRecentNotifications(limit: Int): Flow<List<NotificationItem>> {
        return notificationHistoryDao.getRecent(limit).map { list -> list.map { it.toDomainModel() } }
    }

    override fun getUnreadNotifications(): Flow<List<NotificationItem>> {
        return notificationHistoryDao.getUnread().map { list -> list.map { it.toDomainModel() } }
    }

    override fun getUnreadCount(): Flow<Int> {
        return notificationHistoryDao.getUnreadCount()
    }

    override suspend fun addNotification(notification: NotificationItem): Long {
        return notificationHistoryDao.insert(notification.toEntity())
    }

    override suspend fun markAsRead(id: Long) {
        notificationHistoryDao.markAsRead(id)
    }

    override suspend fun markAllAsRead() {
        notificationHistoryDao.markAllAsRead()
    }

    override suspend fun clearOldNotifications(olderThanMillis: Long) {
        notificationHistoryDao.deleteOlderThan(olderThanMillis)
    }

    private fun NotificationHistoryEntity.toDomainModel() = NotificationItem(
        id = id,
        type = type,
        title = title,
        body = body,
        relatedId = relatedId,
        relatedType = relatedType,
        createdAt = createdAt,
        isRead = isRead,
        deepLink = deepLink
    )

    private fun NotificationItem.toEntity() = NotificationHistoryEntity(
        id = id,
        type = type,
        title = title,
        body = body,
        relatedId = relatedId,
        relatedType = relatedType,
        createdAt = createdAt,
        isRead = isRead,
        deepLink = deepLink
    )
}
