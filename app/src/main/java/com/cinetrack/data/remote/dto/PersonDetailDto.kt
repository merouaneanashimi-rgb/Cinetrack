package com.cinetrack.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PersonDetailDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "also_known_as") val alsoKnownAs: List<String>? = null,
    @Json(name = "biography") val biography: String? = null,
    @Json(name = "birthday") val birthday: String? = null,
    @Json(name = "deathday") val deathday: String? = null,
    @Json(name = "gender") val gender: Int = 0,
    @Json(name = "homepage") val homepage: String? = null,
    @Json(name = "imdb_id") val imdbId: String? = null,
    @Json(name = "known_for_department") val knownForDepartment: String? = null,
    @Json(name = "place_of_birth") val placeOfBirth: String? = null,
    @Json(name = "popularity") val popularity: Double = 0.0,
    @Json(name = "profile_path") val profilePath: String? = null
)

@JsonClass(generateAdapter = true)
data class CombinedCreditsDto(
    @Json(name = "id") val id: Int,
    @Json(name = "cast") val cast: List<CreditRoleDto>? = null,
    @Json(name = "crew") val crew: List<CreditRoleDto>? = null
)

@JsonClass(generateAdapter = true)
data class CreditRoleDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "original_title") val originalTitle: String? = null,
    @Json(name = "original_name") val originalName: String? = null,
    @Json(name = "character") val character: String? = null,
    @Json(name = "job") val job: String? = null,
    @Json(name = "department") val department: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "media_type") val mediaType: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0,
    @Json(name = "episode_count") val episodeCount: Int? = null,
    @Json(name = "popularity") val popularity: Double = 0.0
)
