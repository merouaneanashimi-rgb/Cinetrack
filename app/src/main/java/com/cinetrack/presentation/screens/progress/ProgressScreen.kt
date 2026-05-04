package com.cinetrack.presentation.screens.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cinetrack.BuildConfig
import com.cinetrack.R
import com.cinetrack.domain.model.AirStatus
import com.cinetrack.domain.model.toColor
import com.cinetrack.domain.model.Episode
import com.cinetrack.domain.model.Show
import com.cinetrack.presentation.components.PullToRefreshBox
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onShowClick: (Long) -> Unit,
    onMovieClick: (Long) -> Unit,
    viewModel: ProgressViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val continueWatching by viewModel.continueWatching.collectAsState()
    val recentlyWatched by viewModel.recentlyWatched.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_progress)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { },
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item { OverviewCards(stats = stats) }

                if (continueWatching.isNotEmpty()) {
                    item {
                        SectionTitle(stringResource(R.string.continue_watching))
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(continueWatching) { show ->
                                ContinueWatchingProgressCard(
                                    show = show,
                                    onClick = { onShowClick(show.id) }
                                )
                            }
                        }
                    }
                }

                item {
                    SectionTitle(stringResource(R.string.statistics))
                    Spacer(Modifier.height(8.dp))
                    StatsCharts(stats = stats)
                }

                if (recentlyWatched.isNotEmpty()) {
                    item {
                        SectionTitle(stringResource(R.string.recently_watched))
                        Spacer(Modifier.height(8.dp))
                        RecentlyWatchedList(
                            episodes = recentlyWatched.take(20),
                            onShowClick = onShowClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OverviewCards(stats: StatsData, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(title = stringResource(R.string.total_shows), value = stats.totalShowsTracked.toString(), icon = Icons.Default.LiveTv, modifier = Modifier.weight(1f))
            StatCard(title = stringResource(R.string.total_episodes), value = stats.totalEpisodesWatched.toString(), icon = Icons.Default.PlaylistPlay, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(title = stringResource(R.string.total_watch_time), value = "${stats.totalWatchTimeHours}h", icon = Icons.Default.Schedule, modifier = Modifier.weight(1f))
            StatCard(title = stringResource(R.string.total_movies), value = stats.totalMoviesWatched.toString(), icon = Icons.Default.Movie, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ContinueWatchingProgressCard(show: Show, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.width(160.dp).clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data("${BuildConfig.TMDB_IMAGE_BASE_URL}w342${show.posterPath}").crossfade(true).build(),
                contentDescription = show.title, contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(200.dp)
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = show.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                show.nextEpisodeSeason?.let {
                    Text(text = "S${show.nextEpisodeSeason}E${show.nextEpisodeNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LinearProgressIndicator(
                    progress = { show.watchProgress },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatsCharts(stats: StatsData, modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Bar Chart - Weekly Activity
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Episodes per Week", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 12.dp))
                if (stats.weeklyActivity.isNotEmpty()) {
                    val maxValue = stats.weeklyActivity.maxOrNull()?.toFloat() ?: 1f
                    Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                        drawBarChart(stats.weeklyActivity, maxValue, primaryColor, surfaceVariant)
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("No activity yet", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }

        // Status Distribution
        if (stats.statusDistribution.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Show Status Distribution", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 12.dp))
                    val total = stats.statusDistribution.values.sum().toFloat()
                    stats.statusDistribution.forEach { (status, count) ->
                        val color = try { Color(AirStatus.valueOf(status).toColor()) } catch (e: Exception) { primaryColor }
                        val pct = if (total > 0) (count / total * 100).toInt() else 0
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(color, RoundedCornerShape(3.dp)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = status, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text(text = "$count ($pct%)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        LinearProgressIndicator(
                            progress = { if (total > 0) count / total else 0f },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).padding(bottom = 4.dp),
                            color = color, trackColor = surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawBarChart(data: List<Int>, maxValue: Float, barColor: Color, bgColor: Color) {
    if (data.isEmpty()) return
    val barWidth = size.width / (data.size * 1.5f)
    val spacing = barWidth * 0.5f
    data.forEachIndexed { index, value ->
        val barHeight = if (maxValue > 0) (value / maxValue) * size.height * 0.85f else 0f
        val x = index * (barWidth + spacing)
        val y = size.height - barHeight
        // Background bar
        drawRoundRect(color = bgColor, topLeft = Offset(x, 0f), size = Size(barWidth, size.height), cornerRadius = CornerRadius(4f))
        // Value bar
        if (barHeight > 0) {
            drawRoundRect(color = barColor, topLeft = Offset(x, y), size = Size(barWidth, barHeight), cornerRadius = CornerRadius(4f))
        }
    }
}

@Composable
fun RecentlyWatchedList(episodes: List<Episode>, onShowClick: (Long) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        episodes.forEach { episode ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onShowClick(episode.showId) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data("${BuildConfig.TMDB_IMAGE_BASE_URL}w185${episode.stillPath}").crossfade(true).build(),
                        contentDescription = episode.name, contentScale = ContentScale.Crop,
                        modifier = Modifier.width(80.dp).height(50.dp).clip(RoundedCornerShape(8.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "S${episode.seasonNumber}E${episode.episodeNumber} · ${episode.name}", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        episode.watchedAt?.let {
                            Text(text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(text = title, style = MaterialTheme.typography.titleMedium, modifier = modifier)
}
