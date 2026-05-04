package com.cinetrack.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cinetrack.data.local.dao.*
import com.cinetrack.data.local.entity.*

@Database(
    entities = [
        ShowEntity::class,
        SeasonEntity::class,
        EpisodeEntity::class,
        MovieEntity::class,
        PersonEntity::class,
        NotificationHistoryEntity::class,
        SearchHistoryEntity::class,
        CustomListEntity::class,
        CustomListItemEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class CineTrackDatabase : RoomDatabase() {
    abstract fun showDao(): ShowDao
    abstract fun seasonDao(): SeasonDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun movieDao(): MovieDao
    abstract fun personDao(): PersonDao
    abstract fun notificationHistoryDao(): NotificationHistoryDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun customListDao(): CustomListDao
    abstract fun customListItemDao(): CustomListItemDao
}
