package com.cinetrack.domain.model

data class Season(
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
