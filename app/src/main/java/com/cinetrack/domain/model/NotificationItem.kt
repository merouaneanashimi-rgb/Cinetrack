package com.cinetrack.domain.model

data class NotificationItem(
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
