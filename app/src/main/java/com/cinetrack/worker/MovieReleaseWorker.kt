package com.cinetrack.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.cinetrack.R
import com.cinetrack.data.local.dao.MovieDao
import com.cinetrack.data.local.entity.UserListType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@HiltWorker
class MovieReleaseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val movieDao: MovieDao
) : CoroutineWorker(context, params) {

    companion object {
        private const val WORK_NAME = "movie_release_worker"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<MovieReleaseWorker>(12, TimeUnit.HOURS)
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

    override suspend fun doWork(): androidx.work.ListenableWorker.Result {
        return try {
            val watchlistMovies = movieDao.getByListType(UserListType.WATCHLIST).first()
            val today = LocalDate.now()

            watchlistMovies.forEach { movie ->
                movie.releaseDate?.let { releaseDateStr ->
                    try {
                        val releaseDate = LocalDate.parse(releaseDateStr, DateTimeFormatter.ISO_LOCAL_DATE)
                        val daysUntil = Duration.between(today.atStartOfDay(), releaseDate.atStartOfDay()).toDays()

                        when {
                            daysUntil == 0L -> {
                                sendReleaseNotification(movie.title, "${movie.title} is in theaters today!")
                            }
                            daysUntil in 1..7 -> {
                                sendReleaseNotification(movie.title, "${movie.title} releases in $daysUntil days")
                            }
                        }
                    } catch (e: Exception) { }
                }
            }
            androidx.work.ListenableWorker.Result.success()
        } catch (e: Exception) {
            androidx.work.ListenableWorker.Result.retry()
        }
    }

    private fun sendReleaseNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(applicationContext, "ch_movies")
            .setSmallIcon(R.drawable.ic_splash)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(title.hashCode(), notification)
    }
}
