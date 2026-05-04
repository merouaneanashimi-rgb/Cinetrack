package com.cinetrack.presentation.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cinetrack.domain.model.*
import com.cinetrack.domain.repository.ShowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TvShowDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val showRepository: ShowRepository
) : ViewModel() {

    private val showId: Long = savedStateHandle.get<String>("showId")?.toLongOrNull() ?: 0L

    private val _show = MutableStateFlow<Show?>(null)
    val show: StateFlow<Show?> = _show.asStateFlow()

    val seasons: StateFlow<List<Season>> = showRepository.getSeasonsByShow(showId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val episodes: StateFlow<List<Episode>> = showRepository.getEpisodesBySeason(showId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedSeason = MutableStateFlow<Int?>(null)
    val selectedSeason: StateFlow<Int?> = _selectedSeason.asStateFlow()

    init {
        loadShow()
    }

    private fun loadShow() {
        viewModelScope.launch {
            _isLoading.value = true
            showRepository.getShowById(showId).collect { s ->
                _show.value = s
                _isLoading.value = false
                if (s == null) {
                    // Try to sync from API if not in DB
                }
            }
        }
    }

    fun selectSeason(seasonNumber: Int) {
        _selectedSeason.value = if (_selectedSeason.value == seasonNumber) null else seasonNumber
    }

    fun updateListType(listType: UserListType?) {
        viewModelScope.launch {
            showRepository.updateShowListType(showId, listType)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val current = _show.value ?: return@launch
            showRepository.updateShowFavorite(showId, !current.isFavorite)
        }
    }

    fun rateShow(rating: Double) {
        viewModelScope.launch {
            showRepository.updateShowRating(showId, rating)
        }
    }

    fun updateNotes(notes: String) {
        viewModelScope.launch {
            showRepository.updateShowNotes(showId, notes)
        }
    }

    fun toggleEpisodeWatched(episodeId: Long, watched: Boolean) {
        viewModelScope.launch {
            showRepository.updateEpisodeWatched(episodeId, watched)
        }
    }

    fun markSeasonWatched(seasonId: Long, watched: Boolean) {
        viewModelScope.launch {
            showRepository.markSeasonWatched(seasonId, watched)
        }
    }

    fun markUpToEpisodeWatched(seasonNumber: Int, episodeNumber: Int) {
        viewModelScope.launch {
            showRepository.markUpToEpisodeWatched(showId, seasonNumber, episodeNumber)
        }
    }

    fun markFromEpisodeWatched(seasonNumber: Int, episodeNumber: Int) {
        viewModelScope.launch {
            showRepository.markFromEpisodeWatched(showId, seasonNumber, episodeNumber)
        }
    }

    fun markAllShowEpisodesWatched() {
        viewModelScope.launch {
            showRepository.markAllShowEpisodesWatched(showId)
        }
    }

    fun rateEpisode(episodeId: Long, rating: Double?) {
        viewModelScope.launch {
            showRepository.updateEpisodeRating(episodeId, rating)
        }
    }

    fun updateEpisodeNotes(episodeId: Long, notes: String?) {
        viewModelScope.launch {
            showRepository.updateEpisodeNotes(episodeId, notes)
        }
    }

    fun toggleNotify() {
        viewModelScope.launch {
            val current = _show.value ?: return@launch
            showRepository.toggleNotify(showId, !current.notifyEnabled)
        }
    }
    fun getEpisodesBySeason(seasonId: Long) = showRepository.getEpisodesBySeason(seasonId)
}
