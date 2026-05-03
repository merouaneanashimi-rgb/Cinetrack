package com.cinetrack.domain.repository

import com.cinetrack.domain.model.SearchResult
import com.cinetrack.util.Result
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun getRecentSearches(): Flow<List<String>>
    suspend fun addSearchQuery(query: String)
    suspend fun removeSearchQuery(query: String)
    suspend fun clearSearchHistory()
    suspend fun searchAll(query: String, page: Int = 1): Result<List<SearchResult>>
    suspend fun searchMovies(query: String, page: Int = 1): Result<List<SearchResult>>
    suspend fun searchTvShows(query: String, page: Int = 1): Result<List<SearchResult>>
    suspend fun searchPeople(query: String, page: Int = 1): Result<List<SearchResult>>
}
