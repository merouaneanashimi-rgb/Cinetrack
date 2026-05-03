package com.cinetrack.domain.model

data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: String = "#01B4E4",
    val gridColumns: Int = 3,
    val cardStyle: CardStyle = CardStyle.POSTER_INFO,
    val ratingScale: RatingScale = RatingScale.STARS_5,
    val defaultLanguage: String = "en",
    val adultContent: Boolean = false,
    val region: String = "US",
    val defaultShowList: UserListType = UserListType.WATCHING,
    val autoMarkWatched: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val quietHoursStart: Int? = 23,
    val quietHoursEnd: Int? = 8
)

enum class ThemeMode {
    DARK, LIGHT, SYSTEM
}

enum class CardStyle {
    POSTER_ONLY, POSTER_INFO, LIST_ROW
}

enum class RatingScale {
    STARS_5, NUMERIC_10
}
