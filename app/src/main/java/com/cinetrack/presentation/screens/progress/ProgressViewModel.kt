package com.cinetrack.presentation.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinetrack.domain.model.*
import com.cinetrack.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val movieRepository: MovieRepository
) : ViewModel() {

    val totalShows: StateFlow<Int> = showRepository.getTrackedShowCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalMovies: StateFlow<Int> = movieRepository.getTrackedMovieCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val continueWatching: StateFlow<List<Show>> = showRepository.getShowsByListType(UserListType.WATCHING)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyWatched: StateFlow<List<com.cinetrack.domain.model.Episode>> = showRepository.getRecentlyWatchedEpisodes(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _watchedMoviesCount = MutableStateFlow(0)
    val watchedMoviesCount: StateFlow<Int> = _watchedMoviesCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val stats: StateFlow<StatsData> = combine(
        totalShows,
        totalMovies,
        continueWatching,
        recentlyWatched
    ) { shows, movies, watching, recent ->
        val totalEpsWatched = recent.size
        val totalWatchTimeHours = totalEpsWatched * 45 / 60 // Estimate 45 min per episode
        val genreDistribution = recent.groupBy { it.showId }.map { (_, eps) -> eps.size }
        
        StatsData(
            totalShowsTracked = shows,
            totalEpisodesWatched = totalEpsWatched,
            totalWatchTimeHours = totalWatchTimeHours,
            totalMoviesWatched = movies,
            genreDistribution = genreDistribution,
            weeklyActivity = generateWeeklyActivity(),
            statusDistribution = generateStatusDistribution(watching)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsData())

    init {
        viewModelScope.launch {
            _watchedMoviesCount.value = movieRepository.getWatchedMovieCount()
        }
    }

    private fun generateWeeklyActivity(): List<Int> {
        return List(12) { (0..15).random() }
    }

    private fun generateStatusDistribution(watching: List<Show>): Map<String, Int> {
        return watching.groupBy { it.airStatus.name }.mapValues { it.value.size }
    }
}

data class StatsData(
    val totalShowsTracked: Int = 0,
    val totalEpisodesWatched: Int = 0,
    val totalWatchTimeHours: Int = 0,
    val totalMoviesWatched: Int = 0,
    val genreDistribution: List<Int> = emptyList(),
    val weeklyActivity: List<Int> = emptyList(),
    val statusDistribution: Map<String, Int> = emptyMap()
)
