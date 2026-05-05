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
        showRepository.getAllTrackedShows(),
        movieRepository.getWatchedMovies(),
        showRepository.getAllWatchedEpisodes(),
        recentlyWatched
    ) { allShows, watchedMovies, allWatchedEps, recent ->
        val totalEpsWatched = allWatchedEps.size
        val movieWatchTime = watchedMovies.sumOf { it.runtime ?: 100 }
        val showWatchTime = allWatchedEps.sumOf { it.runtime ?: 45 }
        val totalWatchTimeMinutes = movieWatchTime + showWatchTime
        val totalWatchTimeHours = totalWatchTimeMinutes / 60
        
        StatsData(
            totalShowsTracked = allShows.size,
            totalEpisodesWatched = totalEpsWatched,
            totalWatchTimeHours = totalWatchTimeHours,
            totalMoviesWatched = watchedMovies.size,
            weeklyActivity = calculateWeeklyActivity(recent),
            statusDistribution = calculateStatusDistribution(allShows)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsData())

    init {
        // Initialization if needed
    }

    private fun calculateWeeklyActivity(recent: List<Episode>): List<Int> {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 3600 * 1000L
        val activity = MutableList(7) { 0 }
        
        recent.forEach { episode ->
            episode.watchedAt?.let { watchedAt ->
                val daysAgo = ((now - watchedAt) / oneDayMillis).toInt()
                if (daysAgo in 0..6) {
                    activity[6 - daysAgo]++
                }
            }
        }
        return activity
    }

    private fun calculateStatusDistribution(allShows: List<Show>): Map<String, Int> {
        return allShows.groupBy { it.airStatus.name }.mapValues { it.value.size }
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
