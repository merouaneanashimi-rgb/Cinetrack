package com.cinetrack.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenresResponseDto(
    @Json(name = "genres") val genres: List<GenreDto>? = null
)
