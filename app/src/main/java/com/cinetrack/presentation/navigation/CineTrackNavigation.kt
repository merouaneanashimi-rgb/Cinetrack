package com.cinetrack.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cinetrack.presentation.screens.trakt.TraktScreen
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cinetrack.presentation.screens.discover.DiscoverScreen
import com.cinetrack.presentation.screens.movies.MoviesScreen
import com.cinetrack.presentation.screens.tvshows.TvShowsScreen
import com.cinetrack.presentation.screens.progress.ProgressScreen
import com.cinetrack.presentation.screens.settings.SettingsScreen
import com.cinetrack.presentation.screens.detail.MovieDetailScreen
import com.cinetrack.presentation.screens.detail.TvShowDetailScreen
import com.cinetrack.presentation.screens.detail.PersonDetailScreen
import com.cinetrack.presentation.screens.search.SearchScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CineTrackNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = BottomNavItem.items.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    BottomNavItem.items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.labelResId)
                                )
                            },
                            label = { Text(stringResource(item.labelResId)) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Discover.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Discover.route) {
                DiscoverScreen(
                    onMovieClick = { movieId ->
                        navController.navigate(Screen.MovieDetail.createRoute(movieId))
                    },
                    onShowClick = { showId ->
                        navController.navigate(Screen.TvShowDetail.createRoute(showId))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    }
                )
            }
            composable(Screen.Movies.route) {
                MoviesScreen(
                    onMovieClick = { movieId ->
                        navController.navigate(Screen.MovieDetail.createRoute(movieId))
                    }
                )
            }
            composable(Screen.TvShows.route) {
                TvShowsScreen(
                    onShowClick = { showId ->
                        navController.navigate(Screen.TvShowDetail.createRoute(showId))
                    }
                )
            }
            composable(Screen.Progress.route) {
                ProgressScreen(
                    onShowClick = { showId ->
                        navController.navigate(Screen.TvShowDetail.createRoute(showId))
                    },
                    onMovieClick = { movieId ->
                        navController.navigate(Screen.MovieDetail.createRoute(movieId))
                    }
                )
            }
            
        composable(Screen.Trakt.route) {
            TraktScreen()
        }
        composable(Screen.Settings.route) {
                SettingsScreen()
            }
            composable(Screen.MovieDetail.route) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getString("movieId")?.toLongOrNull() ?: 0L
                MovieDetailScreen(
                    movieId = movieId,
                    onBackClick = { navController.popBackStack() },
                    onPersonClick = { personId ->
                        navController.navigate(Screen.PersonDetail.createRoute(personId))
                    }
                )
            }
            composable(Screen.TvShowDetail.route) { backStackEntry ->
                val showId = backStackEntry.arguments?.getString("showId")?.toLongOrNull() ?: 0L
                TvShowDetailScreen(
                    showId = showId,
                    onBackClick = { navController.popBackStack() },
                    onPersonClick = { personId ->
                        navController.navigate(Screen.PersonDetail.createRoute(personId))
                    }
                )
            }
            composable(Screen.PersonDetail.route) { backStackEntry ->
                val personId = backStackEntry.arguments?.getString("personId")?.toLongOrNull() ?: 0L
                PersonDetailScreen(
                    personId = personId,
                    onBackClick = { navController.popBackStack() },
                    onMovieClick = { movieId ->
                        navController.navigate(Screen.MovieDetail.createRoute(movieId))
                    },
                    onShowClick = { showId ->
                        navController.navigate(Screen.TvShowDetail.createRoute(showId))
                    }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onMovieClick = { movieId ->
                        navController.navigate(Screen.MovieDetail.createRoute(movieId))
                    },
                    onShowClick = { showId ->
                        navController.navigate(Screen.TvShowDetail.createRoute(showId))
                    },
                    onPersonClick = { personId ->
                        navController.navigate(Screen.PersonDetail.createRoute(personId))
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.CustomLists.route) {
                /* Custom Lists Screen - coming soon */
                androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize())
            }
            composable(Screen.EpisodeCalendar.route) {
                /* Episode Calendar Screen - coming soon */
                androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize())
            }
            composable(Screen.NotificationSettings.route) {
                /* Notification Settings Screen - coming soon */
                androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize())
            }
            composable(Screen.YearInReview.route) {
                /* Year In Review Screen - coming soon */
                androidx.compose.foundation.layout.Box(modifier = androidx.compose.ui.Modifier.fillMaxSize())
            }
        }
    }
}
