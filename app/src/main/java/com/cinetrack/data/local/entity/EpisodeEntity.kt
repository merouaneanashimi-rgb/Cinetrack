package com.cinetrack.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "episodes",
    foreignKeys = [
        ForeignKey(
            entity = ShowEntity::class,
            parentColumns = ["id"],
            childColumns = ["showId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SeasonEntity::class,
            parentColumns = ["id"],
            childColumns = ["seasonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["showId"]),
        Index(value = ["seasonId"]),
        Index(value = ["showId", "seasonNumber", "episodeNumber"], unique = true)
    ]
)
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val showId: Long,
    val seasonId: Long,
    val tmdbId: Int? = null,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val name: String,
    val overview: String? = null,
    val airDate: String? = null,
    val runtime: Int? = null,
    val stillPath: String? = null,
    val guestStarsJson: String? = null,
    val isWatched: Boolean = false,
    val watchedAt: Long? = null,
    val userRating: Double? = null,
    val userNotes: String? = null
)
