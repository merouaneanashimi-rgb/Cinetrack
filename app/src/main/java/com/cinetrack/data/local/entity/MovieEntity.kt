package com.cinetrack.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movies",
    indices = [
        Index(value = ["tmdbId"], unique = true),
        Index(value = ["traktId"], unique = true),
        Index(value = ["userListType"]),
        Index(value = ["isFavorite"]),
        Index(value = ["isWatched"])
    ]
)
data class MovieEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tmdbId: Int,
    val traktId: Int? = null,
    val title: String,
    val originalTitle: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val releaseDate: String? = null,
    val runtime: Int? = null,
    val genresJson: String? = null,
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val popularity: Double = 0.0,
    val overview: String? = null,
    val status: String? = null,
    val budget: Long? = null,
    val revenue: Long? = null,
    val productionCompaniesJson: String? = null,
    val userListType: UserListType? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val isWatched: Boolean = false,
    val watchedAt: Long? = null,
    val userRating: Double? = null,
    val userNotes: String? = null,
    val isFavorite: Boolean = false,
    val lastSyncedAt: Long = 0,
    val collectionId: Int? = null,
    val collectionName: String? = null
)
