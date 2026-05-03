package com.cinetrack.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MovieDetailDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "original_title") val originalTitle: String? = null,
    @Json(name = "tagline") val tagline: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "runtime") val runtime: Int? = null,
    @Json(name = "genres") val genres: List<GenreDto>? = null,
    @Json(name = "vote_average") val voteAverage: Double = 0.0,
    @Json(name = "vote_count") val voteCount: Int = 0,
    @Json(name = "popularity") val popularity: Double = 0.0,
    @Json(name = "status") val status: String? = null,
    @Json(name = "budget") val budget: Long? = null,
    @Json(name = "revenue") val revenue: Long? = null,
    @Json(name = "production_companies") val productionCompanies: List<ProductionCompanyDto>? = null,
    @Json(name = "belongs_to_collection") val belongsToCollection: CollectionDto? = null,
    @Json(name = "adult") val adult: Boolean = false,
    @Json(name = "homepage") val homepage: String? = null,
    @Json(name = "imdb_id") val imdbId: String? = null,
    @Json(name = "original_language") val originalLanguage: String? = null,
    @Json(name = "spoken_languages") val spokenLanguages: List<LanguageDto>? = null
)

@JsonClass(generateAdapter = true)
data class GenreDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class ProductionCompanyDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "logo_path") val logoPath: String? = null,
    @Json(name = "origin_country") val originCountry: String? = null
)

@JsonClass(generateAdapter = true)
data class CollectionDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null
)

@JsonClass(generateAdapter = true)
data class LanguageDto(
    @Json(name = "english_name") val englishName: String? = null,
    @Json(name = "iso_639_1") val isoCode: String? = null,
    @Json(name = "name") val name: String? = null
)
