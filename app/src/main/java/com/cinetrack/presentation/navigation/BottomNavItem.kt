package com.cinetrack.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.cinetrack.R

data class BottomNavItem(
    val route: String,
    val labelResId: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    companion object {
        val items = listOf(
            BottomNavItem(
                route = Screen.Discover.route,
                labelResId = R.string.nav_discover,
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home
            ),
            BottomNavItem(
                route = Screen.Movies.route,
                labelResId = R.string.nav_movies,
                selectedIcon = Icons.Filled.Movie,
                unselectedIcon = Icons.Outlined.Movie
            ),
            BottomNavItem(
                route = Screen.TvShows.route,
                labelResId = R.string.nav_tv_shows,
                selectedIcon = Icons.Filled.LiveTv,
                unselectedIcon = Icons.Outlined.LiveTv
            ),
            BottomNavItem(
                route = Screen.Progress.route,
                labelResId = R.string.nav_progress,
                selectedIcon = Icons.Filled.BarChart,
                unselectedIcon = Icons.Outlined.BarChart
            ),
            BottomNavItem(
                route = Screen.Trakt.route,
                labelResId = R.string.nav_trakt,
                selectedIcon = Icons.Filled.Sync,
                unselectedIcon = Icons.Outlined.Sync
            ),
            BottomNavItem(
                route = Screen.Settings.route,
                labelResId = R.string.nav_settings,
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings
            )
        )
    }
}
