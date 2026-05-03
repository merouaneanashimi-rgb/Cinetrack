package com.cinetrack.presentation.navigation

sealed class Screen(val route: String) {
    data object Discover : Screen("discover")
    data object Movies : Screen("movies")
    data object TvShows : Screen("tv_shows")
    data object Progress : Screen("progress")
    data object Settings : Screen("settings")
    data object MovieDetail : Screen("movie_detail/{movieId}") {
        fun createRoute(movieId: Long) = "movie_detail/$movieId"
    }
    data object TvShowDetail : Screen("tv_show_detail/{showId}") {
        fun createRoute(showId: Long) = "tv_show_detail/$showId"
    }
    data object PersonDetail : Screen("person_detail/{personId}") {
        fun createRoute(personId: Long) = "person_detail/$personId"
    }
    data object Search : Screen("search")
    data object CustomLists : Screen("custom_lists")
    data object CustomListDetail : Screen("custom_list_detail/{listId}") {
        fun createRoute(listId: Long) = "custom_list_detail/$listId"
    }
    data object EpisodeCalendar : Screen("episode_calendar")
    data object NotificationSettings : Screen("notification_settings")
    data object YearInReview : Screen("year_in_review")
}
