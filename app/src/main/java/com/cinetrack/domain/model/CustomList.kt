package com.cinetrack.domain.model

data class CustomList(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val emoji: String? = null,
    val colorHex: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
    val filterJson: String? = null,
    val isPublic: Boolean = false
)

data class CustomListItem(
    val id: Long = 0,
    val listId: Long,
    val mediaId: Long,
    val mediaType: MediaType,
    val addedAt: Long = System.currentTimeMillis(),
    val sortIndex: Int = 0
)

enum class MediaType {
    MOVIE,
    SHOW
}
