package com.cinetrack.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideosResponseDto(
    @Json(name = "id") val id: Int,
    @Json(name = "results") val results: List<VideoDto>? = null
)

@JsonClass(generateAdapter = true)
data class VideoDto(
    @Json(name = "id") val id: String,
    @Json(name = "key") val key: String,
    @Json(name = "name") val name: String? = null,
    @Json(name = "site") val site: String? = null,
    @Json(name = "type") val type: String? = null,
    @Json(name = "official") val official: Boolean = false,
    @Json(name = "published_at") val publishedAt: String? = null,
    @Json(name = "size") val size: Int = 0
)
