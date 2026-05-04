package com.cinetrack.presentation.screens.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinetrack.data.remote.api.TmdbApiService
import com.cinetrack.domain.model.*
import com.cinetrack.presentation.screens.discover.MediaItem
import com.cinetrack.domain.repository.MovieRepository
import com.cinetrack.util.Result
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

    val watchlistMovies: StateFlow<List<Movie>> = combine(
        movieRepository.getMoviesByListType(UserListType.WATCHLIST),
        collectionSortState
    ) { movies, sortOrder -> sortMovies(movies, sortOrder) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchedMovies: StateFlow<List<Movie>> = combine(
        movieRepository.getWatchedMovies(),
        collectionSortState
    ) { movies, sortOrder -> sortMovies(movies, sortOrder) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteMovies: StateFlow<List<Movie>> = combine(
        movieRepository.getFavoriteMovies(),
        collectionSortState
    ) { movies, sortOrder -> sortMovies(movies, sortOrder) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun sortMovies(movies: List<Movie>, order: CollectionSortOrder): List<Movie> {
        return when (order) {
            CollectionSortOrder.ADDED_DESC -> movies.sortedByDescending { it.id }
            CollectionSortOrder.ADDED_ASC -> movies.sortedBy { it.id }
            CollectionSortOrder.ALPHABETICAL_ASC -> movies.sortedBy { it.title }
            CollectionSortOrder.ALPHABETICAL_DESC -> movies.sortedByDescending { it.title }
            CollectionSortOrder.RELEASE_DATE_DESC -> movies.sortedByDescending { it.releaseDate }
            CollectionSortOrder.RATING_DESC -> movies.sortedByDescending { it.voteAverage }
        }
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _filterState = MutableStateFlow(MediaFilterState())
    val filterState: StateFlow<MediaFilterState> = _filterState.asStateFlow()

    private val _collectionSortState = MutableStateFlow(CollectionSortOrder.ADDED_DESC)
    val collectionSortState: StateFlow<CollectionSortOrder> = _collectionSortState.asStateFlow()

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    init {
        fetchGenres()
        loadDiscoverMovies()
    }

    private fun fetchGenres() {
        viewModelScope.launch {
            try {
                val response = api.getMovieGenres()
                if (response.isSuccessful) {
                    _genres.value = response.body()?.genres?.map { Genre(it.id, it.name) } ?: emptyList()
                }
            } catch (e: Exception) {}
        }
    }

    fun updateFilter(newState: MediaFilterState) {
        _filterState.value = newState
        loadDiscoverMovies(loadMore = false)
    }

    fun updateSortOrder(newOrder: CollectionSortOrder) {
        _collectionSortState.value = newOrder
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    private var currentPage = 1
    private var isLastPage = false

    fun loadDiscoverMovies(loadMore: Boolean = false) {
        if (loadMore && (isLoading.value || isLastPage)) return
        if (!loadMore) {
            currentPage = 1
            isLastPage = false
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val params = mutableMapOf<String, String>()
                val filter = _filterState.value
                
                params["sort_by"] = filter.sortBy
                if (filter.genres.isNotEmpty()) {
                    params["with_genres"] = filter.genres.joinToString(",")
                }
                filter.voteAverageGte?.let { params["vote_average.gte"] = it.toString() }
                filter.runtimeGte?.let { params["with_runtime.gte"] = it.toString() }
                filter.releaseDateGte?.let { params["primary_release_date.gte"] = it }

                val isFiltering = filter != MediaFilterState()
                
                val response = if (isFiltering) {
                    api.discoverMovies(params, page = currentPage)
                } else {
                    api.getPopularMovies(page = currentPage)
                }
                if (response.isSuccessful) {
                    val pagedResponse = response.body()
                    val newItems = pagedResponse?.results?.map { dto ->
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

                    if (newItems.isEmpty()) {
                        isLastPage = true
                    } else {
                        if (loadMore) {
                            _discoverMovies.value = _discoverMovies.value + newItems
                        } else {
                            _discoverMovies.value = newItems
                        }
                        currentPage++
                    }
                }
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
