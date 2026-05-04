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

    private val tmdbId: Int = savedStateHandle.get<String>("showId")?.toIntOrNull() ?: 0

    private val _localId = MutableStateFlow<Long?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val show: StateFlow<Show?> = _localId.filterNotNull().flatMapLatest { id ->
        showRepository.getShowById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val seasons: StateFlow<List<Season>> = _localId.filterNotNull().flatMapLatest { id ->
        showRepository.getSeasonsByShow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedSeason = MutableStateFlow<Int?>(null)
    val selectedSeason: StateFlow<Int?> = _selectedSeason.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            val localShow = showRepository.getShowByTmdbId(tmdbId)
            if (localShow != null) {
                _localId.value = localShow.id
            } else {
                val result = showRepository.syncShowFromApi(tmdbId)
                if (result.isSuccess) {
                    _localId.value = result.getOrNull()?.id
                }
            }
            _isLoading.value = false
        }
    }

    fun selectSeason(seasonNumber: Int) {
        _selectedSeason.value = if (_selectedSeason.value == seasonNumber) null else seasonNumber
    }

    fun updateListType(listType: UserListType?) {
        viewModelScope.launch {
            val currentId = _localId.value ?: return@launch
            showRepository.updateShowListType(currentId, listType)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentId = _localId.value ?: return@launch
            val current = show.value ?: return@launch
            showRepository.updateShowFavorite(currentId, !current.isFavorite)
        }
    }

    fun rateShow(rating: Double) {
        viewModelScope.launch {
            val currentId = _localId.value ?: return@launch
            showRepository.updateShowRating(currentId, rating)
        }
    }

    fun updateNotes(notes: String) {
        viewModelScope.launch {
            val currentId = _localId.value ?: return@launch
            showRepository.updateShowNotes(currentId, notes)
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
            val currentId = _localId.value ?: return@launch
            showRepository.markUpToEpisodeWatched(currentId, seasonNumber, episodeNumber)
        }
    }

    fun markFromEpisodeWatched(seasonNumber: Int, episodeNumber: Int) {
        viewModelScope.launch {
            val currentId = _localId.value ?: return@launch
            showRepository.markFromEpisodeWatched(currentId, seasonNumber, episodeNumber)
        }
    }

    fun markAllShowEpisodesWatched() {
        viewModelScope.launch {
            val currentId = _localId.value ?: return@launch
            showRepository.markAllShowEpisodesWatched(currentId)
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
            val currentId = _localId.value ?: return@launch
            val current = show.value ?: return@launch
            showRepository.toggleNotify(currentId, !current.notifyEnabled)
        }
    }

    fun getEpisodesBySeason(seasonId: Long) = showRepository.getEpisodesBySeason(seasonId)
}
