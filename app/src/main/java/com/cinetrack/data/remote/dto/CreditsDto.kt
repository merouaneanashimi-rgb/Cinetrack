package com.cinetrack.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CreditsDto(
    @Json(name = "id") val id: Int,
    @Json(name = "cast") val cast: List<CastDto>? = null,
    @Json(name = "crew") val crew: List<CrewDto>? = null
)

@JsonClass(generateAdapter = true)
data class CastDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "character") val character: String? = null,
    @Json(name = "profile_path") val profilePath: String? = null,
    @Json(name = "order") val order: Int = 0,
    @Json(name = "gender") val gender: Int = 0,
    @Json(name = "known_for_department") val knownForDepartment: String? = null,
    @Json(name = "popularity") val popularity: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class CrewDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "job") val job: String? = null,
    @Json(name = "department") val department: String? = null,
    @Json(name = "profile_path") val profilePath: String? = null,
    @Json(name = "gender") val gender: Int = 0,
    @Json(name = "popularity") val popularity: Double = 0.0
)
