package com.cinetrack.presentation.screens.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinetrack.data.remote.api.TmdbApiService
import com.cinetrack.domain.model.Movie
import com.cinetrack.domain.model.UserListType
import com.cinetrack.domain.repository.MovieRepository
import com.cinetrack.presentation.screens.discover.MediaItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    private val api: TmdbApiService
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _discoverMovies = MutableStateFlow<List<MediaItem>>(emptyList())
    val discoverMovies: StateFlow<List<MediaItem>> = _discoverMovies.asStateFlow()

    val watchlistMovies: StateFlow<List<Movie>> = movieRepository.getMoviesByListType(UserListType.WATCHLIST)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchedMovies: StateFlow<List<Movie>> = movieRepository.getWatchedMovies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteMovies: StateFlow<List<Movie>> = movieRepository.getFavoriteMovies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadDiscoverMovies()
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun loadDiscoverMovies() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getPopularMovies()
                val movies = response.body()?.results?.map { dto ->
                    MediaItem(
                        id = dto.id,
                        tmdbId = dto.id,
                        title = dto.title ?: dto.name ?: "",
                        posterPath = dto.posterPath,
                        backdropPath = dto.backdropPath,
                        year = (dto.releaseDate ?: dto.firstAirDate ?: "").take(4),
                        rating = dto.voteAverage,
                        genreIds = dto.genreIds ?: emptyList(),
                        isMovie = true
                    )
                } ?: emptyList()
                _discoverMovies.value = movies
            } catch (e: Exception) {
                // Error handling
            }
            _isLoading.value = false
        }
    }

    fun addToWatchlist(movie: Movie) {
        viewModelScope.launch {
            movieRepository.addMovie(movie, UserListType.WATCHLIST)
        }
    }

    fun markAsWatched(movieId: Long) {
        viewModelScope.launch {
            movieRepository.updateMovieWatched(movieId, true)
        }
    }

    fun toggleFavorite(movieId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            movieRepository.updateMovieFavorite(movieId, !isFavorite)
        }
    }

    fun removeMovie(movieId: Long) {
        viewModelScope.launch {
            movieRepository.removeMovie(movieId)
        }
    }

    fun rateMovie(movieId: Long, rating: Double) {
        viewModelScope.launch {
            movieRepository.updateMovieRating(movieId, rating)
        }
    }
}
