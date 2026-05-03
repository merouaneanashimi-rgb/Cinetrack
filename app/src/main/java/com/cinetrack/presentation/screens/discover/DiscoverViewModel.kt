package com.cinetrack.presentation.screens.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinetrack.data.remote.api.TmdbApiService
import com.cinetrack.data.remote.dto.MediaResultDto
import com.cinetrack.data.repository.ShowRepositoryImpl
import com.cinetrack.domain.model.AirStatus
import com.cinetrack.domain.model.Show
import com.cinetrack.domain.model.UserListType
import com.cinetrack.domain.repository.ShowRepository
import com.cinetrack.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    private val api: TmdbApiService,
    private val showRepository: ShowRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
        observeContinueWatching()
    }

    private fun observeContinueWatching() {
        viewModelScope.launch {
            showRepository.getShowsByListType(UserListType.WATCHING)
                .collect { shows ->
                    _uiState.update { it.copy(continueWatching = shows) }
                }
        }
    }

    fun loadAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val trending = fetchTrending()
                val popularMovies = fetchPopularMovies()
                val topRatedMovies = fetchTopRatedMovies()
                val nowPlaying = fetchNowPlaying()
                val upcomingMovies = fetchUpcomingMovies()
                val airingThisWeek = fetchAiringThisWeek()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        trendingToday = trending,
                        popularMovies = popularMovies,
                        topRatedMovies = topRatedMovies,
                        nowPlaying = nowPlaying,
                        upcomingMovies = upcomingMovies,
                        airingThisWeek = airingThisWeek,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }

    private suspend fun fetchTrending(): List<MediaItem> {
        return try {
            val response = api.getTrending("all", "day")
            response.body()?.results?.map { it.toMediaItem() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchPopularMovies(): List<MediaItem> {
        return try {
            val response = api.getPopularMovies()
            response.body()?.results?.map { it.toMediaItem() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchTopRatedMovies(): List<MediaItem> {
        return try {
            val response = api.getTopRatedMovies()
            response.body()?.results?.map { it.toMediaItem() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchNowPlaying(): List<MediaItem> {
        return try {
            val response = api.getNowPlayingMovies()
            response.body()?.results?.map { it.toMediaItem() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchUpcomingMovies(): List<MediaItem> {
        return try {
            val response = api.getUpcomingMovies()
            response.body()?.results?.map { it.toMediaItem() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchAiringThisWeek(): List<MediaItem> {
        return try {
            val response = api.getTvOnTheAir()
            response.body()?.results?.map { it.toMediaItem() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun MediaResultDto.toMediaItem(): MediaItem {
        val isMovie = mediaType == "movie" || title != null
        return MediaItem(
            id = id,
            tmdbId = id,
            title = title ?: name ?: "",
            posterPath = posterPath,
            backdropPath = backdropPath,
            year = (releaseDate ?: firstAirDate)?.take(4) ?: "",
            rating = voteAverage,
            genreIds = genreIds ?: emptyList(),
            isMovie = isMovie
        )
    }
}

data class DiscoverUiState(
    val isLoading: Boolean = true,
    val trendingToday: List<MediaItem> = emptyList(),
    val popularMovies: List<MediaItem> = emptyList(),
    val topRatedMovies: List<MediaItem> = emptyList(),
    val nowPlaying: List<MediaItem> = emptyList(),
    val upcomingMovies: List<MediaItem> = emptyList(),
    val airingThisWeek: List<MediaItem> = emptyList(),
    val continueWatching: List<Show> = emptyList(),
    val error: String? = null
)

data class MediaItem(
    val id: Int,
    val tmdbId: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val year: String,
    val rating: Double,
    val genreIds: List<Int>,
    val isMovie: Boolean
)
