package com.cinetrack.domain.model

data class Show(
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
    val genres: List<String> = emptyList(),
    val networks: List<String> = emptyList(),
    val voteAverage: Double = 0.0,
    val voteCount: Int = 0,
    val popularity: Double = 0.0,
    val overview: String? = null,
    val numberOfSeasons: Int = 0,
    val numberOfEpisodes: Int = 0,
    val episodeRuntime: Int = 0,
    val userListType: UserListType? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val userRating: Double? = null,
    val userNotes: String? = null,
    val isFavorite: Boolean = false,
    val lastSyncedAt: Long = 0,
    val notifyEnabled: Boolean = false
) {
    val watchProgress: Float
        get() = if (numberOfEpisodes > 0) watchedEpisodes.toFloat() / numberOfEpisodes else 0f

    val watchedEpisodes: Int = 0 // Computed from episodes
    val totalWatchTimeMinutes: Int = watchedEpisodes * episodeRuntime
}

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

fun AirStatus.toDisplayName(): String = when (this) {
    AirStatus.RETURNING -> "AIRING"
    AirStatus.HIATUS -> "HIATUS"
    AirStatus.UPCOMING -> "UPCOMING"
    AirStatus.ENDED -> "ENDED"
    AirStatus.CANCELED -> "CANCELED"
    AirStatus.IN_PRODUCTION -> "IN PRODUCTION"
}

fun AirStatus.toColor(): Long = when (this) {
    AirStatus.RETURNING -> 0xFF4CAF50
    AirStatus.HIATUS -> 0xFFFFC107
    AirStatus.UPCOMING -> 0xFF2196F3
    AirStatus.ENDED -> 0xFF9E9E9E
    AirStatus.CANCELED -> 0xFFF44336
    AirStatus.IN_PRODUCTION -> 0xFF9C27B0
}
