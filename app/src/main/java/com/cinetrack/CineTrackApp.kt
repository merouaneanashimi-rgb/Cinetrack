package com.cinetrack

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.cinetrack.worker.EpisodeCheckWorker
import com.cinetrack.worker.MovieReleaseWorker
import com.cinetrack.worker.StreakWorker
import com.cinetrack.worker.TrendingDailyWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CineTrackApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleWorkers()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    "ch_episodes",
                    getString(R.string.notification_channel_episodes),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for upcoming and airing episodes"
                },
                NotificationChannel(
                    "ch_movies",
                    getString(R.string.notification_channel_movies),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for movie releases"
                },
                NotificationChannel(
                    "ch_reminders",
                    getString(R.string.notification_channel_reminders),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Gentle reminders about your watchlist"
                },
                NotificationChannel(
                    "ch_social",
                    getString(R.string.notification_channel_social),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Streak updates and motivational messages"
                }
            )

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(channels)
        }
    }

    private fun scheduleWorkers() {
        EpisodeCheckWorker.schedule(this)
        MovieReleaseWorker.schedule(this)
        StreakWorker.schedule(this)
        TrendingDailyWorker.schedule(this)
    }
}
