package com.cinetrack.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.cinetrack.R
import com.cinetrack.data.local.dao.EpisodeDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@HiltWorker
class StreakWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val episodeDao: EpisodeDao
) : CoroutineWorker(applicationContext, params) {

    companion object {
        private const val WORK_NAME = "streak_worker"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<StreakWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val recentEpisodes = episodeDao.getRecentlyWatched(1000).first()
            val watchDates = recentEpisodes.mapNotNull { ep ->
                ep.watchedAt?.let { watchedAt ->
                    Instant.ofEpochMilli(watchedAt)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
            }.distinct()

            val streakDays = calculateStreak(watchDates)

            if (streakDays in listOf(3, 7, 14, 30, 50, 100)) {
                sendStreakNotification(streakDays)
            }

            // Check if streak was broken
            if (streakDays == 0 && watchDates.isNotEmpty()) {
                val yesterday = LocalDate.now().minusDays(1)
                val hadStreak = watchDates.contains(yesterday)
                if (hadStreak) {
                    val previousStreak = calculatePreviousStreak(watchDates)
                    if (previousStreak >= 5) {
                        sendStreakBrokenNotification(previousStreak)
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun calculateStreak(dates: List<LocalDate>): Int {
        if (dates.isEmpty()) return 0
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        // Check if watched today or yesterday
        val mostRecent = dates.maxOrNull() ?: return 0
        if (mostRecent != today && mostRecent != yesterday) return 0

        var streak = 1
        var checkDate = mostRecent.minusDays(1)

        while (dates.contains(checkDate)) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        return streak
    }

    private fun calculatePreviousStreak(dates: List<LocalDate>): Int {
        val sortedDates = dates.sortedDescending()
        if (sortedDates.isEmpty()) return 0

        var streak = 1
        for (i in 1 until sortedDates.size) {
            if (sortedDates[i] == sortedDates[i - 1].minusDays(1)) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    private fun sendStreakNotification(streakDays: Int) {
        val notification = NotificationCompat.Builder(applicationContext, "ch_social")
            .setSmallIcon(R.drawable.ic_splash)
            .setContentTitle("🔥 $streakDays-day streak!")
            .setContentText("You're on fire! Keep watching to maintain your streak.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(90000 + streakDays, notification)
    }

    private fun sendStreakBrokenNotification(previousStreak: Int) {
        val notification = NotificationCompat.Builder(applicationContext, "ch_social")
            .setSmallIcon(R.drawable.ic_splash)
            .setContentTitle("Streak ended")
            .setContentText("Your $previousStreak-day streak ended. Get back on track tonight?")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(95000, notification)
    }
}
