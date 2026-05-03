package com.cinetrack.domain.model

sealed class SearchResult {
    abstract val id: Int

    data class MovieResult(
        override val id: Int,
        val title: String,
        val posterPath: String?,
        val releaseDate: String?,
        val voteAverage: Double
    ) : SearchResult()

    data class ShowResult(
        override val id: Int,
        val title: String,
        val posterPath: String?,
        val firstAirDate: String?,
        val voteAverage: Double
    ) : SearchResult()

    data class PersonResult(
        override val id: Int,
        val name: String,
        val profilePath: String?,
        val knownForDepartment: String?
    ) : SearchResult()
}
