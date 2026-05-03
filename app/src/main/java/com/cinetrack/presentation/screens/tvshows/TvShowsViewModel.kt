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

    val watchingShows: StateFlow<List<Show>> = showRepository.getShowsByListType(UserListType.WATCHING)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlistShows: StateFlow<List<Show>> = showRepository.getShowsByListType(UserListType.WATCHLIST)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchedShows: StateFlow<List<Show>> = showRepository.getShowsByListType(UserListType.WATCHED)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val droppedShows: StateFlow<List<Show>> = showRepository.getShowsByListType(UserListType.DROPPED)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val planToWatchShows: StateFlow<List<Show>> = showRepository.getShowsByListType(UserListType.PLAN_TO_WATCH)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteShows: StateFlow<List<Show>> = showRepository.getFavoriteShows()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadDiscoverShows()
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun selectSubTab(index: Int) {
        _selectedSubTab.value = index
    }

    fun loadDiscoverShows() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getPopularTvShows()
                val shows = response.body()?.results?.map { dto ->
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
                _discoverShows.value = shows
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

    fun getFilteredWatchingShows(subTabIndex: Int): List<Show> {
        val allShows = watchingShows.value
        return when (subTabIndex) {
            1 -> allShows.filter { it.airStatus == AirStatus.RETURNING }
            2 -> allShows.filter { it.airStatus == AirStatus.HIATUS }
            3 -> allShows.filter { it.airStatus == AirStatus.ENDED || it.airStatus == AirStatus.CANCELED }
            4 -> allShows.filter { it.airStatus == AirStatus.UPCOMING || it.airStatus == AirStatus.IN_PRODUCTION }
            else -> allShows
        }
    }
}
