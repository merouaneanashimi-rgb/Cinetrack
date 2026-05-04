package com.cinetrack.presentation.screens.tvshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinetrack.data.remote.api.TmdbApiService
import com.cinetrack.domain.model.*
import com.cinetrack.domain.repository.ShowRepository
import com.cinetrack.presentation.screens.discover.MediaItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvShowsViewModel @Inject constructor(
    private val showRepository: ShowRepository,
    private val api: TmdbApiService
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedSubTab = MutableStateFlow(0)
    val selectedSubTab: StateFlow<Int> = _selectedSubTab.asStateFlow()

    private val _discoverShows = MutableStateFlow<List<MediaItem>>(emptyList())
    val discoverShows: StateFlow<List<MediaItem>> = _discoverShows.asStateFlow()

    val watchingShows: StateFlow<List<Show>> = combine(
        showRepository.getShowsByListType(UserListType.WATCHING),
        collectionSortState
    ) { shows, sortOrder -> sortShows(shows, sortOrder) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlistShows: StateFlow<List<Show>> = combine(
        showRepository.getShowsByListType(UserListType.WATCHLIST),
        collectionSortState
    ) { shows, sortOrder -> sortShows(shows, sortOrder) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchedShows: StateFlow<List<Show>> = combine(
        showRepository.getShowsByListType(UserListType.WATCHED),
        collectionSortState
    ) { shows, sortOrder -> sortShows(shows, sortOrder) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val droppedShows: StateFlow<List<Show>> = combine(
        showRepository.getShowsByListType(UserListType.DROPPED),
        collectionSortState
    ) { shows, sortOrder -> sortShows(shows, sortOrder) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val planToWatchShows: StateFlow<List<Show>> = combine(
        showRepository.getShowsByListType(UserListType.PLAN_TO_WATCH),
        collectionSortState
    ) { shows, sortOrder -> sortShows(shows, sortOrder) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteShows: StateFlow<List<Show>> = combine(
        showRepository.getFavoriteShows(),
        collectionSortState
    ) { shows, sortOrder -> sortShows(shows, sortOrder) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun sortShows(shows: List<Show>, order: CollectionSortOrder): List<Show> {
        return when (order) {
            CollectionSortOrder.ADDED_DESC -> shows.sortedByDescending { it.id }
            CollectionSortOrder.ADDED_ASC -> shows.sortedBy { it.id }
            CollectionSortOrder.ALPHABETICAL_ASC -> shows.sortedBy { it.name }
            CollectionSortOrder.ALPHABETICAL_DESC -> shows.sortedByDescending { it.name }
            CollectionSortOrder.RELEASE_DATE_DESC -> shows.sortedByDescending { it.firstAirDate }
            CollectionSortOrder.RATING_DESC -> shows.sortedByDescending { it.voteAverage }
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
        loadDiscoverShows()
    }

    private fun fetchGenres() {
        viewModelScope.launch {
            try {
                val response = api.getTvGenres()
                if (response.isSuccessful) {
                    _genres.value = response.body()?.genres?.map { Genre(it.id, it.name) } ?: emptyList()
                }
            } catch (e: Exception) {}
        }
    }

    fun updateFilter(newState: MediaFilterState) {
        _filterState.value = newState
        loadDiscoverShows(loadMore = false)
    }

    fun updateSortOrder(newOrder: CollectionSortOrder) {
        _collectionSortState.value = newOrder
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun selectSubTab(index: Int) {
        _selectedSubTab.value = index
    }

    val allTrackedShows: StateFlow<List<Show>> = combine(
        showRepository.getAllTrackedShows(),
        collectionSortState
    ) { shows, sortOrder -> sortShows(shows, sortOrder) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var currentPage = 1
    private var isLastPage = false

    fun loadDiscoverShows(loadMore: Boolean = false) {
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
                filter.releaseDateGte?.let { params["first_air_date.gte"] = it }

                val isFiltering = filter != MediaFilterState()

                val response = if (isFiltering) {
                    api.discoverTvShows(params, page = currentPage)
                } else {
                    api.getPopularTvShows(page = currentPage)
                }
                if (response.isSuccessful) {
                    val pagedResponse = response.body()
                    val newItems = pagedResponse?.results?.map { dto ->
                        MediaItem(
                            id = dto.id,
                            tmdbId = dto.id,
                            title = dto.name ?: dto.originalName ?: "",
                            posterPath = dto.posterPath,
                            backdropPath = dto.backdropPath,
                            year = (dto.firstAirDate ?: "").take(4),
                            rating = dto.voteAverage,
                            genreIds = dto.genreIds ?: emptyList(),
                            isMovie = false
                        )
                    } ?: emptyList()

                    if (newItems.isEmpty()) {
                        isLastPage = true
                    } else {
                        if (loadMore) {
                            _discoverShows.value = _discoverShows.value + newItems
                        } else {
                            _discoverShows.value = newItems
                        }
                        currentPage++
                    }
                }
            } catch (e: Exception) {
            }
            _isLoading.value = false
        }
    }

    fun updateShowListType(showId: Long, listType: UserListType?) {
        viewModelScope.launch {
            showRepository.updateShowListType(showId, listType)
        }
    }

    fun toggleFavorite(showId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            showRepository.updateShowFavorite(showId, !isFavorite)
        }
    }

    fun removeShow(showId: Long) {
        viewModelScope.launch {
            showRepository.removeShow(showId)
        }
    }

    fun toggleNotify(showId: Long, enabled: Boolean) {
        viewModelScope.launch {
            showRepository.toggleNotify(showId, enabled)
        }
    }

    fun getFilteredWatchingShows(shows: List<Show>, subTabIndex: Int): List<Show> {
        return when (subTabIndex) {
            1 -> shows.filter { it.airStatus == AirStatus.RETURNING }
            2 -> shows.filter { it.airStatus == AirStatus.HIATUS }
            3 -> shows.filter { it.airStatus == AirStatus.ENDED || it.airStatus == AirStatus.CANCELED }
            4 -> shows.filter { it.airStatus == AirStatus.UPCOMING || it.airStatus == AirStatus.IN_PRODUCTION }
            else -> shows
        }
    }
}
