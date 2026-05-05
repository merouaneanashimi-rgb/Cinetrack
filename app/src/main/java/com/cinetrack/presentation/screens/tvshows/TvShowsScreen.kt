package com.cinetrack.presentation.screens.tvshows

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.cinetrack.presentation.components.FilterSortBottomSheet
import com.cinetrack.presentation.components.PullToRefreshBox
import com.cinetrack.presentation.screens.discover.MediaCard
import com.cinetrack.presentation.screens.movies.EmptyState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun TvShowsScreen(
    onShowClick: (Long) -> Unit,
    viewModel: TvShowsViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedSubTab by viewModel.selectedSubTab.collectAsState()
    val discoverShows by viewModel.discoverShows.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val filterState by viewModel.filterState.collectAsState()
    val collectionSortOrder by viewModel.collectionSortState.collectAsState()
    val genres by viewModel.genres.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    val mainTabs = listOf(
        R.string.tv_discover,
        R.string.tv_watching,
        R.string.tv_watchlist,
        R.string.tv_watched,
        R.string.tv_dropped,
        R.string.tv_plan_to_watch,
        R.string.tv_favorites,
        R.string.shows_collection
    )

    val subTabs = listOf(
        R.string.subtab_all,
        R.string.subtab_airing,
        R.string.subtab_hiatus,
        R.string.subtab_ended,
        R.string.subtab_upcoming
    )

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.nav_tv_shows)) },
                    actions = {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.List,
                                contentDescription = stringResource(R.string.filter)
                            )
                        }
                    },
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
                    mainTabs.forEachIndexed { index, titleRes ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { viewModel.selectTab(index) },
                            text = { Text(stringResource(titleRes)) }
                        )
                    }
                }
                // Sub-tabs for Watching tab
                if (selectedTab == 1) {
                    SubTabRow(
                        tabs = subTabs,
                        selectedIndex = selectedSubTab,
                        onSelect = { viewModel.selectSubTab(it) }
                    )
                }
            }
        }
    ) { padding ->
        Crossfade(targetState = selectedTab, label = "tv_tab") { tab ->
            when (tab) {
                0 -> {
                    val listState = androidx.compose.foundation.lazy.grid.rememberLazyGridState()
                    
                    LaunchedEffect(listState, discoverShows.size) {
                        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                            .collect { lastIndex ->
                                if (lastIndex != null && lastIndex >= discoverShows.size - 6 && !isLoading) {
                                    viewModel.loadDiscoverShows(loadMore = true)
                                }
                            }
                    }

                    PullToRefreshBox(
                        isRefreshing = isLoading,
                        onRefresh = { viewModel.loadDiscoverShows() },
                        modifier = Modifier.padding(padding)
                    ) {
                        LazyVerticalGrid(
                            state = listState,
                            columns = GridCells.Fixed(3),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(discoverShows, key = { it.id }) { show ->
                                MediaCard(
                                    item = show,
                                    onClick = { onShowClick(show.tmdbId.toLong()) },
                                    onAddClick = { viewModel.addToWatchlist(show) }
                                )
                            }
                        }
                    }
                }
                1 -> WatchingContent(
                    subTab = selectedSubTab,
                    viewModel = viewModel,
                    onShowClick = onShowClick,
                    modifier = Modifier.padding(padding)
                )
                2 -> ShowListGrid(
                    showsFlow = viewModel.watchlistShows,
                    onShowClick = onShowClick,
                    onAddClick = { /* Already in watchlist */ },
                    modifier = Modifier.padding(padding)
                )
                3 -> ShowListGrid(
                    showsFlow = viewModel.watchedShows,
                    onShowClick = onShowClick,
                    onAddClick = { /* Already watched */ },
                    modifier = Modifier.padding(padding)
                )
                4 -> ShowListGrid(
                    showsFlow = viewModel.droppedShows,
                    onShowClick = onShowClick,
                    onAddClick = { /* Already in collection */ },
                    modifier = Modifier.padding(padding)
                )
                5 -> ShowListGrid(
                    showsFlow = viewModel.planToWatchShows,
                    onShowClick = onShowClick,
                    onAddClick = { /* Already in collection */ },
                    modifier = Modifier.padding(padding)
                )
                6 -> ShowListGrid(
                    showsFlow = viewModel.favoriteShows,
                    onShowClick = onShowClick,
                    onAddClick = { /* Already favorite */ },
                    modifier = Modifier.padding(padding)
                )
                7 -> CollectionContent(
                    viewModel = viewModel,
                    onShowClick = onShowClick,
                    modifier = Modifier.padding(padding)
                )
                else -> EmptyState(message = stringResource(R.string.empty_state_title))
            }
        }

        if (showFilterSheet) {
            FilterSortBottomSheet(
                onDismiss = { showFilterSheet = false },
                filterState = filterState,
                onFilterChange = { state -> viewModel.updateFilter(state) },
                availableGenres = genres,
                isCollection = selectedTab != 0,
                collectionSortOrder = collectionSortOrder,
                onSortChange = { order -> viewModel.updateSortOrder(order) }
            )
        }
    }
}

@Composable
fun SubTabRow(
    tabs: List<Int>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        tabs.forEachIndexed { index, titleRes ->
            val selected = selectedIndex == index
            FilterChip(
                selected = selected,
                onClick = { onSelect(index) },
                label = { Text(stringResource(titleRes)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
fun WatchingContent(
    subTab: Int,
    viewModel: TvShowsViewModel,
    onShowClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val watchingShows by viewModel.watchingShows.collectAsState()
    val shows = remember(watchingShows, subTab) {
        viewModel.getFilteredWatchingShows(watchingShows, subTab)
    }

    if (shows.isEmpty()) {
        EmptyState(
            message = "No shows in this category",
            modifier = modifier
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(shows, key = { it.id }) { show ->
                WatchingShowCard(
                    show = show,
                    onClick = { onShowClick(show.tmdbId.toLong()) },
                    onNotifyToggle = { viewModel.toggleNotify(show.id, !show.notifyEnabled) }
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CollectionContent(
    viewModel: TvShowsViewModel,
    onShowClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allShows by viewModel.allTrackedShows.collectAsState()
    
    if (allShows.isEmpty()) {
        EmptyState(message = "Your collection is empty", modifier = modifier)
    } else {
        val groupedShows = remember(allShows) {
            allShows.groupBy { it.airStatus }.toSortedMap(compareBy { it.ordinal })
        }
        
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier.fillMaxSize()
        ) {
            groupedShows.forEach { (status, shows) ->
                stickyHeader {
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = status.toDisplayName(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                items(shows, key = { it.id }) { show ->
                    WatchingShowCard(
                        show = show,
                        onClick = { onShowClick(show.tmdbId.toLong()) },
                        onNotifyToggle = { viewModel.toggleNotify(show.id, !show.notifyEnabled) }
                    )
                }
            }
        }
    }
}

@Composable
fun WatchingShowCard(
    show: Show,
    onClick: () -> Unit,
    onNotifyToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val airStatusColor = show.airStatus.toColor()
    val daysUntil = show.nextEpisodeAirDate?.let { calculateDaysUntil(it) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Poster
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("${BuildConfig.TMDB_IMAGE_BASE_URL}w185${show.posterPath}")
                        .crossfade(true)
                        .build(),
                    contentDescription = show.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(80.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                // Progress indicator
                if (show.numberOfEpisodes > 0) {
                    CircularProgressIndicator(
                        progress = { show.watchProgress },
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                        color = Color(0xFF4CAF50),
                        trackColor = Color.Black.copy(alpha = 0.5f),
                        strokeWidth = 2.dp
                    )
                }
            }

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = show.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Air status badge
                Surface(
                    color = Color(airStatusColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = show.airStatus.toDisplayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                // Next episode info
                show.nextEpisodeAirDate?.let { date ->
                    Text(
                        text = "S${show.nextEpisodeSeason}E${show.nextEpisodeNumber}${show.nextEpisodeTitle?.let { " · $it" } ?: ""} · ${formatDate(date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    daysUntil?.let { days ->
                        val countdownText = when {
                            days < 0 -> "Aired"
                            days == 0L -> "TODAY"
                            days == 1L -> "Tomorrow"
                            else -> "in $days days"
                        }
                        Text(
                            text = countdownText,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (days == 0L) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Episodes behind
                if (show.numberOfEpisodes > 0) {
                    val unwatched = show.numberOfEpisodes - show.watchedEpisodes
                    if (unwatched > 0) {
                        Surface(
                            color = Color(0xFFFF9800).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "$unwatched eps behind",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF9800),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Notify button
            IconButton(onClick = onNotifyToggle) {
                Icon(
                    imageVector = if (show.notifyEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                    contentDescription = "Notify",
                    tint = if (show.notifyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ShowListGrid(
    showsFlow: StateFlow<List<Show>>,
    onShowClick: (Long) -> Unit,
    onAddClick: (Show) -> Unit,
    modifier: Modifier = Modifier
) {
    val shows by showsFlow.collectAsState()

    if (shows.isEmpty()) {
        EmptyState(message = stringResource(R.string.empty_state_title), modifier = modifier)
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier.fillMaxSize()
        ) {
            items(shows, key = { it.id }) { show ->
                ShowGridCard(
                    show = show,
                    onClick = { onShowClick(show.tmdbId.toLong()) },
                    onAddClick = { onAddClick(show) }
                )
            }
        }
    }
}

@Composable
fun ShowGridCard(
    show: Show,
    onClick: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val airStatusColor = show.airStatus.toColor()

    Column(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${BuildConfig.TMDB_IMAGE_BASE_URL}w342${show.posterPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = show.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(12.dp))
            )
            // Air status badge
            Surface(
                color = Color(airStatusColor),
                shape = RoundedCornerShape(bottomEnd = 8.dp, topStart = 8.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = show.airStatus.toDisplayName(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Add button overlay
            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        androidx.compose.foundation.shape.CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add to collection",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = show.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun calculateDaysUntil(dateString: String): Long? {
    return try {
        val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
        val airDate = java.time.LocalDate.parse(dateString, formatter)
        java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), airDate)
    } catch (e: Exception) {
        null
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
        val date = java.time.LocalDate.parse(dateString, formatter)
        date.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy"))
    } catch (e: Exception) {
        dateString
    }
}

@Composable
private fun stringResource(id: Int): String {
    return androidx.compose.ui.res.stringResource(id)
}
