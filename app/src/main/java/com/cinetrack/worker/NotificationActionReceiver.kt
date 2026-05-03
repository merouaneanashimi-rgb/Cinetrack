package com.cinetrack.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cinetrack.data.local.CineTrackDatabase
import com.cinetrack.data.local.dao.EpisodeDao
import com.cinetrack.data.local.dao.ShowDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var database: CineTrackDatabase

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "com.cinetrack.ACTION_MARK_WATCHED" -> {
                val showId = intent.getLongExtra("show_id", -1)
                if (showId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val episodeDao = database.episodeDao()
                        val nextUnwatched = episodeDao.getNextUnwatched(showId)
                        nextUnwatched?.let {
                            episodeDao.updateWatchedStatus(it.id, true, System.currentTimeMillis())
                        }
                    }
                }
            }
            "com.cinetrack.ACTION_DISMISS" -> {
                // Just dismiss the notification
            }
            "com.cinetrack.ACTION_OPEN_DETAIL" -> {
                val showId = intent.getLongExtra("show_id", -1)
                if (showId != -1L) {
                    // Open app to show detail
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                    launchIntent?.let {
                        it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        it.putExtra("show_id", showId)
                        context.startActivity(it)
                    }
                }
            }
        }
    }
}
