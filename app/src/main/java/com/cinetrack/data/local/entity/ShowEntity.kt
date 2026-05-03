package com.cinetrack.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shows",
    indices = [
        Index(value = ["tmdbId"], unique = true),
        Index(value = ["traktId"], unique = true),
        Index(value = ["userListType"]),
        Index(value = ["airStatus"]),
        Index(value = ["isFavorite"])
    ]
)
data class ShowEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tmdbId: Int,
    val traktId: Int? = null,
    val title: String,
    val originalTitle: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val firstAirDate: String? = null,
    val lastAirDate: String? = null,
    val status: String? = null,
    val airStatus: AirStatus = AirStatus.UPCOMING,
    val nextEpisodeAirDate: String? = null,
    val nextEpisodeNumber: Int? = null,
    val nextEpisodeSeason: Int? = null,
    val nextEpisodeTitle: String? = null,
    val genresJson: String? = null,
    val networksJson: String? = null,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val popularity: Double = 0.0,
    val overview: String? = null,
    val numberOfSeasons: Int = 0,
    val numberOfEpisodes: Int = 0,
    val episodeRuntime: Int = 0,
    val createdByJson: String? = null,
    val userListType: UserListType? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val userRating: Double? = null,
    val userNotes: String? = null,
    val isFavorite: Boolean = false,
    val lastSyncedAt: Long = 0,
    val notifyEnabled: Boolean = false
)

enum class AirStatus {
    RETURNING,
    HIATUS,
    UPCOMING,
    ENDED,
    CANCELED,
    IN_PRODUCTION
}

enum class UserListType {
    WATCHING,
    WATCHLIST,
    WATCHED,
    DROPPED,
    PLAN_TO_WATCH
}
