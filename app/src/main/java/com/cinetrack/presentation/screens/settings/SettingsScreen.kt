package com.cinetrack.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cinetrack.R
import com.cinetrack.domain.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Appearance
            item { SettingsSectionTitle(stringResource(R.string.appearance)) }

            item {
                SettingsItem(
                    title = stringResource(R.string.theme),
                    subtitle = settings.theme.name.lowercase().replaceFirstChar { it.uppercase() },
                    icon = Icons.Default.Palette,
                    onClick = { showThemeDialog = true }
                )
            }

            item {
                SettingsItem(
                    title = stringResource(R.string.accent_color),
                    subtitle = settings.accentColor,
                    icon = Icons.Default.Colorize,
                    trailing = {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(settings.accentColor)))
                        )
                    },
                    onClick = { showAccentDialog = true }
                )
            }

            item {
                SettingsItem(
                    title = stringResource(R.string.grid_columns),
                    subtitle = "${settings.gridColumns} columns",
                    icon = Icons.Default.GridView,
                    onClick = { }
                )
            }

            item {
                SettingsItem(
                    title = stringResource(R.string.card_style),
                    subtitle = settings.cardStyle.name.lowercase().replaceFirstChar { it.uppercase() },
                    icon = Icons.Default.ViewAgenda,
                    onClick = { }
                )
            }

            item {
                SettingsItem(
                    title = stringResource(R.string.rating_scale),
                    subtitle = when (settings.ratingScale) {
                        RatingScale.STARS_5 -> stringResource(R.string.rating_scale_stars)
                        RatingScale.NUMERIC_10 -> stringResource(R.string.rating_scale_numeric)
                    },
                    icon = Icons.Default.Star,
                    onClick = { }
                )
            }

            // Content
            item { SettingsSectionTitle(stringResource(R.string.content)) }

            item {
                SettingsItem(
                    title = stringResource(R.string.adult_content),
                    subtitle = if (settings.adultContent) "Enabled" else "Disabled",
                    icon = Icons.Default.Block,
                    trailing = {
                        Switch(
                            checked = settings.adultContent,
                            onCheckedChange = { }
                        )
                    },
                    onClick = { }
                )
            }

            // Tracking
            item { SettingsSectionTitle(stringResource(R.string.tracking)) }

            item {
                SettingsItem(
                    title = stringResource(R.string.auto_mark_watched),
                    subtitle = "Automatically mark shows as watched",
                    icon = Icons.Default.CheckCircle,
                    trailing = {
                        Switch(
                            checked = settings.autoMarkWatched,
                            onCheckedChange = { }
                        )
                    },
                    onClick = { }
                )
            }

            // Notifications
            item { SettingsSectionTitle(stringResource(R.string.notifications)) }

            item {
                SettingsItem(
                    title = stringResource(R.string.notification_settings),
                    subtitle = stringResource(R.string.notification_settings),
                    icon = Icons.Default.Notifications,
                    onClick = { }
                )
            }

            item {
                SettingsItem(
                    title = stringResource(R.string.quiet_hours),
                    subtitle = "${settings.quietHoursStart}:00 - ${settings.quietHoursEnd}:00",
                    icon = Icons.Default.NightsStay,
                    onClick = { }
                )
            }

            // Data & Storage
            item { SettingsSectionTitle(stringResource(R.string.data_storage)) }

            item {
                SettingsItem(
                    title = stringResource(R.string.clear_cache),
                    subtitle = "Clear downloaded images",
                    icon = Icons.Default.Cached,
                    onClick = { viewModel.clearImageCache() }
                )
            }

            item {
                SettingsItem(
                    title = stringResource(R.string.export_json),
                    subtitle = "Export all data as JSON file",
                    icon = Icons.Default.FileDownload,
                    onClick = { }
                )
            }

            item {
                SettingsItem(
                    title = stringResource(R.string.export_csv),
                    subtitle = "Export watch history as CSV",
                    icon = Icons.Default.TableChart,
                    onClick = { }
                )
            }

            // About
            item { SettingsSectionTitle(stringResource(R.string.about)) }

            item {
                SettingsItem(
                    title = stringResource(R.string.app_version),
                    subtitle = "1.0.0",
                    icon = Icons.Default.Info,
                    onClick = { }
                )
            }

            item {
                SettingsItem(
                    title = stringResource(R.string.privacy_policy),
                    subtitle = "",
                    icon = Icons.Default.Policy,
                    onClick = { }
                )
            }

            item {
                SettingsItem(
                    title = stringResource(R.string.rate_app),
                    subtitle = "",
                    icon = Icons.Default.ThumbUp,
                    onClick = { }
                )
            }
        }
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.theme)) },
            text = {
                Column {
                    ThemeMode.values().forEach { theme ->
                        ListItem(
                            headlineContent = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.clickable {
                                viewModel.updateTheme(theme)
                                showThemeDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Accent Color Dialog
    if (showAccentDialog) {
        AlertDialog(
            onDismissRequest = { showAccentDialog = false },
            title = { Text(stringResource(R.string.accent_color)) },
            text = {
                val colors = listOf(
                    "#01B4E4", "#90CEA1", "#B983FF", "#FF9F43",
                    "#FF6B9D", "#FF6B6B", "#4DABF7", "#A9E34B"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .clickable {
                                    viewModel.updateAccentColor(color)
                                    showAccentDialog = false
                                }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAccentDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SettingsSectionTitle(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = if (subtitle.isNotEmpty()) {
            { Text(subtitle) }
        } else null,
        leadingContent = {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingContent = trailing,
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun stringResource(id: Int): String {
    return androidx.compose.ui.res.stringResource(id)
}
