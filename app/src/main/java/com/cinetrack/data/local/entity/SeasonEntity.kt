package com.cinetrack.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "seasons",
    foreignKeys = [
        ForeignKey(
            entity = ShowEntity::class,
            parentColumns = ["id"],
            childColumns = ["showId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["showId"]),
        Index(value = ["showId", "seasonNumber"], unique = true)
    ]
)
data class SeasonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val showId: Long,
    val tmdbId: Int? = null,
    val seasonNumber: Int,
    val name: String,
    val posterPath: String? = null,
    val episodeCount: Int = 0,
    val airDate: String? = null,
    val overview: String? = null,
    val isWatched: Boolean = false
)
