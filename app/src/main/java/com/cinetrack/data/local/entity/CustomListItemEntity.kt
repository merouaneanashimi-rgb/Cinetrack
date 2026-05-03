package com.cinetrack.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "custom_list_items",
    foreignKeys = [
        ForeignKey(
            entity = CustomListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["listId"]),
        Index(value = ["listId", "mediaId", "mediaType"], unique = true)
    ]
)
data class CustomListItemEntity(
    @PrimaryKey(autoGenerate = true)
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
