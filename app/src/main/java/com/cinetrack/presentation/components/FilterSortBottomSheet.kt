package com.cinetrack.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cinetrack.R
import com.cinetrack.domain.model.Genre
import com.cinetrack.domain.model.MediaFilterState
import com.cinetrack.domain.model.CollectionSortOrder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSortBottomSheet(
    onDismiss: () -> Unit,
    filterState: MediaFilterState,
    onFilterChange: (MediaFilterState) -> Unit,
    availableGenres: List<Genre>,
    isCollection: Boolean = false,
    collectionSortOrder: CollectionSortOrder = CollectionSortOrder.ADDED_DESC,
    onSortChange: (CollectionSortOrder) -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(if (isCollection) R.string.sort else R.string.filter),
                    style = MaterialTheme.typography.headlineSmall
                )
                TextButton(onClick = {
                    if (isCollection) onSortChange(CollectionSortOrder.ADDED_DESC)
                    else onFilterChange(MediaFilterState())
                }) {
                    Text(stringResource(R.string.reset_all))
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (isCollection) {
                    item {
                        Text(stringResource(R.string.sort), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CollectionSortOrder.values().forEach { order ->
                                FilterChip(
                                    selected = collectionSortOrder == order,
                                    onClick = { onSortChange(order) },
                                    label = { Text(getSortLabel(order)) }
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Text(stringResource(R.string.sort), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val sortOptions = listOf(
                                "popularity.desc" to "Popularity",
                                "vote_average.desc" to "Rating",
                                "primary_release_date.desc" to "Release Date"
                            )
                            sortOptions.forEach { (value, label) ->
                                FilterChip(
                                    selected = filterState.sortBy == value,
                                    onClick = { onFilterChange(filterState.copy(sortBy = value)) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }

                    item {
                        Text(stringResource(R.string.filter_genres), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            availableGenres.forEach { genre ->
                                val selected = filterState.genres.contains(genre.id)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        val newGenres = if (selected) {
                                            filterState.genres - genre.id
                                        } else {
                                            filterState.genres + genre.id
                                        }
                                        onFilterChange(filterState.copy(genres = newGenres))
                                    },
                                    label = { Text(genre.name) }
                                )
                            }
                        }
                    }

                    item {
                        Text(stringResource(R.string.filter_rating), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = (filterState.voteAverageGte ?: 0.0).toFloat(),
                            onValueChange = { onFilterChange(filterState.copy(voteAverageGte = it.toDouble())) },
                            valueRange = 0f..10f,
                            steps = 9
                        )
                        Text(
                            text = "Min Rating: ${String.format("%.1f", filterState.voteAverageGte ?: 0.0)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    item {
                        Text(stringResource(R.string.filter_runtime), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = (filterState.runtimeGte ?: 0).toFloat(),
                            onValueChange = { onFilterChange(filterState.copy(runtimeGte = it.toInt())) },
                            valueRange = 0f..240f,
                            steps = 15
                        )
                        Text(
                            text = "Min Runtime: ${filterState.runtimeGte ?: 0} min",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                item {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.apply))
                    }
                }
            }
        }
    }
}

@Composable
fun getSortLabel(order: CollectionSortOrder): String {
    return when (order) {
        CollectionSortOrder.ADDED_DESC -> "Recently Added"
        CollectionSortOrder.ADDED_ASC -> "Oldest Added"
        CollectionSortOrder.ALPHABETICAL_ASC -> "A-Z"
        CollectionSortOrder.ALPHABETICAL_DESC -> "Z-A"
        CollectionSortOrder.RELEASE_DATE_DESC -> "Release Date"
        CollectionSortOrder.RATING_DESC -> "Rating"
    }
}
