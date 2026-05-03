package com.cinetrack.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WatchProvidersResponseDto(
    @Json(name = "id") val id: Int,
    @Json(name = "results") val results: Map<String, CountryProvidersDto>? = null
)

@JsonClass(generateAdapter = true)
data class CountryProvidersDto(
    @Json(name = "link") val link: String? = null,
    @Json(name = "flatrate") val flatrate: List<ProviderDto>? = null,
    @Json(name = "rent") val rent: List<ProviderDto>? = null,
    @Json(name = "buy") val buy: List<ProviderDto>? = null,
    @Json(name = "free") val free: List<ProviderDto>? = null,
    @Json(name = "ads") val ads: List<ProviderDto>? = null
)

@JsonClass(generateAdapter = true)
data class ProviderDto(
    @Json(name = "provider_id") val providerId: Int,
    @Json(name = "provider_name") val providerName: String,
    @Json(name = "logo_path") val logoPath: String? = null,
    @Json(name = "display_priority") val displayPriority: Int = 0
)
