package com.cinetrack.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ConfigurationDto(
    @Json(name = "images") val images: ImageConfigDto? = null,
    @Json(name = "change_keys") val changeKeys: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class ImageConfigDto(
    @Json(name = "base_url") val baseUrl: String? = null,
    @Json(name = "secure_base_url") val secureBaseUrl: String? = null,
    @Json(name = "backdrop_sizes") val backdropSizes: List<String>? = null,
    @Json(name = "logo_sizes") val logoSizes: List<String>? = null,
    @Json(name = "poster_sizes") val posterSizes: List<String>? = null,
    @Json(name = "profile_sizes") val profileSizes: List<String>? = null,
    @Json(name = "still_sizes") val stillSizes: List<String>? = null
)
