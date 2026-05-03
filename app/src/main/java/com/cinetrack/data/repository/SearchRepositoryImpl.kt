package com.cinetrack.data.repository

import com.cinetrack.data.local.dao.SearchHistoryDao
import com.cinetrack.data.local.entity.SearchHistoryEntity
import com.cinetrack.data.remote.api.TmdbApiService
import com.cinetrack.data.remote.dto.MediaResultDto
import com.cinetrack.domain.model.SearchResult
import com.cinetrack.domain.repository.SearchRepository
import com.cinetrack.util.Result
import com.cinetrack.util.safeApiCall
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val searchHistoryDao: SearchHistoryDao,
    private val api: TmdbApiService
) : SearchRepository {

    override fun getRecentSearches(): Flow<List<String>> {
        return searchHistoryDao.getRecent().map { list -> list.map { it.query } }
    }

    override suspend fun addSearchQuery(query: String) {
        val existing = searchHistoryDao.findByQuery(query)
        if (existing != null) {
            searchHistoryDao.delete(existing)
        }
        searchHistoryDao.insert(SearchHistoryEntity(query = query))
    }

    override suspend fun removeSearchQuery(query: String) {
        searchHistoryDao.deleteByQuery(query)
    }

    override suspend fun clearSearchHistory() {
        searchHistoryDao.clearAll()
    }

    override suspend fun searchAll(query: String, page: Int): Result<List<SearchResult>> {
        return safeApiCall {
            val response = api.searchMulti(query, page)
            if (response.isSuccessful) {
                response.body()?.results?.map { it.toSearchResult() } ?: emptyList()
            } else {
                throw Exception("API Error: ${response.code()}")
            }
        }
    }

    override suspend fun searchMovies(query: String, page: Int): Result<List<SearchResult>> {
        return safeApiCall {
            val response = api.searchMovies(query, page)
            if (response.isSuccessful) {
                response.body()?.results?.map { it.toSearchResult() } ?: emptyList()
            } else {
                throw Exception("API Error: ${response.code()}")
            }
        }
    }

    override suspend fun searchTvShows(query: String, page: Int): Result<List<SearchResult>> {
        return safeApiCall {
            val response = api.searchTvShows(query, page)
            if (response.isSuccessful) {
                response.body()?.results?.map { it.toSearchResult() } ?: emptyList()
            } else {
                throw Exception("API Error: ${response.code()}")
            }
        }
    }

    override suspend fun searchPeople(query: String, page: Int): Result<List<SearchResult>> {
        return safeApiCall {
            val response = api.searchPeople(query, page)
            if (response.isSuccessful) {
                response.body()?.results?.map { it.toSearchResult() } ?: emptyList()
            } else {
                throw Exception("API Error: ${response.code()}")
            }
        }
    }

    private fun MediaResultDto.toSearchResult(): SearchResult {
        return when (mediaType) {
            "movie" -> SearchResult.MovieResult(
                id = id,
                title = title ?: originalTitle ?: "",
                posterPath = posterPath,
                releaseDate = releaseDate,
                voteAverage = voteAverage
            )
            "tv" -> SearchResult.ShowResult(
                id = id,
                title = name ?: originalName ?: "",
                posterPath = posterPath,
                firstAirDate = firstAirDate,
                voteAverage = voteAverage
            )
            "person" -> SearchResult.PersonResult(
                id = id,
                name = name ?: "",
                profilePath = profilePath,
                knownForDepartment = knownForDepartment
            )
            else -> {
                if (title != null) {
                    SearchResult.MovieResult(
                        id = id,
                        title = title ?: "",
                        posterPath = posterPath,
                        releaseDate = releaseDate,
                        voteAverage = voteAverage
                    )
                } else {
                    SearchResult.ShowResult(
                        id = id,
                        title = name ?: "",
                        posterPath = posterPath,
                        firstAirDate = firstAirDate,
                        voteAverage = voteAverage
                    )
                }
            }
        }
    }
}
