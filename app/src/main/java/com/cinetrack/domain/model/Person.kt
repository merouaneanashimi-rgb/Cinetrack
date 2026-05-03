package com.cinetrack.domain.model

data class Person(
    val id: Long = 0,
    val tmdbId: Int,
    val name: String,
    val profilePath: String? = null,
    val biography: String? = null,
    val birthday: String? = null,
    val deathday: String? = null,
    val placeOfBirth: String? = null,
    val knownForDepartment: String? = null,
    val isFollowed: Boolean = false
)

data class Credit(
    val id: Int,
    val title: String,
    val mediaType: String,
    val character: String? = null,
    val job: String? = null,
    val department: String? = null,
    val posterPath: String? = null,
    val releaseDate: String? = null,
    val voteAverage: Double = 0.0
)
