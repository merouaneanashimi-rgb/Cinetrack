package com.cinetrack.di

import android.content.Context
import androidx.room.Room
import com.cinetrack.data.local.CineTrackDatabase
import com.cinetrack.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CineTrackDatabase {
        return Room.databaseBuilder(
            context,
            CineTrackDatabase::class.java,
            "cinetrack_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideShowDao(database: CineTrackDatabase): ShowDao = database.showDao()

    @Provides
    fun provideSeasonDao(database: CineTrackDatabase): SeasonDao = database.seasonDao()

    @Provides
    fun provideEpisodeDao(database: CineTrackDatabase): EpisodeDao = database.episodeDao()

    @Provides
    fun provideMovieDao(database: CineTrackDatabase): MovieDao = database.movieDao()

    @Provides
    fun providePersonDao(database: CineTrackDatabase): PersonDao = database.personDao()

    @Provides
    fun provideNotificationHistoryDao(database: CineTrackDatabase): NotificationHistoryDao = database.notificationHistoryDao()

    @Provides
    fun provideSearchHistoryDao(database: CineTrackDatabase): SearchHistoryDao = database.searchHistoryDao()

    @Provides
    fun provideCustomListDao(database: CineTrackDatabase): CustomListDao = database.customListDao()

    @Provides
    fun provideCustomListItemDao(database: CineTrackDatabase): CustomListItemDao = database.customListItemDao()
}
