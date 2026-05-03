package com.cinetrack.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.cinetrack.R
import com.cinetrack.data.local.dao.ShowDao
import com.cinetrack.data.local.entity.AirStatus
import com.cinetrack.data.local.entity.ShowEntity
import com.cinetrack.data.remote.api.TmdbApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@HiltWorker
class EpisodeCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val showDao: ShowDao,
    private val api: TmdbApiService
) : CoroutineWorker(context, params) {

    companion object {
        private const val WORK_NAME = "episode_check_worker"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<EpisodeCheckWorker>(6, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
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
            val trackedShows = showDao.getAllTracked().first()
            trackedShows.forEach { show ->
                checkShowForUpdates(show)
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private suspend fun checkShowForUpdates(show: ShowEntity) {
        try {
            val response = api.getTvShowDetail(show.tmdbId)
            if (response.isSuccessful) {
                val detail = response.body() ?: return

                val newAirStatus = computeAirStatus(detail.status, detail.nextEpisodeToAir != null)
                val oldAirStatus = show.airStatus

                // Check for next episode airing soon
                detail.nextEpisodeToAir?.airDate?.let { airDateStr ->
                    try {
                        val airDate = LocalDate.parse(airDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                        val daysUntil = Duration.between(LocalDate.now().atStartOfDay(), airDate.atStartOfDay()).toDays()

                        if (daysUntil in 0..1) {
                            sendEpisodeNotification(show, detail.nextEpisodeToAir!!.name ?: "New Episode", detail.nextEpisodeToAir!!.seasonNumber ?: 0, detail.nextEpisodeToAir!!.episodeNumber ?: 0)
                        } else if (daysUntil in 2..7) {
                            sendComingSoonNotification(show, daysUntil.toInt())
                        }
                    } catch (e: Exception) { }
                }

                // Check for status changes
                if (oldAirStatus == AirStatus.RETURNING && newAirStatus == AirStatus.HIATUS) {
                    sendSeasonEndedNotification(show)
                }

                if (oldAirStatus == AirStatus.HIATUS && newAirStatus == AirStatus.RETURNING) {
                    sendSeasonPremiereNotification(show, detail.nextEpisodeToAir?.seasonNumber ?: 0)
                }

                // Check for series finale
                if (newAirStatus == AirStatus.ENDED && detail.nextEpisodeToAir != null) {
                    val isLastEpisode = detail.nextEpisodeToAir!!.episodeNumber == detail.numberOfEpisodes
                    if (isLastEpisode) {
                        sendSeriesFinaleNotification(show, detail.nextEpisodeToAir!!.name ?: "Finale")
                    }
                }
            }
        } catch (e: Exception) {
            // Log error but continue with other shows
        }
    }

    private fun computeAirStatus(status: String?, hasNextEpisode: Boolean): AirStatus {
        return when (status) {
            "Returning Series" -> if (hasNextEpisode) AirStatus.RETURNING else AirStatus.HIATUS
            "In Production" -> AirStatus.IN_PRODUCTION
            "Planned", "Pilot" -> AirStatus.UPCOMING
            "Ended" -> AirStatus.ENDED
            "Canceled" -> AirStatus.CANCELED
            else -> AirStatus.UPCOMING
        }
    }

    private fun sendEpisodeNotification(show: ShowEntity, episodeTitle: String, season: Int, episode: Int) {
        val intent = Intent(applicationContext, NotificationActionReceiver::class.java).apply {
            action = "com.cinetrack.ACTION_OPEN_DETAIL"
            putExtra("show_id", show.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            show.tmdbId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val markWatchedIntent = Intent(applicationContext, NotificationActionReceiver::class.java).apply {
            action = "com.cinetrack.ACTION_MARK_WATCHED"
            putExtra("show_id", show.id)
        }
        val markWatchedPendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            show.tmdbId + 10000,
            markWatchedIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "ch_episodes")
            .setSmallIcon(R.drawable.ic_splash)
            .setContentTitle("${show.title} tonight")
            .setContentText("S${season}E${episode} · $episodeTitle airs today")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(0, "Mark Watched", markWatchedPendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(show.tmdbId, notification)
    }

    private fun sendComingSoonNotification(show: ShowEntity, daysUntil: Int) {
        val notification = NotificationCompat.Builder(applicationContext, "ch_episodes")
            .setSmallIcon(R.drawable.ic_splash)
            .setContentTitle("${show.title} coming soon")
            .setContentText("New episode airs in $daysUntil days")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(show.tmdbId + 5000, notification)
    }

    private fun sendSeasonEndedNotification(show: ShowEntity) {
        val notification = NotificationCompat.Builder(applicationContext, "ch_episodes")
            .setSmallIcon(R.drawable.ic_splash)
            .setContentTitle("${show.title} season ended")
            .setContentText("Season has ended. Next season TBD.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(show.tmdbId + 20000, notification)
    }

    private fun sendSeasonPremiereNotification(show: ShowEntity, seasonNumber: Int) {
        val notification = NotificationCompat.Builder(applicationContext, "ch_episodes")
            .setSmallIcon(R.drawable.ic_splash)
            .setContentTitle("${show.title} is back!")
            .setContentText("Season $seasonNumber has started!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(show.tmdbId + 30000, notification)
    }

    private fun sendSeriesFinaleNotification(show: ShowEntity, finaleTitle: String) {
        val notification = NotificationCompat.Builder(applicationContext, "ch_episodes")
            .setSmallIcon(R.drawable.ic_splash)
            .setContentTitle("${show.title} series finale")
            .setContentText("$finaleTitle is the last episode ever.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(show.tmdbId + 40000, notification)
    }
}
