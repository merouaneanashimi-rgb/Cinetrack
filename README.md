# CineTrack

A premium, feature-rich Android app for tracking movies and TV shows. Combines the best features of MovieBase and Hobi TV into one superior product, with all premium features available for free.

## Features

### Core Features
- **Movie Tracking**: Watchlist, Watched, Favorites, Custom Lists
- **TV Show Tracking**: Watching, Watchlist, Watched, Dropped, Plan to Watch with Hobi-style air status system
- **Air Status System**: RETURNING, HIATUS, UPCOMING, ENDED, CANCELED, IN_PRODUCTION
- **Episode Management**: Mark watched/unwatched, rate individual episodes, add notes
- **Season Accordion**: Expandable seasons with episode lists and bulk actions
- **Search**: Unified search for movies, shows, and people with history
- **Discover**: Trending, Popular, Top Rated, Now Playing, Upcoming carousels
- **Statistics**: Charts for weekly activity, genre distribution, status distribution
- **Progress Tracking**: Continue watching, watch time calculation
- **Calendar View**: Episode calendar with airing episodes
- **Custom Lists**: Unlimited custom named lists
- **Person Following**: Follow actors/directors, get notified of new projects

### All Premium Features - Free
- Advanced filtering and sorting
- Unlimited custom lists
- Full backup & restore (JSON export/import)
- Extended statistics with Vico charts
- Episode calendar
- Airing status sub-tabs (Airing/Hiatus/Ended/Upcoming)
- Episode ratings and notes
- Watch time calculation
- Year in Review
- Streak tracking
- Home screen widgets (Glance)
- Person follow system
- Full notification system
- Bulk actions
- Grid layout customization
- Ad-free experience

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Local DB**: Room
- **Network**: Retrofit + OkHttp + Moshi
- **Images**: Coil
- **Charts**: Vico
- **Notifications**: WorkManager + NotificationCompat
- **Navigation**: Compose Navigation
- **Preferences**: DataStore
- **Widgets**: Glance
- **minSdk**: 26
- **targetSdk**: 34

## Setup

### 1. Get TMDB API Key (Free)

1. Go to [https://www.themoviedb.org/settings/api](https://www.themoviedb.org/settings/api)
2. Create a free account if you don't have one
3. Request an API key (select "Developer" option)
4. Fill in the required information
5. Your API key will be available in the API section of your account settings

### 2. Add API Key

Copy `local.properties.template` to `local.properties`:

```bash
cp local.properties.template local.properties
```n
Edit `local.properties` and add your TMDB API key:

```
TMDB_API_KEY=your_actual_tmdb_api_key_here
```

### 3. Build Debug APK

```bash
./gradlew assembleDebug
```

The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

### 4. Build Release APK

```bash
./gradlew assembleRelease
```

### 5. Sign Release APK

Generate a keystore:

```bash
keytool -genkey -v -keystore cinetrack-release.keystore -alias cinetrack -keyalg RSA -keysize 2048 -validity 10000
```

Sign the APK:

```bash
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore cinetrack-release.keystore app/build/outputs/apk/release/app-release-unsigned.apk cinetrack
```

Align the APK:

```bash
zipalign -v 4 app/build/outputs/apk/release/app-release-unsigned.apk CineTrack-release.apk
```

## Architecture

```
com.cinetrack/
├── data/
│   ├── local/          # Room database, entities, DAOs
│   ├── remote/         # Retrofit API service, DTOs
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Domain models (Show, Movie, Episode, etc.)
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Use cases (if needed)
├── di/                  # Hilt modules
├── presentation/
│   ├── base/           # Base classes
│   ├── components/     # Reusable UI components
│   ├── navigation/     # Navigation setup
│   ├── screens/        # Screen composables
│   │   ├── discover/
│   │   ├── movies/
│   │   ├── tvshows/
│   │   ├── detail/
│   │   ├── progress/
│   │   ├── settings/
│   │   ├── search/
│   │   └── customlist/
│   └── theme/          # Material theme
├── worker/             # Background workers
└── widget/             # Glance widgets
```

## TMDB Attribution

This product uses the TMDB API but is not endorsed or certified by TMDB.

## License

This project is open source and available under the MIT License.
