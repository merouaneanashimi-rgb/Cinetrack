package com.cinetrack.presentation.screens.trakt

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

// ─── Trakt Brand Colors ───────────────────────────────────────────────────────
private val TraktRed    = Color(0xFFED1C24)
private val TraktDark   = Color(0xFF1A1A2E)
private val TraktSurface = Color(0xFF16213E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraktScreen(viewModel: TraktViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TraktLogo()
                        Spacer(Modifier.width(8.dp))
                        Text("Trakt.tv", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = uiState.isConnected,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "trakt_state"
            ) { connected ->
                if (connected) {
                    ConnectedContent(uiState = uiState, viewModel = viewModel, context = context)
                } else {
                    DisconnectedContent(
                        isLoading = uiState.isLoading,
                        onConnect = { viewModel.startAuth(context) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TraktLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(28.dp)
            .background(TraktRed, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "T",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.Black
        )
    }
}

// ─── Disconnected UI ──────────────────────────────────────────────────────────

@Composable
private fun DisconnectedContent(isLoading: Boolean, onConnect: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    Brush.radialGradient(listOf(TraktRed, TraktDark)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("T", style = MaterialTheme.typography.displayMedium, color = Color.White, fontWeight = FontWeight.Black)
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "Sync with Trakt.tv",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Connect your Trakt.tv account to sync your watched history, ratings, and watchlists across all your devices.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // Features list
        listOf(
            "↕️ Bidirectional sync — push & pull",
            "📺 Episode tracking synced automatically",
            "⭐ Ratings synced both ways",
            "📋 Watchlist kept in sync",
            "🔄 Auto-sync every 6 hours"
        ).forEach { feature ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(feature, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = onConnect,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TraktRed),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
            } else {
                Icon(Icons.Default.Login, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Connect with Trakt.tv", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "A browser window will open for secure login.\nYour credentials are never stored by CineTrack.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Connected UI ─────────────────────────────────────────────────────────────

@Composable
private fun ConnectedContent(
    uiState: TraktUiState,
    viewModel: TraktViewModel,
    context: Context
) {
    var showDisconnectDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar
                    if (uiState.avatarUrl != null) {
                        AsyncImage(
                            model = uiState.avatarUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(56.dp).clip(CircleShape)
                                .border(2.dp, TraktRed, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(56.dp)
                                .background(TraktRed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (uiState.username?.firstOrNull() ?: "T").uppercaseChar().toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.username ?: "Trakt User",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF4CAF50), CircleShape))
                            Spacer(Modifier.width(6.dp))
                            Text("Connected", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                        }
                    }

                    TraktLogo()
                }

                uiState.lastSyncTime?.let { time ->
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Sync, null, modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Last sync: ${formatSyncTime(time)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Sync Now Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Sync", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                AnimatedVisibility(visible = uiState.syncResult != null) {
                    uiState.syncResult?.let { result ->
                        SyncResultCard(result = result)
                        Spacer(Modifier.height(12.dp))
                    }
                }

                Button(
                    onClick = { viewModel.syncNow(context) },
                    enabled = !uiState.isSyncing,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TraktRed),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (uiState.isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Syncing...", color = Color.White)
                    } else {
                        Icon(Icons.Default.Sync, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Sync Now", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Sync Status Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SyncStatCard("Push", "Local → Trakt", Icons.Default.CloudUpload, TraktRed, Modifier.weight(1f))
            SyncStatCard("Pull", "Trakt → Local", Icons.Default.CloudDownload, Color(0xFF2196F3), Modifier.weight(1f))
        }

        // Auto-sync info
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Auto-sync", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(
                        "Runs automatically every 6 hours when connected to internet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Disconnect
        OutlinedButton(
            onClick = { showDisconnectDialog = true },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.LinkOff, null)
            Spacer(Modifier.width(8.dp))
            Text("Disconnect Trakt Account")
        }
    }

    if (showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { showDisconnectDialog = false },
            title = { Text("Disconnect Trakt.tv?") },
            text = { Text("Your local data will remain. Auto-sync will be disabled.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.disconnect(); showDisconnectDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Disconnect") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDisconnectDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SyncResultCard(result: com.cinetrack.data.sync.SyncResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (result.errors.isEmpty())
                Color(0xFF1B5E20).copy(alpha = 0.2f)
            else Color(0xFFB71C1C).copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (result.errors.isEmpty()) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (result.errors.isEmpty()) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (result.errors.isEmpty()) "Sync completed" else "Sync completed with errors",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            if (result.errors.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "↑ ${result.pushedMovies} movies · ${result.pushedEpisodes} episodes · ${result.pushedRatings} ratings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "↓ ${result.pulledMovies} movies · ${result.pulledEpisodes} episodes · ${result.pulledRatings} ratings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SyncStatCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatSyncTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}
