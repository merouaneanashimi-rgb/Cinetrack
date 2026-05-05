package com.cinetrack.domain.model

data class MediaFilterState(
    val sortBy: String = "popularity.desc",
    val genres: List<Int> = emptyList(),
    val voteAverageGte: Double? = null,
    val runtimeGte: Int? = null,
    val releaseDateGte: String? = null,
    val releaseDateLte: String? = null,
    val status: String? = null,
    val year: Int? = null
)

enum class CollectionSortOrder {
    ADDED_DESC,
    ADDED_ASC,
    ALPHABETICAL_ASC,
    ALPHABETICAL_DESC,
    RELEASE_DATE_DESC,
    RATING_DESC,
    POPULARITY_DESC,
    RUNTIME_DESC
}

data class CollectionSortState(
    val sortOrder: CollectionSortOrder = CollectionSortOrder.ADDED_DESC
)
