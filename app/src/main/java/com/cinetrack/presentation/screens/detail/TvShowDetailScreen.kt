package com.cinetrack.presentation.screens.detail

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.graphicsLayer
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvShowDetailScreen(
    showId: Long,
    onBackClick: () -> Unit,
    onPersonClick: (Long) -> Unit,
    viewModel: TvShowDetailViewModel = hiltViewModel()
) {
    val show by viewModel.show.collectAsState()
    val seasons by viewModel.seasons.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedSeason by viewModel.selectedSeason.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = show?.title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    show?.let { s ->
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (s.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (s.isFavorite) Color.Red else LocalContentColor.current
                            )
                        }
                        IconButton(onClick = { viewModel.toggleNotify() }) {
                            Icon(
                                imageVector = if (s.notifyEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                contentDescription = "Notify"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            show?.let { showData ->
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Hero backdrop
                    item {
                        HeroBackdropSection(show = showData)
                    }

                    // Quick actions
                    item {
                        QuickActionsRow(
                            show = showData,
                            onListTypeChange = { viewModel.updateListType(it) },
                            onRate = { viewModel.rateShow(it) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }

                    // Air Status Card
                    item {
                        AirStatusCard(show = showData, modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    // Overview
                    item {
                        OverviewSection(overview = showData.overview)
                    }

                    // Progress
                    if (showData.numberOfEpisodes > 0) {
                        item {
                            ProgressSection(show = showData)
                        }
                    }

                    // Seasons Accordion
                    if (seasons.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.seasons),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }
                        items(seasons, key = { it.id }) { season ->
                            SeasonAccordionItem(
                                season = season,
                                showId = showId,
                                isExpanded = selectedSeason == season.seasonNumber,
                                onToggle = { viewModel.selectSeason(season.seasonNumber) },
                                onMarkSeasonWatched = { viewModel.markSeasonWatched(season.id, it) },
                                onEpisodeWatched = { epId, watched ->
                                    viewModel.toggleEpisodeWatched(epId, watched)
                                },
                                onRateEpisode = { epId, rating ->
                                    viewModel.rateEpisode(epId, rating)
                                }
                            )
                        }
                    }
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Show not found")
                }
            }
        }
    }
}

@Composable
fun HeroBackdropSection(show: Show, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().height(280.dp)) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("${BuildConfig.TMDB_IMAGE_BASE_URL}w780${show.backdropPath ?: show.posterPath}")
                .crossfade(true)
                .build(),
            contentDescription = show.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background),
                        startY = 100f
                    )
                )
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${BuildConfig.TMDB_IMAGE_BASE_URL}w342${show.posterPath}")
                    .crossfade(true)
                    .build(),
                contentDescription = show.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(100.dp)
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Column {
                Text(
                    text = show.title,
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    show.firstAirDate?.let {
                        Text(
                            text = it.take(4),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${show.numberOfSeasons} seasons",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (show.voteAverage > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = String.format("%.1f", show.voteAverage),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                // Genre chips
                if (show.genres.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(show.genres.take(4)) { genre ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = genre,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    show: Show,
    onListTypeChange: (UserListType?) -> Unit,
    onRate: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showListMenu by remember { mutableStateOf(false) }
    var showRateDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // List selector
        Box {
            ActionButton(
                icon = Icons.Default.PlaylistAdd,
                label = show.userListType?.name ?: "Add to List",
                onClick = { showListMenu = true }
            )
            DropdownMenu(
                expanded = showListMenu,
                onDismissRequest = { showListMenu = false }
            ) {
                UserListType.values().forEach { listType ->
                    DropdownMenuItem(
                        text = { Text(listType.name.replace("_", " ")) },
                        onClick = {
                            onListTypeChange(listType)
                            showListMenu = false
                        },
                        leadingIcon = if (show.userListType == listType) {
                            { Icon(Icons.Default.Check, null) }
                        } else null
                    )
                }
                DropdownMenuItem(
                    text = { Text("Remove from lists") },
                    onClick = {
                        onListTypeChange(null)
                        showListMenu = false
                    }
                )
            }
        }

        ActionButton(
            icon = Icons.Default.Star,
            label = if (show.userRating != null) "${show.userRating}" else "Rate",
            onClick = { showRateDialog = true }
        )

        ActionButton(
            icon = Icons.Default.Share,
            label = "Share",
            onClick = { }
        )
    }

    // Rating dialog
    if (showRateDialog) {
        AlertDialog(
            onDismissRequest = { showRateDialog = false },
            title = { Text("Rate Show") },
            text = {
                Column {
                    Text("Select your rating:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        (1..10).forEach { star ->
                            IconButton(onClick = {
                                onRate(star.toDouble())
                                showRateDialog = false
                            }) {
                                Icon(
                                    imageVector = if (star <= (show.userRating ?: 0.0).toInt())
                                        Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star stars",
                                    tint = Color(0xFFFFD700)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = CircleShape,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = label)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun AirStatusCard(show: Show, modifier: Modifier = Modifier) {
    val statusColor = Color(show.airStatus.toColor())

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(statusColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = show.airStatus.toDisplayName(),
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            show.nextEpisodeAirDate?.let { date ->
                Text(
                    text = "Next: S${show.nextEpisodeSeason}E${show.nextEpisodeNumber}${show.nextEpisodeTitle?.let { " · $it" } ?: ""} · $date",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            show.lastAirDate?.let { date ->
                Text(
                    text = "Last aired: $date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun OverviewSection(overview: String?, modifier: Modifier = Modifier) {
    if (!overview.isNullOrEmpty()) {
        var expanded by remember { mutableStateOf(false) }
        Column(modifier = modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.overview),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = overview,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { expanded = !expanded }
            )
            if (!expanded) {
                Text(
                    text = "Show more",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { expanded = true }
                )
            }
        }
    }
}

@Composable
fun ProgressSection(show: Show, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Overall Progress",
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { show.watchProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(show.watchProgress * 100).toInt()}% complete · ${show.totalWatchTimeMinutes / 60}h watched",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonAccordionItem(
    season: Season,
    showId: Long,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onMarkSeasonWatched: (Boolean) -> Unit,
    onEpisodeWatched: (Long, Boolean) -> Unit,
    onRateEpisode: (Long, Double?) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onToggle
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = season.name,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "${season.episodeCount} episodes${season.airDate?.let { " · ${it.take(4)}" } ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { onMarkSeasonWatched(true) }) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50))
                    }
                    IconButton(onClick = onToggle) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand"
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                SeasonEpisodesList(
                    showId = showId,
                    seasonId = season.id,
                    onEpisodeWatched = onEpisodeWatched,
                    onRateEpisode = onRateEpisode
                )
            }
        }
    }
}

@Composable
fun SeasonEpisodesList(
    showId: Long,
    seasonId: Long,
    onEpisodeWatched: (Long, Boolean) -> Unit,
    onRateEpisode: (Long, Double?) -> Unit,
    viewModel: TvShowDetailViewModel = hiltViewModel()
) {
    val episodes by viewModel.getEpisodesBySeason(seasonId)
        .collectAsState(initial = emptyList())

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        episodes.forEach { episode ->
            EpisodeRow(
                episode = episode,
                onWatchedToggle = { onEpisodeWatched(episode.id, !episode.isWatched) },
                onRate = { onRateEpisode(episode.id, it) }
            )
            if (episode.id != episodes.lastOrNull()?.id) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}

@Composable
fun EpisodeRow(
    episode: Episode,
    onWatchedToggle: () -> Unit,
    onRate: (Double?) -> Unit
) {
    val borderColor = when {
        episode.isWatched -> Color(0xFF4CAF50)
        episode.isAired -> MaterialTheme.colorScheme.outline
        else -> Color(0xFF2196F3)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Still image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("${BuildConfig.TMDB_IMAGE_BASE_URL}w300${episode.stillPath}")
                .crossfade(true)
                .build(),
            contentDescription = episode.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(80.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        // Episode info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "E${episode.episodeNumber} · ${episode.name}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                episode.airDate?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                episode.runtime?.let {
                    Text(
                        text = "${it}min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!episode.isAired) {
                Text(
                    text = "Airs ${episode.airDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2196F3)
                )
            }
        }

        // Watched checkbox
        IconButton(
            onClick = onWatchedToggle,
            enabled = episode.isAired
        ) {
            Icon(
                imageVector = if (episode.isWatched) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = "Watched",
                tint = if (episode.isWatched) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun stringResource(id: Int): String {
    return androidx.compose.ui.res.stringResource(id)
}
