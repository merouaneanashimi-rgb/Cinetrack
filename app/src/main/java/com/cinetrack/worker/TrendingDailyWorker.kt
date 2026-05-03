package com.cinetrack.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.cinetrack.R
import com.cinetrack.data.remote.api.TmdbApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class TrendingDailyWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val api: TmdbApiService
) : CoroutineWorker(applicationContext, params) {

    companion object {
        private const val WORK_NAME = "trending_daily_worker"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<TrendingDailyWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(1, TimeUnit.HOURS)
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
            val response = api.getTrending("all", "day")
            if (response.isSuccessful) {
                val results = response.body()?.results ?: emptyList()
                val topItem = results.firstOrNull() ?: return Result.success()

                val title = topItem.title ?: topItem.name ?: "Trending Title"
                val rating = topItem.voteAverage

                val notification = NotificationCompat.Builder(applicationContext, "ch_reminders")
                    .setSmallIcon(R.drawable.ic_splash)
                    .setContentTitle("🔥 Trending: $title")
                    .setContentText("Rating: ${String.format("%.1f", rating)}⭐ · Tap to explore")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setAutoCancel(true)
                    .build()

                val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(80000, notification)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
