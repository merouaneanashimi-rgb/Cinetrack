package com.cinetrack.presentation.screens.movies

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cinetrack.BuildConfig
import com.cinetrack.R
import com.cinetrack.domain.model.Movie
import com.cinetrack.domain.model.UserListType
import com.cinetrack.presentation.components.PullToRefreshBox
import com.cinetrack.presentation.screens.discover.MediaCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    onMovieClick: (Long) -> Unit,
    viewModel: MoviesViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val discoverMovies by viewModel.discoverMovies.collectAsState()
    val watchlistMovies by viewModel.watchlistMovies.collectAsState()
    val watchedMovies by viewModel.watchedMovies.collectAsState()
    val favoriteMovies by viewModel.favoriteMovies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val tabs = listOf(
        R.string.movies_discover,
        R.string.movies_watchlist,
        R.string.movies_watched,
        R.string.movies_favorites,
        R.string.movies_custom_lists
    )

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.nav_movies)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 16.dp
                ) {
                    tabs.forEachIndexed { index, titleRes ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { viewModel.selectTab(index) },
                            text = { Text(stringResource(titleRes)) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Crossfade(targetState = selectedTab, label = "tab_switch") { tab ->
            when (tab) {
                0 -> MovieGrid(
                    items = discoverMovies.map { it.toDomainMovie() },
                    onMovieClick = onMovieClick,
                    isLoading = isLoading,
                    onRefresh = { viewModel.loadDiscoverMovies() },
                    modifier = Modifier.padding(padding)
                )
                1 -> MovieGrid(
                    items = watchlistMovies,
                    onMovieClick = onMovieClick,
                    isLoading = false,
                    onRefresh = {},
                    modifier = Modifier.padding(padding)
                )
                2 -> MovieGrid(
                    items = watchedMovies,
                    onMovieClick = onMovieClick,
                    isLoading = false,
                    onRefresh = {},
                    modifier = Modifier.padding(padding)
                )
                3 -> MovieGrid(
                    items = favoriteMovies,
                    onMovieClick = onMovieClick,
                    isLoading = false,
                    onRefresh = {},
                    modifier = Modifier.padding(padding)
                )
                else -> EmptyState(message = stringResource(R.string.empty_state_title))
            }
        }
    }
}

@Composable
fun MovieGrid(
    items: List<Movie>,
    onMovieClick: (Long) -> Unit,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = modifier
    ) {
        if (items.isEmpty() && !isLoading) {
            EmptyState(message = "No movies found")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items, key = { it.id }) { movie ->
                    MovieGridCard(
                        movie = movie,
                        onClick = { onMovieClick(movie.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun MovieGridCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${BuildConfig.TMDB_IMAGE_BASE_URL}w342${movie.posterPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(12.dp))
            )
            if (movie.voteAverage > 0) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format("%.1f", movie.voteAverage),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = movie.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun MediaItem.toDomainMovie() = Movie(
    id = id.toLong(),
    tmdbId = tmdbId,
    title = title,
    posterPath = posterPath,
    voteAverage = rating
)

@Composable
private fun stringResource(id: Int): String {
    return androidx.compose.ui.res.stringResource(id)
}
