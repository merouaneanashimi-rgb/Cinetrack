package com.cinetrack.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notifications_history",
    indices = [
        Index(value = ["createdAt"]),
        Index(value = ["isRead"])
    ]
)
data class NotificationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val title: String,
    val body: String,
    val relatedId: Long? = null,
    val relatedType: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val deepLink: String? = null
)
