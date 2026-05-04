package com.cinetrack.presentation.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cinetrack.BuildConfig
import com.cinetrack.R
import com.cinetrack.domain.model.*
import com.cinetrack.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    savedStateHandle: androidx.lifecycle.SavedStateHandle,
    val movieRepository: MovieRepository
) : androidx.lifecycle.ViewModel() {
    private val tmdbId: Int = savedStateHandle.get<String>("movieId")?.toIntOrNull() ?: 0

    private val _localId = MutableStateFlow<Long?>(null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val movie: StateFlow<Movie?> = _localId.filterNotNull().flatMapLatest { id ->
        movieRepository.getMovieById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            val localMovie = movieRepository.getMovieByTmdbId(tmdbId)
            if (localMovie != null) {
                _localId.value = localMovie.id
            } else {
                val result = movieRepository.syncMovieFromApi(tmdbId)
                if (result.isSuccess) {
                    _localId.value = result.getOrNull()?.id
                }
            }
        }
    }

    fun updateListType(listType: UserListType?) {
        viewModelScope.launch { 
            val currentId = _localId.value ?: return@launch
            movieRepository.updateMovieListType(currentId, listType) 
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val currentId = _localId.value ?: return@launch
            val current = movie.value ?: return@launch
            movieRepository.updateMovieFavorite(currentId, !current.isFavorite)
        }
    }

    fun toggleWatched() {
        viewModelScope.launch {
            val currentId = _localId.value ?: return@launch
            val current = movie.value ?: return@launch
            movieRepository.updateMovieWatched(currentId, !current.isWatched)
        }
    }

    fun rateMovie(rating: Double) {
        viewModelScope.launch { 
            val currentId = _localId.value ?: return@launch
            movieRepository.updateMovieRating(currentId, rating) 
        }
    }

    fun updateNotes(notes: String) {
        viewModelScope.launch { 
            val currentId = _localId.value ?: return@launch
            movieRepository.updateMovieNotes(currentId, notes) 
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Long,
    onBackClick: () -> Unit,
    onPersonClick: (Long) -> Unit,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    val movie by viewModel.movie.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(movie?.title ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                )
            )
        }
    ) { padding ->
        movie?.let { m ->
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    MovieHeroSection(movie = m)
                }
                item {
                    MovieQuickActions(
                        movie = m,
                        onListTypeChange = { viewModel.updateListType(it) },
                        onToggleWatched = { viewModel.toggleWatched() },
                        onToggleFavorite = { viewModel.toggleFavorite() },
                        onRate = { viewModel.rateMovie(it) },
                        modifier = Modifier.padding(16.dp)
                    )
                }
                item {
                    OverviewSection(overview = m.overview)
                }
                if (m.genres.isNotEmpty()) {
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(m.genres) { genre ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = genre,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun MovieHeroSection(movie: Movie, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(300.dp)) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("${BuildConfig.TMDB_IMAGE_BASE_URL}w780${movie.backdropPath ?: movie.posterPath}")
                .crossfade(true)
                .build(),
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background), startY = 100f))
        )
        Row(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${BuildConfig.TMDB_IMAGE_BASE_URL}w342${movie.posterPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(120.dp).height(180.dp).clip(RoundedCornerShape(16.dp))
            )
            Column {
                Text(text = movie.title, style = MaterialTheme.typography.headlineSmall)
                movie.releaseDate?.let {
                    Text(text = it.take(4), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                movie.runtime?.let {
                    Text(text = "${it}min", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (movie.voteAverage > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                        Text(" ${String.format("%.1f", movie.voteAverage)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun MovieQuickActions(
    movie: Movie,
    onListTypeChange: (UserListType?) -> Unit,
    onToggleWatched: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRate: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showListMenu by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Box {
            ActionButton(icon = Icons.Default.PlaylistAdd, label = movie.userListType?.name ?: "Watchlist", onClick = { showListMenu = true })
            DropdownMenu(expanded = showListMenu, onDismissRequest = { showListMenu = false }) {
                UserListType.values().forEach { listType ->
                    DropdownMenuItem(
                        text = { Text(listType.name.replace("_", " ")) },
                        onClick = { onListTypeChange(listType); showListMenu = false },
                        leadingIcon = if (movie.userListType == listType) { { Icon(Icons.Default.Check, null) } } else null
                    )
                }
                DropdownMenuItem(text = { Text("Remove") }, onClick = { onListTypeChange(null); showListMenu = false })
            }
        }
        ActionButton(icon = if (movie.isWatched) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
            label = if (movie.isWatched) "Watched" else "Mark Watched", onClick = onToggleWatched)
        ActionButton(icon = if (movie.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            label = "Favorite", onClick = onToggleFavorite)
        ActionButton(icon = Icons.Default.Star, label = movie.userRating?.let { "${it}" } ?: "Rate", onClick = { showRateDialog = true })
    }

    if (showRateDialog) {
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text("Rate Movie") },
            text = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    (1..10).forEach { star ->
                        IconButton(onClick = { onRate(star.toDouble()); showRateDialog = false }) {
                            Icon(Icons.Default.Star, null, tint = if (star <= (movie.userRating ?: 0.0).toInt()) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showRateDialog = false }) { Text("Cancel") } }
        )
    }
}
