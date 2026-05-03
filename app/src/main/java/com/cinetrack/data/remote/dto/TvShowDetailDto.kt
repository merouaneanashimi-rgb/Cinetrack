package com.cinetrack.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TvShowDetailDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "original_name") val originalName: String? = null,
    @Json(name = "tagline") val tagline: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "last_air_date") val lastAirDate: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "genres") val genres: List<GenreDto>? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0,
    @Json(name = "vote_count") val voteCount: Int = 0,
    @Json(name = "popularity") val popularity: Double = 0.0,
    @Json(name = "number_of_seasons") val numberOfSeasons: Int = 0,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int = 0,
    @Json(name = "episode_run_time") val episodeRunTime: List<Int>? = null,
    @Json(name = "created_by") val createdBy: List<CreatorDto>? = null,
    @Json(name = "networks") val networks: List<NetworkDto>? = null,
    @Json(name = "next_episode_to_air") val nextEpisodeToAir: EpisodeAirDto? = null,
    @Json(name = "last_episode_to_air") val lastEpisodeToAir: EpisodeAirDto? = null,
    @Json(name = "in_production") val inProduction: Boolean = false,
    @Json(name = "type") val type: String? = null,
    @Json(name = "homepage") val homepage: String? = null,
    @Json(name = "original_language") val originalLanguage: String? = null,
    @Json(name = "origin_country") val originCountry: List<String>? = null,
    @Json(name = "spoken_languages") val spokenLanguages: List<LanguageDto>? = null,
    @Json(name = "seasons") val seasons: List<SeasonSummaryDto>? = null,
    @Json(name = "production_companies") val productionCompanies: List<ProductionCompanyDto>? = null
)

@JsonClass(generateAdapter = true)
data class CreatorDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "profile_path") val profilePath: String? = null
)

@JsonClass(generateAdapter = true)
data class NetworkDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "logo_path") val logoPath: String? = null,
    @Json(name = "origin_country") val originCountry: String? = null
)

@JsonClass(generateAdapter = true)
data class EpisodeAirDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "air_date") val airDate: String? = null,
    @Json(name = "episode_number") val episodeNumber: Int? = null,
    @Json(name = "season_number") val seasonNumber: Int? = null,
    @Json(name = "runtime") val runtime: Int? = null,
    @Json(name = "still_path") val stillPath: String? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class SeasonSummaryDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "air_date") val airDate: String? = null,
    @Json(name = "season_number") val seasonNumber: Int,
    @Json(name = "episode_count") val episodeCount: Int = 0,
    @Json(name = "poster_path") val posterPath: String? = null
)
