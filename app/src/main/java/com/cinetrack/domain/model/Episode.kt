package com.cinetrack.domain.model

data class Episode(
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
    val guestStars: List<String> = emptyList(),
    val isWatched: Boolean = false,
    val watchedAt: Long? = null,
    val userRating: Double? = null,
    val userNotes: String? = null
) {
    val isAired: Boolean
        get() = airDate?.let { date ->
            try {
                val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
                val airDate = java.time.LocalDate.parse(date, formatter)
                !airDate.isAfter(java.time.LocalDate.now())
            } catch (e: Exception) {
                false
            }
        } ?: false
}
